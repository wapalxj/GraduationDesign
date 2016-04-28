package com.muguihai.beta1.view.slidemenu;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.IntEvaluator;

/**
 * Created by vero on 2016/4/7.
 */
public class SlideMenu extends FrameLayout{
    //菜单
    private View mMenuView;
    //主界面
    private View mMainView;

    private ViewDragHelper mViewDragHelper;
    private int width;//自己的宽度
    private float dragRange;//拖拽范围
    private IntEvaluator intEvaluator;//int数值计算

    //定义状态常量:当前SlideMenu的开启状态
    enum DragState{Open,Close}
    private DragState curState= DragState.Close;

    public SlideMenu(Context context) {
        super(context);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mViewDragHelper=ViewDragHelper.create(SlideMenu.this, mCallback);
        intEvaluator=new IntEvaluator();
    }

    //获取当前状态
    public DragState getCurrentState(){
        return curState;
    }

    private ViewDragHelper.Callback mCallback=new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            return child==mMainView||child==mMenuView;
        }

        /**
         * 水平方向移动的范围
         * @param child:被捕捉的view
         * @return  水平方向移动的范围
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return (int)dragRange;
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
            if (child==mMainView){
                //限制main的边界
                if (left<=0) left=0;
                if (left>dragRange) left=(int)dragRange;
            }
//            else
//          if (child==mMenuView){
//                //限制main的边界:不让menu移动
//                left=left-dx;
//            }

            return left;
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
            if (changedView==mMenuView){
                //手动固定menu，不让menu移动
                mMenuView.layout(0,0,mMenuView.getMeasuredWidth(),mMenuView.getMeasuredHeight());
                //让main移动,需要在进行限制边界
                int newLeft= mMainView.getLeft()+dx;
                if (newLeft<=0){newLeft=0;}
                if (newLeft>dragRange){newLeft=(int)dragRange;}
                mMainView.layout(
                        newLeft,
                        mMainView.getTop()+dy,
                        newLeft+mMainView.getMeasuredWidth(),
                        mMainView.getBottom()+dy
                );
            }

            //计算百分比+执行伴随动画
            float fraction=mMainView.getLeft()/dragRange;
            executeAnim(fraction);

            //接口回调
            if (fraction==0 && curState!= DragState.Close){//SlideMenu为关闭状态
                curState= DragState.Close;
                if (listener!=null){
                    listener.onClose();
                }

            }else if (fraction==1f && curState!= DragState.Open){//SlideMenu为开启状态
                curState= DragState.Open;
                if (listener!=null){
                    listener.onOpen();
                }
            }
            //将fraction暴露给外界
            if (listener!=null){
                listener.onDraging(fraction);
            }

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
            if (mMainView.getLeft()<dragRange/2){
                //左半边
                closeMenu();
               }else {
                //右半边
                openMenu();
              }
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //抛出异常处理
        if (getChildCount()!=2){
            throw new IllegalArgumentException("SlideMenu only has 2 child view!");
        }
        mMenuView = getChildAt(0);
        mMainView = getChildAt(1);
    }

    /**
     * 改方法在onMeasure()后执行,可以在该方法中初始化自己和子view的宽高
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width =getMeasuredWidth();
        dragRange=width*0.85f;
    }


    /**
     *用于刷新平滑滚动
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mViewDragHelper.continueSettling(true)) {
            //动画没结束则继续
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);//类似invalidate();
        }
    }

    //打开菜单
    public void openMenu(){
        mViewDragHelper.smoothSlideViewTo(mMainView,(int)dragRange,mMainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }
    //关闭菜单
    public void closeMenu(){
        mViewDragHelper.smoothSlideViewTo(mMainView,0,mMainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }



    /**
     *
     *
     * 伴随动画
     * @param fraction
     */
    private void executeAnim(float fraction){
        //menu的移动:从负的宽的1/4到0
        mMenuView.setTranslationX(intEvaluator.evaluate(fraction,-mMenuView.getMeasuredWidth()/4,0));

//        //main头像透明度变化--->通过接口回调处理
//        ImageView main_head= (ImageView) findViewById(R.id.iv_head);
//        main_head.setAlpha(1-fraction);

    }

    /**
     * 接口回调
     */
    private OnDragStateChangeListener listener;
    public void setOnDragStateChangeListener(OnDragStateChangeListener listener){
        this.listener=listener;
    }
    public interface OnDragStateChangeListener{
        /**
         * 打开的回调
         */
        void onOpen();
        /**
         * 关闭的回调
         */
        void onClose();
        /**
         * 拖拽中的回调
         */
        void onDraging(float fraction);
    }

}
