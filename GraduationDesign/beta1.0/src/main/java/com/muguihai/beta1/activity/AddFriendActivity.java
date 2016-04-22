package com.muguihai.beta1.activity;

import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.PinyinUtil;
import com.muguihai.beta1.utils.ToastUtils;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.xml.sax.helpers.XMLFilterImpl;

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
                        Presence subscription = new Presence(Presence.Type.subscribe);
                        subscription.setTo(account);
                        XMPPService.conn.sendPacket(subscription);
                    }
                }
                finish();
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
