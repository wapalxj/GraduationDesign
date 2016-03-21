package com.muguihai.rc1.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by vero on 2016/3/18.
 */
public class ToastUtils {
    public static void myToast(final Context context,final String text){
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
