package com.muguihai.beta1.view.slidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 当slideMenu打开的时候，拦截并消费掉触摸事件
 * Created by vero on 2016/4/7.
 */
public class MyLinearLayout extends LinearLayout{
    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    private SlideMenu slideMenu;

    public void setSlideMenu(SlideMenu slideMenu){
        this.slideMenu=slideMenu;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (slideMenu!=null &&slideMenu.getCurrentState()== SlideMenu.DragState.Open){
            //如果slidemenu打开则拦截并消费掉事件
            return true;

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (slideMenu!=null &&slideMenu.getCurrentState()== SlideMenu.DragState.Open) {
            //如果slidemenu打开则拦截并消费掉事件
            if (event.getAction()==MotionEvent.ACTION_UP){
                //如果slidemenu打开，点击main则关闭如果slidemenu打开
                slideMenu.closeMenu();
            }
            return true;
        }
            return super.onTouchEvent(event);
    }
}
