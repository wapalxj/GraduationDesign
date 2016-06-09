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
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import service.IMService;
import service.PushService;
import utils.ThreadUtils;
import utils.ToastUtils;

public class LoginActivity extends AppCompatActivity {
    private static final String SERVER ="113.55.72.120";//IP
    private static final int PORT=5222;//port
    public static final String SERVICENAME	= "vero";
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
        ConnectionConfiguration configuration =new ConnectionConfiguration(SERVER,PORT);
        //额外的配置:上线则改回来
        configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//明文传输
        configuration.setDebuggerEnabled(true);//调试模式，方便查看具体内容
        configuration.setSASLAuthenticationEnabled(false);

        //
        // 允许自动连接
        configuration.setReconnectionAllowed(false);
        // 允许登陆成功后更新在线状态
        configuration.setSendPresence(true);
        // 收到好友邀请后manual表示需要经过同意,accept_all表示不经同意自动为好友
//        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
        //

        //创建连接对象
        XMPPConnection conn=new XMPPConnection(configuration);
        //连接
        try {
            conn.connect();
            //登录
            conn.login(username, password);
            ToastUtils.myToast(getApplicationContext(), "登录成功");


            //保存连接对象
            IMService.conn=conn;
            //保存当前登录账户
            username=username+"@"+LoginActivity.SERVICENAME;
            IMService.current_account=username;//user@vero
            //启动IMService
            Intent intent =new Intent(getApplicationContext(),IMService.class);
            startService(intent);

            //启动pushService
            Intent intent2 =new Intent(getApplicationContext(), PushService.class);
            startService(intent2);

            //跳到主界面
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
                case 502 :
                case 504 :
                case 404 :
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
        finish();
    }

}
