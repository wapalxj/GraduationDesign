package graduationdesign.muguihai.com.v023.goo_view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 *
 * Created by vero on 2016/4/8.
 */
public class GooView extends View{
    private Paint mPaint;
    public GooView(Context context) {
        super(context);
        init();
    }

    public GooView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        //参数：抗锯齿
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
    }

    private float dragRadius=20;//拖拽圆的半径
    private float stickyRadius=20;//固定圆的半径
    private PointF dragCenter=new PointF(100f,120f);//拖拽圆的圆心
    private PointF stickyCenter=new PointF(200f,120f);//固定圆的圆心
    private PointF[] stickyPoint={new PointF(180f,100f),new PointF(180f,140f)};//2个粘性点
    private PointF[] dragPoint={new PointF(100f,100f),new PointF(100f,140f)};//2个固定点

    private PointF ctrlPoint=new PointF(140f,120f);//控制点(中点)
    private double lineK;//斜率
    private boolean isDragOutOfRange=false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //让整体画布让下偏移状态栏的高度
        canvas.translate(0,-Utils.getStatusBarHeight(getResources()));

        stickyRadius=getStickyRadius();
        //根据dragCenter动态求出stickyPoint、stickyPoint
        //stickyPoint:2个圆圆心连线的垂线与drag圆的交点
        float xOffset=dragCenter.x-stickyCenter.x;
        float yOffset=dragCenter.y-stickyCenter.y;
        if (xOffset!=0){//在圆不在同一垂直线上
            lineK=yOffset/xOffset;
        }
        //利用工具类计算斜率为lineK的过圆心的直线与圆的交点
        dragPoint=GeometryUtil.getIntersectionPoints(dragCenter,dragRadius,lineK);
        stickyPoint=GeometryUtil.getIntersectionPoints(stickyCenter,stickyRadius,lineK);
        ctrlPoint=GeometryUtil.getPointByPercent(dragCenter,stickyCenter,0.618f);



        if (!isDragOutOfRange){
            //绘制
            canvas.drawCircle(stickyCenter.x,stickyCenter.y,stickyRadius,mPaint);
            canvas.drawCircle(dragCenter.x,dragCenter.y,dragRadius,mPaint);
            //贝塞尔曲线
            Path path =new Path();
            //设置起点stickyPoint[0]
            path.moveTo(stickyPoint[0].x,stickyPoint[0].y);
            //使用贝塞尔曲线:控制点(中间点ctrlPoint),end:粘性点（dragPoint）
            path.quadTo(ctrlPoint.x,ctrlPoint.y,dragPoint[0].x,dragPoint[0].y);
            //连接到dragPoint[1]
            path.lineTo(dragPoint[1].x,dragPoint[1].y);
            //使用贝塞尔曲线:控制点(中间点ctrlPoint),end:固定点（stickyPoint）
            path.quadTo(ctrlPoint.x,ctrlPoint.y,stickyPoint[1].x,stickyPoint[1].y);
            //        path.close();//默认闭合，不用调用
            canvas.drawPath(path,mPaint);
        }

        //绘制范围圆圈
        //以固定圆为圆心，300为半径
        mPaint.setStyle(Paint.Style.STROKE);//设置只绘制边线
        canvas.drawCircle(stickyCenter.x,stickyCenter.y,maxDistance,mPaint);
        mPaint.setStyle(Paint.Style.FILL);//再设置回来


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x=event.getRawX();
        float y=event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                dragCenter.set(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                dragCenter.set(x,y);

                if (GeometryUtil.getDistanceBetween2Points(dragCenter,stickyCenter)>maxDistance){
                    //超出范围，断掉(不再绘制)
                    isDragOutOfRange=true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (GeometryUtil.getDistanceBetween2Points(dragCenter,stickyCenter)>maxDistance){
                    //超出范围，还原
                    dragCenter.set(stickyCenter.x,stickyCenter.y);
                }else {
                    //弹回去
                    if (isDragOutOfRange){
                        //如果曾经超出(释放的时候不超出)

                    }else {
                        ValueAnimator valueAnimator= ObjectAnimator.ofFloat(1);
                        final PointF start=new PointF(dragCenter.x,dragCenter.y);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                //执行百分比
                                float animFraction=animation.getAnimatedFraction();
                                PointF pointF=GeometryUtil.getPointByPercent(start,stickyCenter,animFraction);
                                //弹回
                                dragCenter.set(pointF);
                                invalidate();//刷新
                            }
                        });
                        valueAnimator.setDuration(500);
                        valueAnimator.setInterpolator(new OvershootInterpolator(3));
                        valueAnimator.start();
                    }
                }
                break;
            default:
                break;
        }
        invalidate();

        return true;
    }


    //动态求出固定圆的半径
    private float maxDistance=300;

    private float getStickyRadius(){
        float radius;
        float centerDis=GeometryUtil.getDistanceBetween2Points(dragCenter,stickyCenter);
        float fraction=centerDis/maxDistance;
        radius=GeometryUtil.evaluateValue(fraction,20f,4f);//固定圆的半径在12-4之间变化
        return radius;
    }
}
