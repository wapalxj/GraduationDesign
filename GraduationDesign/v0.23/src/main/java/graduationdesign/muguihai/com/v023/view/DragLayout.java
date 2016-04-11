package graduationdesign.muguihai.com.v023.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import graduationdesign.muguihai.com.v023.slidemenu.ColorUtil;


/**
 * Created by vero on 2016/4/6.
 */
public class DragLayout  extends FrameLayout{

    private View redChildView;
    private View blueChildView;
    private ViewDragHelper viewDragHelper;

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragLayout(Context context) {
        super(context);
        init();
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        viewDragHelper=ViewDragHelper.create(this,callback);
    }

    /**
     * 当当前类(DragLayout)的XML布局的结束标签被读取完成会执行该方法，此时会知道自己有几个子view
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        redChildView = getChildAt(0);//第0个子view
        blueChildView = getChildAt(1);//第1个子view
    }

    /**
     * 测量自己的子view
     */
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        //第1种方式
////        int size=(int)getResources().getDimension(R.dimen.width);
////        redChildView.measure(size,MeasureSpec.EXACTLY);
//
//        //第2种方式
////        int measureSpec=MeasureSpec.makeMeasureSpec(redChildView.getLayoutParams().width, MeasureSpec.EXACTLY);
////        redChildView.measure(measureSpec, measureSpec);//宽高
////        blueChildView.measure(measureSpec, measureSpec);
//
//        //第3种测量方式
//        measureChild(redChildView, widthMeasureSpec, heightMeasureSpec);
//        measureChild(blueChildView,widthMeasureSpec,heightMeasureSpec);
//    }

    /**
     * 布局
     * @param changed
     * @param l: 左边的左标
     * @param t:顶部的左标
     * @param r：右侧的左标
     * @param b：底部的坐标
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //getMeasuredWidth()---->获取当前view的宽度(Draglayout)
//        int left=getPaddingLeft()+getMeasuredWidth()/2-redChildView.getMeasuredWidth()/2;//处于中间
        int left=getPaddingLeft();
        int top=getPaddingTop();
        redChildView.layout(
                left,
                top,
                left + redChildView.getMeasuredWidth(),
                top + redChildView.getMeasuredHeight()
        );

        blueChildView.layout(
                left,
                redChildView.getBottom(),
                left + redChildView.getMeasuredWidth(),
                redChildView.getBottom() + redChildView.getMeasuredHeight()
        );


    }

    /**
     *事件拦截
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        //让viewDragHelper帮我们判断是否应该拦截
        boolean result=viewDragHelper.shouldInterceptTouchEvent(event);
        return result;
    }

    /**
     * 触摸事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //事件传入:将触摸事件交给viewDragHelper解析处理
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * ViewDragHelper回调
     */
    private ViewDragHelper.Callback callback=new ViewDragHelper.Callback() {
        /**
         *用于判断是否捕获当前child的触摸事件
         * @param child:当前触摸的子view
         * @param pointerId:
         * @return:true捕获并解析 false:不处理
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child==blueChildView|| child==redChildView;
        }

        /**
         * 当view被开始捕获和解析的回调
         * @param capturedChild:当前被捕获的子view
         * @param activePointerId:
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
            Log.i("onViewCaptured","ChildView被捕获了");
        }

        /**
         * 获取view的水平拖拽的范围,但是不能再这里限制边界
         * 最好不要返回0
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {

//            return super.getViewVerticalDragRange(child);
            return super.getViewHorizontalDragRange(child);
        }
        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }

        /**
         * 控制view在水平方向上的移动
         * @param child
         * @param left:表示viewDragHelper认为你想让当前child的left改变的值
         *            计算的值：left=child.getLeft()+dx
         * @param dx:view本次移动的距离
         * @return: 表示真正想让child的left变成的值
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.i("left","left----"+left+"---dx");
            if (left>=getMeasuredWidth()-child.getMeasuredWidth()){
                left=getMeasuredWidth()-child.getMeasuredWidth();
            }else if (left<=0){
                left=0;
            }
            return left;
        }
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            Log.i("top","top----"+top+"---dy");
            if (top>=getMeasuredHeight()-child.getMeasuredHeight()){
                top=getMeasuredHeight()-child.getMeasuredHeight();
            }else if (top<=0){
                top=0;
            }
            return top;
        }

        /**
         *当child的位置改变的时候执行,一般用来做让其他view伴随移动
         * @param changedView:位置改变的view
         * @param left:当前最新的left
         * @param top:当前最新的top
         * @param dx:本次水平移动的距离
         * @param dy:本次垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            Log.i("onViewPositionChanged",
                    "left->:"+left+
                    "----top->:"+top+
                    "----dx->:"+dx+
                    "----dy->:"+dy);

            //跟随移动
            if (changedView==blueChildView){
                //让 red跟随移动
                redChildView.layout(
                        redChildView.getLeft()+dx,
                        redChildView.getTop()+dy,
                        redChildView.getRight()+dx,
                        redChildView.getBottom()+dy
                );
            }else if (changedView==redChildView){
                //让 blue跟随移动
                blueChildView.layout(
                        blueChildView.getLeft()+dx,
                        blueChildView.getTop()+dy,
                        blueChildView.getRight()+dx,
                        blueChildView.getBottom()+dy
                );
            }

            //1.计算view移动的百分比
            float fraction=changedView.getLeft()*1f/(getMeasuredWidth()-changedView.getMeasuredWidth());
            Log.i("移动百分比",""+fraction);
            //2.执行一系列的伴随动画
            executeAnim(fraction);
        }

        /**
         * 手指抬起的时候执行的方法
         * @param releasedChild:当前抬起的view
         * @param xvel:x方向的移动速度
         * @param yvel:y方向的移动速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int centerLeft=getMeasuredWidth()/2-releasedChild.getMeasuredWidth()/2;
            if (releasedChild.getLeft()<centerLeft){
                //在左半边，向左缓慢移动
                viewDragHelper.smoothSlideViewTo(releasedChild, 0, releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);//类似invalidate();
            }else {
                //在右半边，向右缓慢移动
                viewDragHelper.smoothSlideViewTo(releasedChild, getMeasuredWidth()-redChildView.getMeasuredWidth(), releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);//类似invalidate();
            }
        }


    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(DragLayout.this);//类似invalidate();

        }
    }

    /**
     * 执行伴随动画
     * @param fraction：百分比
     */
    private void executeAnim(float fraction){
        //:缩放
//        redChildView.setScaleX(1+fraction*1.5f);
//        redChildView.setScaleY(1+fraction*1.5f);

        //翻转
        redChildView.setRotation(360*fraction);

        //平移
//        redChildView.setTranslationX(100*fraction);

        //透明
//        redChildView.setAlpha(1-fraction);

        //变色
        redChildView.setBackgroundColor((Integer) ColorUtil.evaluateColor(fraction, Color.RED, Color.GREEN));
    }
}

