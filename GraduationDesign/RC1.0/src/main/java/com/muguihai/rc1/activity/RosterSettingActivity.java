package com.muguihai.rc1.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.muguihai.rc1.R;
import com.muguihai.rc1.dbhelper.ContactOpenHelper;
import com.muguihai.rc1.dbhelper.SessionOpenHelper;
import com.muguihai.rc1.dbhelper.SmsOpenHelper;
import com.muguihai.rc1.provider.ContactsProvider;
import com.muguihai.rc1.provider.SessionProvider;
import com.muguihai.rc1.provider.SmsProvider;
import com.muguihai.rc1.service.XMPPService;
import com.muguihai.rc1.utils.ToastUtils;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

public class RosterSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView btn_back;
    private Button btn_delete_entry;
    private Button btn_change_group;

    private String account;
    private String nickname;
    private String signature;
    private String gender;
    private String tel;
    private String addr;
    private String email;

    private TextView tv_chat_setting_account;
    private TextView tv_chat_setting_nickname;
    private TextView tv_chat_setting_signature;
    private TextView tv_chat_setting_gender;
    private TextView tv_chat_setting_tel;
    private TextView tv_chat_setting_addr;
    private TextView tv_chat_setting_email;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster_setting);
        initData();
        initView();
    }

    private void initData(){
        account = getIntent().getStringExtra(ChatActivity.CHAT_ACCOUNT);
        Cursor cursor = getContentResolver().query(ContactsProvider.URI_CONTACT, null,
                "account = ? and belong_to= ? ",
                new String[]{account,XMPPService.current_account}, null);
        cursor.moveToFirst();
        signature=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.SIGNATURE));
        nickname=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
        gender=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.GENDER));
        tel=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.TEL));
        addr=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.ADDR));
        email=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.EMAIL));
        cursor.close();
    }
    private void initView(){
        btn_back= (TextView) findViewById(R.id.chat_setting_back);
        btn_delete_entry= (Button) findViewById(R.id.btn_delete_entry);
        btn_change_group= (Button) findViewById(R.id.btn_change_group);
        btn_change_group.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_delete_entry.setOnClickListener(this);

        tv_chat_setting_account= (TextView) findViewById(R.id.tv_chat_setting_account);
        tv_chat_setting_nickname= (TextView) findViewById(R.id.tv_chat_setting_nickname);
        tv_chat_setting_signature= (TextView) findViewById(R.id.tv_chat_setting_signature);
        tv_chat_setting_gender= (TextView) findViewById(R.id.tv_chat_setting_gender);
        tv_chat_setting_tel= (TextView) findViewById(R.id.tv_chat_setting_tel);
        tv_chat_setting_addr= (TextView) findViewById(R.id.tv_chat_setting_addr);
        tv_chat_setting_email= (TextView) findViewById(R.id.tv_chat_setting_email);


        tv_chat_setting_account.setText(account);
        tv_chat_setting_nickname.setText(nickname==null?"保密":nickname);
        tv_chat_setting_signature.setText(signature==null?"保密":signature);
        tv_chat_setting_gender.setText(gender==null?"保密":gender);
        tv_chat_setting_tel.setText(tel==null?"保密":tel);
        tv_chat_setting_addr.setText(addr==null?"保密":addr);
        tv_chat_setting_email.setText(email==null?"保密":email);
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
                                        if (XMPPService.checkConnection()){
                                            RosterEntry entry=XMPPService.conn.getRoster().getEntry(account);
                                            XMPPService.conn.getRoster().removeEntry(entry);

                                            //删除联系人
                                            getContentResolver().delete(ContactsProvider.URI_CONTACT,
                                                    ContactOpenHelper.ContactTable.ACCOUNT + "=? and "+ContactOpenHelper.ContactTable.BELONG_TO+ "=?"
                                                    , new String[]{account,XMPPService.current_account});

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
                                        }else {
                                            ToastUtils.myToast(getApplicationContext(),"网络连接失败，请检查网络！");
                                            return;
                                        }
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
