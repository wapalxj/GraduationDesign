package com.muguihai.beta1.utils;

import android.os.Handler;

/**
 * Created by vero on 2016/3/18.
 */
public class ThreadUtils {
    public static Handler mHandler=new Handler();


    //子线程
    public static  void runInThread(Runnable task){
        new Thread(task).start();
    }

    //主线程
    public static  void runInUIThread(Runnable task){
        mHandler.post(task);
    }
}
