package com.muguihai.beta1.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.SessionOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.SessionProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.PinyinUtil;
import com.muguihai.beta1.utils.ToastUtils;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.xml.sax.ContentHandler;

public class RosterSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView btn_back;
    private Button btn_delete_entry;
    private Button btn_change_group;

    private String account;
    private String nickname;
    private String signature;

    private TextView tv_chat_setting_account;
    private TextView tv_chat_setting_nickname;
    private TextView tv_chat_setting_signature;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster_setting);
        initData();
        initView();
    }

    private void initData(){
        account = getIntent().getStringExtra(ChatActivity.CHAT_ACCOUNT);
        nickname = getIntent().getStringExtra(ChatActivity.CHAT_NICKNAME);
        signature = getIntent().getStringExtra(ChatActivity.CHAT_SIGNATURE);
    }
    private void initView(){
        btn_back= (TextView) findViewById(R.id.chat_setting_back);
        btn_delete_entry= (Button) findViewById(R.id.btn_delete_entry);
        btn_change_group= (Button) findViewById(R.id.btn_change_group);
        btn_change_group.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_delete_entry.setOnClickListener(this);

        tv_chat_setting_account= (TextView) findViewById(R.id.chat_setting_account);
        tv_chat_setting_nickname= (TextView) findViewById(R.id.chat_setting_nickname);
        tv_chat_setting_signature= (TextView) findViewById(R.id.chat_setting_signature);

        tv_chat_setting_account.setText(account);
        tv_chat_setting_nickname.setText(nickname);
        tv_chat_setting_signature.setText(signature);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chat_setting_back:
                finish();
                break;
            case R.id.btn_delete_entry:
                if (XMPPService.checkConnection()){
                    ToastUtils.myToast(getApplicationContext(),account);
                    new AlertDialog.Builder(RosterSettingActivity.this)
                            .setMessage("确定删除好友好友"+nickname+"吗？")
                            .setTitle("提示")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        RosterEntry entry=XMPPService.conn.getRoster().getEntry(account);
                                        XMPPService.conn.getRoster().removeEntry(entry);

                                        // 删除聊天记录
                                        getContentResolver().delete(
                                                SmsProvider.URI_SMS,
                                                SmsOpenHelper.SmsTable.SESSION_ACCOUNT+"=? and "+SmsOpenHelper.SmsTable.SESSION_BELONG_TO+ "=?"
                                                ,
                                                new String[]{account,XMPPService.current_account});

                                        // 删除会话
                                        getContentResolver().delete(
                                                SessionProvider.URI_SESSION,
                                                SessionOpenHelper.SessionTable.SESSION_ACCOUNT+"=? and "+SessionOpenHelper.SessionTable.SESSION_BELONG_TO+ "=?"
                                                ,
                                                new String[]{account,XMPPService.current_account});
                                        finish();
                                    } catch (XMPPException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            }).show();
                }
                break;
            case R.id.btn_change_group:
                ToastUtils.myToast(getApplicationContext(),"移动"+nickname);
                Intent changeGroup=new Intent(RosterSettingActivity.this,FriendGroupChangeActivity.class);
                changeGroup.putExtra(ChatActivity.CHAT_ACCOUNT,account);
                changeGroup.putExtra(ChatActivity.CHAT_NICKNAME,nickname);
                startActivity(changeGroup);
                finish();
                break;
            default:
                break;
        }
    }


}
