package com.muguihai.beta1.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.muguihai.beta1.R;
import com.muguihai.beta1.utils.LoginTask;

/**
 * 登录Activity
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SERVER ="113.55.81.234";//IP
    public static final int PORT=5222;//port
    public static final String SERVICENAME	= "vero";
    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private Button mBtnRegister;
    private String username;
    private String password;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    public void initView(){
        mEtUsername= (EditText) findViewById(R.id.et_username);
        mEtPassword= (EditText) findViewById(R.id.et_password);
        mBtnLogin= (Button) findViewById(R.id.btn_login);
        mBtnRegister= (Button) findViewById(R.id.btn_register);
        mBtnLogin.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
        progressDialog=new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
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
                    //登录
                    login();
                }
                break;
            case R.id.btn_register:
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }

    //登录
    public void login(){
        LoginTask loginTask = new LoginTask(LoginActivity.this, username,password);
        loginTask.execute();
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }


}
