package com.muguihai.beta1.view.swipe_delete;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 *
 * Created by vero on 2016/4/8.
 */
public class SwipeLayout extends FrameLayout{

    private View contentView;//内容区域
    private View deleteView;//删除区域
    private int delete_height;//delete高度
    private int delete_width;//delete寬度
    private int content_height;
    private int content_width;

    private ViewDragHelper mViewDragHelper;

    public SwipeLayout(Context context) {
        super(context);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    //删除打开状态
    enum SwipeState{Open,Close}

    private SwipeState currentState= SwipeState.Close;//默认为关闭

    private void init() {
        mViewDragHelper=ViewDragHelper.create(SwipeLayout.this,callback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = getChildAt(0);
        deleteView = getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        delete_height = deleteView.getMeasuredHeight();
        delete_width = deleteView.getMeasuredWidth();
        content_height = contentView.getMeasuredHeight();
        content_width = contentView.getMeasuredWidth();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        contentView.layout(0,0,content_width,content_height);
        deleteView.layout(content_width,0,content_width+delete_width,delete_height);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result=mViewDragHelper.shouldInterceptTouchEvent(ev);

        //如果当前又打开的，拦截后交给onTouch处理
        //这是保证onTouchEvent中的第一个if能被执行
        if (!SwipeLayoutManager.getInstance().canBeSwiped(this)){
            //关闭已经打开的layout
            SwipeLayoutManager.getInstance().closeCurrentLayout();
            return true;
        }
        return result;
    }


    private float downX,downY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //判断当前是否可以滑动，打开了某一个，则不会执行if下面的代码
        //即：点击listview关闭
        if (!SwipeLayoutManager.getInstance().canBeSwiped(this)){

//            SwipeLayoutManager.getInstance().closeCurrentLayout();
            //上行代码最好放在上面的onInterceptTouchEvent()中,
            // 因为不断的点击会不断的调用这个方法导致卡顿

            requestDisallowInterceptTouchEvent(true);
            return true;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //获取按下的坐标
                downX=event.getX();
                downY=event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //获取移动的距离
                float moveX=event.getX();
                float moveY=event.getY();
                float dX=moveX-downX;//X方向移动的距离
                float dY=moveY-downY;//Y方向移动的距离
                if (Math.abs(dX)>Math.abs(dY)){
                    //偏向于水平方向
                    //SwipeLayout处理，而请求listView不拦截：解决delete划出的时候listview还能上下移动的bug
                    requestDisallowInterceptTouchEvent(true);
                }
                //更新当前的位置
                downX=moveX;
                downY=moveY;
                break;
            case MotionEvent.ACTION_UP:
                //获取按下的坐标
                downX=event.getX();
                downY=event.getY();
                break;
        }

        //事件交给我处理
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback=new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child==contentView||child==deleteView;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            //范围正好是刪除view的范围
            return delete_width;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child==contentView){
                if (left>=0){left=0;}
                if (left<-delete_width){left=-delete_width;}
            }else if (child==deleteView){
                if (left>content_width){left=content_width;}
                if (left<=content_width-delete_width){left=content_width-delete_width;}
            }

            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView==contentView){
                //手动移动deleteView
                deleteView.layout(
                        deleteView.getLeft()+dx,
                        deleteView.getTop()+dy,
                        deleteView.getRight()+dx,
                        deleteView.getBottom()+dy
                );
            }else if (changedView==deleteView){
                //手动移动deleteView
                contentView.layout(
                        contentView.getLeft()+dx,
                        contentView.getTop()+dy,
                        contentView.getRight()+dx,
                        contentView.getBottom()+dy
                );
            }

            //判断开关
            if (contentView.getLeft()==0 && currentState!= SwipeState.Close){
                //关闭
                //state更改
                currentState= SwipeState.Close;

                //回调关闭的方法
                if (listener!=null){
                    listener.onClose(getTag());
                }

                //已经关闭，让manager清除
                SwipeLayoutManager.getInstance().clearCurrentLayout();

            }else if (contentView.getLeft()==-delete_width && currentState!= SwipeState.Open){
                //打开
                currentState= SwipeState.Open;

                //回调关闭的方法
                if (listener!=null){
                    listener.onOpen(getTag());
                }


                //已经打开，让manager记录
                SwipeLayoutManager.getInstance().setSwipeLayout(SwipeLayout.this);

            }

        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            if (contentView.getLeft()<-delete_width/2){
                //打开
                openDelete();
            }else {
                //打开
               closeDelete();
            }
        }
    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mViewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
        }
    }

    //打开删除条
    public void openDelete(){
        mViewDragHelper.smoothSlideViewTo(contentView,-delete_width,contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }
    //关闭删除条
    public void closeDelete(){
        mViewDragHelper.smoothSlideViewTo(contentView,0,contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }

    /**
     * 接口回调:暴露状态给外界
     */
    private OnSwipeChangeListener listener;
    public void setOnSwipeChangeListener(OnSwipeChangeListener listener){
        this.listener=listener;
    }
    public interface OnSwipeChangeListener{
        void onOpen(Object tag);
        void onClose(Object tag);
    }


}
