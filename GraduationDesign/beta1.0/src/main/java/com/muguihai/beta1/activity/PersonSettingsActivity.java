package com.muguihai.beta1.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.ToastUtils;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

public class PersonSettingsActivity extends AppCompatActivity {

    private TextView password_setting_back;
    private EditText et_newNickname;
    private Button nickname_btn_sure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_settings);
        initView();
    }

    private void initView(){
        password_setting_back= (TextView) findViewById(R.id.password_setting_back);
        et_newNickname= (EditText) findViewById(R.id.et_newNickname);
        nickname_btn_sure= (Button) findViewById(R.id.nickname_btn_sure);

        nickname_btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (XMPPService.checkConnection()){
                    try {
                        VCard vCard=new VCard();
                        vCard.load(XMPPService.conn);
                        if (TextUtils.isEmpty(et_newNickname.getText())){
                            et_newNickname.setError("请输入内容");
                        }else {
                            vCard.setNickName(et_newNickname.getText().toString());
                            vCard.save(XMPPService.conn);
                            ToastUtils.myToast(getApplicationContext(),"修改成功");
                            finish();
                        }
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }

                }else {
                    ToastUtils.myToast(getApplicationContext(),"网络连接失败");
                }

            }
        });

        password_setting_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
