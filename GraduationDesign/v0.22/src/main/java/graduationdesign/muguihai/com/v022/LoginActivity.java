package graduationdesign.muguihai.com.v022;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import utils.ThreadUtils;
import utils.ToastUtils;

public class LoginActivity extends AppCompatActivity {
    private static final String SERVER ="113.55.45.175";//IP
    private static final int PORT=5222;//port
    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private Button mBtnRegister;
    private String username;
    private String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    public void init(){
        mEtUsername= (EditText) findViewById(R.id.et_username);
        mEtPassword= (EditText) findViewById(R.id.et_password);
        mBtnLogin= (Button) findViewById(R.id.btn_login);
        mBtnRegister= (Button) findViewById(R.id.btn_register);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = mEtUsername.getText().toString();
                password = mEtPassword.getText().toString();
                //判断用户名
                if (TextUtils.isEmpty(username)) {
                    mEtUsername.setError("用户名不能为空");
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    mEtPassword.setError("密码不能为空");
                    return;
                }else {
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            createConnection();
                        }
                    });

                }
            }
        });
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //创建连接
    public void createConnection(){
        //创建配置
        ConnectionConfiguration config =new ConnectionConfiguration(SERVER,PORT);
        //额外的配置:上线则改回来
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//明文传输
        config.setDebuggerEnabled(true);//调试模式，方便查看具体内容
        config.setSASLAuthenticationEnabled(false);
        //创建连接对象
        XMPPConnection conn=new XMPPConnection(config);
        //连接
        try {
            conn.connect();
            //登录
            conn.login(username, password);
            ToastUtils.myToast(getApplicationContext(), "登录成功");
            jeepToMain();
        } catch (XMPPException e) {
            e.printStackTrace();
            XMPPError error = e.getXMPPError();
            String err=null;
            Log.i("error",""+error.getCode());
            switch (error.getCode()){
                case 401:
                    err="账号或者密码不正确";
                    break;
                case 502:
                    err="服务器连接失败，请检查网络连接";
                    break;
                case 408:
                    err="请求超时";
                    break;
            }
            ToastUtils.myToast(getApplicationContext(), "登录失败:"+err);
        }
    }

    public void jeepToMain(){
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
    }

}
