package com.muguihai.rc1.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.muguihai.rc1.R;
import com.muguihai.rc1.service.XMPPService;
import com.muguihai.rc1.utils.ThreadUtils;
import com.muguihai.rc1.utils.ToastUtils;

import org.jivesoftware.smack.XMPPException;

public class PasswordChangeActivity extends AppCompatActivity {
    private TextView password_change_back;
    private EditText et_newP;
    private EditText et_newP2;
    private Button password_btn_sure;

    private  String p;
    private  String p2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);
        initView();
    }

    private void initView(){
        password_change_back= (TextView) findViewById(R.id.password_change_back);
        et_newP= (EditText) findViewById(R.id.et_newP);
        et_newP2= (EditText) findViewById(R.id.et_newP2);
        password_btn_sure= (Button) findViewById(R.id.password_btn_sure);

        password_btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p = et_newP.getText().toString();
                p2 = et_newP2.getText().toString();
                //判断用户名
                if (TextUtils.isEmpty(p)) {
                    et_newP.setError("密码不能为空");
                    return;
                } else if (p.length()<6){
                    et_newP.setError("密码不能小于6位！");
                }else if (!p.equals(p2)) {
                    et_newP2.setError("两次密码不一致");
                    return;
                } else {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            if(XMPPService.checkConnection()){
                                try {
                                    XMPPService.conn.getAccountManager().changePassword(p2);
                                    ToastUtils.myToast(getApplicationContext(),"密码修改成功");
                                    finish();
                                } catch (XMPPException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                    ToastUtils.myToast(getApplicationContext(),"网络连接失败，请检查网络！");
                                    return;
                            }

                        }
                    });
                }
            }

        });

        password_change_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
