package com.muguihai.beta1.view.swipe_delete;

/**
 * 单例模式来管理swipe能否打开
 * Created by vero on 2016/4/8.
 */
public class SwipeLayoutManager {
    private SwipeLayoutManager(){}
    private static SwipeLayoutManager mInstance=new SwipeLayoutManager();

    public static SwipeLayoutManager getInstance(){
        return mInstance;
    }

    private SwipeLayout currentLayout;//记录当前打开的layout

    public void setSwipeLayout(SwipeLayout layout){
        this.currentLayout=layout;
    }

    //判断能不能滑动
    public boolean canBeSwiped(SwipeLayout swipeLayout){
        if (currentLayout==null){
            //当前没有打开的layout
            return true;
        }else {
            //打开的是当前的layout则能滑动，否则不能滑动
            return swipeLayout==currentLayout;
        }
    }

    //关闭当前已经打开的swipelayout
    public void closeCurrentLayout(){
        if (currentLayout!=null){
            currentLayout.closeDelete();
        }
    }

    //关闭后设置为null
    public void clearCurrentLayout(){
            currentLayout=null;
    }
}
