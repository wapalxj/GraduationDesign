package com.muguihai.rc1.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.muguihai.rc1.R;
import com.muguihai.rc1.service.XMPPService;
import com.muguihai.rc1.utils.ToastUtils;

import org.jivesoftware.smack.packet.Presence;

public class AddFriendActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_add_back;
    private TextView tv_add_account;
    private Button btn_add_sure;
    private Button btn_add_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        initView();


    }

    private void initView(){
        tv_add_back= (TextView) findViewById(R.id.addF_toolbar_back);
        btn_add_sure= (Button) findViewById(R.id.btn_addFsure);
        btn_add_cancel= (Button) findViewById(R.id.btn_addFcancel);
        tv_add_account= (TextView) findViewById(R.id.add_acount);
        btn_add_sure.setOnClickListener(this);
        btn_add_cancel.setOnClickListener(this);
        tv_add_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_addFsure:
                String account =tv_add_account.getText().toString();
                if (TextUtils.isEmpty(account)) {
                    tv_add_account.setError("添加的名称不能为空");
                    return;
                }else {
                    account=account+"@"+ LoginActivity.SERVICENAME;
                    if (XMPPService.checkConnection()){
                        if (XMPPService.conn.getRoster().contains(account)){
                            tv_add_account.setError("不能重复添加！");
                        }else {
                            Presence subscription = new Presence(Presence.Type.subscribe);
                            subscription.setTo(account);
                            XMPPService.conn.sendPacket(subscription);
                            ToastUtils.myToast(getApplicationContext(),"添加成功，请等待回复");
                            tv_add_account.setText(null);
                            finish();
                        }
                    }else {
                        tv_add_account.setError("网络连接失败，请检查网络！");
                        return;
                    }
                }

                break;
            case R.id.btn_addFcancel:
                tv_add_account.setText("");
                break;
            case R.id.addF_toolbar_back:
                finish();
                break;
        }
    }



}
