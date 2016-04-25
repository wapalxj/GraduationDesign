package mobile.android.aspark;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.XMPPConnection;

import mobile.android.aspark.R.id;
import mobile.android.aspark.R.layout;
import mobile.android.aspark.common.Const;
import mobile.android.aspark.common.XMPPUtil;
import mobile.android.aspark.data.DataWarehouse;
import mobile.android.aspark.data.LoginData;
import mobile.android.aspark.utils.Storage;

public class LoginActivity extends ParentActivity implements
        OnClickListener, Const
{
    private Button mButtonLogin;
    private EditText mEditTextUsername;
    private EditText mEditTextPassword;
    private EditText mEditTextServer;
    private CheckBox mCheckBoxSavePassword;
    private CheckBox mCheckBoxAutoLogin;
    private LoginData mLoginData;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_aspark_login);
        mButtonLogin = (Button) findViewById(id.button_login);
        mEditTextUsername = (EditText) findViewById(id.edittext_user);
        mEditTextPassword = (EditText) findViewById(id.edittext_password);
        mEditTextServer = (EditText) findViewById(id.edittext_server);
        mCheckBoxAutoLogin = (CheckBox) findViewById(id.checkbox_auto_login);
        mCheckBoxSavePassword = (CheckBox) findViewById(id.checkbox_save_password);
        mButtonLogin.setOnClickListener(this);

        mLoginData = DataWarehouse.getGlobalData(this).loginData;

        mLoginData.username = Storage.getString(this, KEY_USERNAME);
        mLoginData.password = Storage.getString(this, KEY_PASSWORD);
        mLoginData.loginServer = Storage.getString(this, KEY_LOGIN_SERVER);
        mLoginData.isAutoLogin = Storage.getBoolean(this, KEY_AUTO_LOGIN);
        mLoginData.isSavePassword = Storage.getBoolean(this, KEY_SAVE_PASSWORD);
        mEditTextUsername.setText(mLoginData.username);
        mEditTextPassword.setText(mLoginData.password);
        mEditTextServer.setText(mLoginData.loginServer);

        mCheckBoxAutoLogin.setChecked(mLoginData.isAutoLogin);
        mCheckBoxSavePassword.setChecked(mLoginData.isSavePassword);

        if (mLoginData.isAutoLogin)
        {
            onClick_Login(null);
        }

        if (!mLoginData.isSavePassword)
        {
            Storage.putString(this, KEY_PASSWORD, "");
            mEditTextPassword.setText("");
        }
    }

    private boolean login()
    {

        boolean hasErrors = false;
        String errorMessage = null;

        if (!hasErrors)
        {

            try
            {


                XMPPConnection connection = XMPPUtil.getXMPPConnection(mLoginData.loginServer);

                if (connection == null)
                {
                    throw new Exception("连接服务器失败.");
                }
                connection.login(mLoginData.username, mLoginData.password);


                DataWarehouse.setXMPPConnection(this, connection);
                DataWarehouse.setServiceName(this, connection.getServiceName());


            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;

            }

        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.aspark_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }


    public void onClick(View view)
    {

        switch (view.getId())
        {
            case id.button_login:
                onClick_Login(view);
                break;

            default:
                break;
        }

    }

    public void onClick_Login(View view)
    {

        mLoginData.username = mEditTextUsername.getText().toString();
        mLoginData.password = mEditTextPassword.getText().toString();
        mLoginData.loginServer = mEditTextServer.getText().toString();
        mLoginData.isSavePassword = mCheckBoxSavePassword.isChecked();
        mLoginData.isAutoLogin = mCheckBoxAutoLogin.isChecked();

        Storage.putString(this, KEY_USERNAME, mLoginData.username);
        Storage.putString(this, KEY_PASSWORD, mLoginData.password);
        Storage.putString(this, KEY_LOGIN_SERVER, mLoginData.loginServer);
        Storage.putBoolean(this, KEY_AUTO_LOGIN, mLoginData.isAutoLogin);
        Storage.putBoolean(this, KEY_SAVE_PASSWORD, mLoginData.isSavePassword);

        // Toast.makeText(getActivity(), "ok", Toast.LENGTH_LONG).show();
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {

                if (login())
                {
                    Intent intent = new Intent(LoginActivity.this,
                            MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    mHandler.post(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            Toast.makeText(LoginActivity.this,
                                    "登录失败，请检查用户名、密码和服务器IP的正确性！",
                                    Toast.LENGTH_LONG).show();

                        }
                    });

                }

            }
        }).start();


    }

    public void onClick_Register(View view)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        String server = mEditTextServer.getText().toString().trim();
        if (!"".equals(server))
            intent.putExtra("server", server);
        startActivity(intent);
    }

}
