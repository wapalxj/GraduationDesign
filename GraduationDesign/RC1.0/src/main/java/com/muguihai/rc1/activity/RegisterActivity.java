package com.muguihai.rc1.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.muguihai.rc1.R;
import com.muguihai.rc1.utils.ThreadUtils;
import com.muguihai.rc1.utils.ToastUtils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.XMPPError;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtPassword2;
    private Button mBtnRegister;
    private Button mBtnBack;
    private String username;
    private String password;
    private String password2;
    private XMPPConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    public void init(){
        mEtUsername= (EditText) findViewById(R.id.et_username);
        mEtPassword= (EditText) findViewById(R.id.et_password);
        mEtPassword2= (EditText) findViewById(R.id.et_password2);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mBtnBack= (Button) findViewById(R.id.btn_back);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = mEtUsername.getText().toString();
                password = mEtPassword.getText().toString();
                password2 = mEtPassword2.getText().toString();
                //判断用户名
                if (TextUtils.isEmpty(username)) {
                    mEtUsername.setError("用户名不能为空");
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    mEtPassword.setError("密码不能为空");
                    return;
                } else if (TextUtils.isEmpty(password2)) {
                    mEtPassword2.setError("密码不能为空");
                    return;
                } else if (!password.equals(password2)) {
                    ToastUtils.myToast(getApplicationContext(), "2次密码不一致");
                    return;
                } else {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            createConnection();
                        }
                    });
                    finish();
                }
            }
        });
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conn!=null&&conn.isConnected()){
                    conn.disconnect();
                }
                finish();
            }
        });
    }

    public void register(XMPPConnection conn) {
        Registration reg = new Registration();
        reg.setType(IQ.Type.SET);
        reg.setTo(conn.getServiceName());
        reg.setUsername(username);
        reg.setPassword(password);
        reg.addAttribute("android", "geolo_createUser_android");
        System.out.println("reg:" + reg);
        PacketFilter filter = new AndFilter(new PacketIDFilter(reg
                .getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector = conn.createPacketCollector(filter);
        conn.sendPacket(reg);

        IQ result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();// 停止请求results（是否成功的结果）
        if (result == null) {
            ToastUtils.myToast(getApplicationContext(), "服务器没有返回结果");
        } else if (result.getType() == IQ.Type.ERROR) {
            if (result.getError().toString().equalsIgnoreCase(
                    "conflict(409)")) {
                ToastUtils.myToast(getApplicationContext(), "这个账号已经存在");
            } else {
                ToastUtils.myToast(getApplicationContext(), "注册失败");
            }
        } else if (result.getType() == IQ.Type.RESULT) {
            ToastUtils.myToast(getApplicationContext(), "恭喜你注册成功");
        }

    }
    public void createConnection(){
        //创建配置
        ConnectionConfiguration config =new ConnectionConfiguration(LoginActivity.SERVER,LoginActivity.PORT);
        //额外的配置:上线则改回来
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//明文传输
        config.setDebuggerEnabled(true);//调试模式，方便查看具体内容
        config.setSASLAuthenticationEnabled(false);
        //创建连接对象
        conn = new XMPPConnection(config);
        //连接
        try {
            conn.connect();
            register(conn);
        } catch (XMPPException e) {
            e.printStackTrace();
            XMPPError error = e.getXMPPError();
            String err=null;
            Log.i("error", "" + error.getCode());
            switch (error.getCode()){
                case 502:
                    err="服务器连接失败，请检查网络连接";
                    break;
                case 408:
                    err="请求超时";
                    break;
            }
            ToastUtils.myToast(getApplicationContext(), "注册失败:"+err);
        }
    }


}

