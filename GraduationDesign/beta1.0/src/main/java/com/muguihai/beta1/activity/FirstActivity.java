package com.muguihai.beta1.activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.muguihai.beta1.R;
import com.muguihai.beta1.utils.ThreadUtils;

public class FirstActivity extends AppCompatActivity {
    private Intent loginActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
//                    SystemClock.sleep(2000);
                    loginActivity=new Intent(FirstActivity.this,LoginActivity.class);
                    startActivity(loginActivity);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
