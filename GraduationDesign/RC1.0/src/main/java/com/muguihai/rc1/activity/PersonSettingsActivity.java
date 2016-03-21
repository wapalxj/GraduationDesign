package com.muguihai.rc1.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.muguihai.rc1.R;
import com.muguihai.rc1.service.XMPPService;
import com.muguihai.rc1.utils.ToastUtils;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

public class PersonSettingsActivity extends AppCompatActivity {

    private TextView password_setting_back;
    private EditText et_newSign;
    private EditText et_newNickname;
    private EditText et_newGender;
    private EditText et_newTel;
    private EditText et_newAddr;
    private EditText et_newEmail;



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
        et_newSign= (EditText) findViewById(R.id.et_newSign);
        et_newGender= (EditText) findViewById(R.id.et_newGender);
        et_newTel= (EditText) findViewById(R.id.et_newTel);
        et_newAddr= (EditText) findViewById(R.id.et_newAddr);
        et_newEmail= (EditText) findViewById(R.id.et_newEmail);
        nickname_btn_sure= (Button) findViewById(R.id.nickname_btn_sure);

        nickname_btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (XMPPService.checkConnection()){
                    try {
                        VCard vCard=new VCard();
                        vCard.load(XMPPService.conn);
                        vCard.setNickName(et_newNickname.getText().toString());
                        vCard.setField("sign",et_newSign.getText().toString());
                        vCard.setField("gender",et_newGender.getText().toString());
                        vCard.setPhoneHome("tel",et_newTel.getText().toString());
                        vCard.setAddressFieldHome("addr",et_newAddr.getText().toString());
                        vCard.setEmailHome(et_newEmail.getText().toString());

                        vCard.save(XMPPService.conn);
                        //发送修改广播
                        //发送广播
                        Intent vcard=new Intent(SlideActivity.XMPPReceiver.VCARD);
                        vcard.putExtra(SlideActivity.XMPPReceiver.VCARD_SIGN,et_newSign.getText().toString());
                        vcard.putExtra(SlideActivity.XMPPReceiver.VCARD_NICKNAME,et_newNickname.getText().toString());
                        vcard.putExtra(SlideActivity.XMPPReceiver.VCARD_GENDER,et_newGender.getText().toString());
                        vcard.putExtra(SlideActivity.XMPPReceiver.VCARD_TEL,et_newTel.getText().toString());
                        vcard.putExtra(SlideActivity.XMPPReceiver.VCARD_ADDR,et_newAddr.getText().toString());
                        vcard.putExtra(SlideActivity.XMPPReceiver.VCARD_EMAIL,et_newEmail.getText().toString());
                        sendBroadcast(vcard);
                        ToastUtils.myToast(getApplicationContext(),"修改成功");
                        finish();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }else {
                    ToastUtils.myToast(getApplicationContext(),"网络连接失败");
                    return;
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
