package com.muguihai.beta1.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.muguihai.beta1.R;

public class SlideSettingActivity extends AppCompatActivity {
    private TextView tv_change_mine;
    private TextView tv_change_password;
    private TextView slide_setting_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_setting);
        initView();
    }

    private void initView(){
        tv_change_mine= (TextView) findViewById(R.id.tv_change_mine);
        tv_change_password= (TextView) findViewById(R.id.tv_change_password);
        slide_setting_back= (TextView) findViewById(R.id.slide_setting_back);

        tv_change_mine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent person_settings=new Intent(SlideSettingActivity.this,PersonSettingsActivity.class);
                startActivity(person_settings);
            }
        });

        tv_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent password_settings=new Intent(SlideSettingActivity.this,PasswordChangeActivity.class);
                startActivity(password_settings);
            }
        });

        slide_setting_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
