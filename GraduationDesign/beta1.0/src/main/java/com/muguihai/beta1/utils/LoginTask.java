package com.muguihai.beta1.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.muguihai.beta1.activity.LoginActivity;
import com.muguihai.beta1.activity.SlideActivity;
import com.muguihai.beta1.service.PacketService;
import com.muguihai.beta1.service.XMPPService;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;


/**
 * Created by vero on 2016/4/11.
 */
public class LoginTask extends AsyncTask<String, Integer, Integer> {
    public static final int LOGIN_SECCESS = 0;// 成功
    public static final int SOME_ERROR = 1;// 未知错误
    public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// 账号或者密码错误
    public static final int SERVER_UNAVAILABLE = 4;// 无法连接到服务器
    public static final int LOGIN_ERROR = 5;// 连接失败



    private ProgressDialog pd;
    private Context context;
    private Activity activity;
    private String username;
    private String password;
    private XMPPConnection conn;

    public LoginTask(LoginActivity activity, String username,String password) {
        this.activity=activity;
        this.context=activity.getApplicationContext();
        this.username=username;
        this.password=password;
        this.pd=activity.getProgressDialog();
        conn=XMPPUtil.getXMPPConnection(LoginActivity.SERVER, LoginActivity.PORT);
    }

    @Override
    protected void onPreExecute() {
        pd.setTitle("请稍等");
        pd.setMessage("正在登录...");
        pd.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return login(conn);
    }

    //处理子线程返回的结果
    @Override
    protected void onPostExecute(Integer result) {
        pd.dismiss();
        switch (result) {
            case LOGIN_SECCESS: // 登录成功
                ToastUtils.myToast(context, "登陆成功");
                //跳到主界面
                jeepToMain();
                break;
            case LOGIN_ERROR_ACCOUNT_PASS:// 账户或者密码错误
                ToastUtils.myToast(context, "账户或者密码错误");
                break;
            case SERVER_UNAVAILABLE:// 服务器连接失败
                ToastUtils.myToast(context, "服务器连接失败");
                break;
            case SOME_ERROR:// 未知异常
                ToastUtils.myToast(context, "未知错误");
                break;
        }
        super.onPostExecute(result);

    }

    // 登录
    private Integer login(XMPPConnection conn) {
        //连接
        try {
            conn.connect();
            //登录
            conn.login(username, password);
            //保存连接对象
            XMPPService.conn=conn;
            //保存当前登录账户
            username = username + "@" + LoginActivity.SERVICENAME;
            XMPPService.current_account=username;//user@vero
            //启动IMService
            Intent intent =new Intent(this.context,XMPPService.class);
            this.activity.startService(intent);

            //启动pushService
            Intent intent2 =new Intent(this.context, PacketService.class);
            this.activity.startService(intent2);

            return LOGIN_SECCESS;

        } catch (XMPPException e) {
            e.printStackTrace();
            XMPPError error = e.getXMPPError();
            String err = null;
            Log.i("error", "" + error.getCode());
            switch (error.getCode()) {
                case 401:
                    err = "账号或者密码不正确";
                    return LOGIN_ERROR_ACCOUNT_PASS;
                case 502:
                case 504:
                case 404:
                    err = "服务器连接失败，请检查网络连接";
                    return SERVER_UNAVAILABLE;
                case 408:
                    err = "请求超时";
                    return LOGIN_ERROR;
                default:
                    return SOME_ERROR;//未知错误
            }
        }
    }
    public void jeepToMain(){
        Intent intent=new Intent(this.context,SlideActivity.class);
        this.activity.startActivity(intent);
    }

}