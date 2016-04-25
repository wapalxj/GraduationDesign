package mobile.android.aspark;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.search.UserSearchManager;

import java.util.HashMap;
import java.util.Map;

import mobile.android.aspark.common.XMPPUtil;


public class RegisterActivity extends Activity
{
    private EditText mEditTextUser;
    private EditText mEditTextPassword;
    private EditText mEditTextRepassword;
    private EditText mEditTextServer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEditTextUser = (EditText) findViewById(R.id.edittext_user);
        mEditTextPassword = (EditText) findViewById(R.id.edittext_password);
        mEditTextRepassword = (EditText) findViewById(R.id.edittext_repassword);
        mEditTextServer = (EditText) findViewById(R.id.edittext_server);
        String server = getIntent().getStringExtra("server");
        if (server != null)
        {
            mEditTextServer.setText(server);
        }


    }

    private boolean verify()
    {
        if ("".equals(mEditTextUser.getText().toString().trim()))
        {
            Toast.makeText(this, "请输入用户名.", Toast.LENGTH_LONG).show();
            return false;
        }
        if ("".equals(mEditTextPassword.getText().toString().trim()))
        {
            Toast.makeText(this, "请输入密码.", Toast.LENGTH_LONG).show();
            return false;
        }
        if ("".equals(mEditTextRepassword.getText().toString().trim()))
        {
            Toast.makeText(this, "请再输入一遍密码.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!mEditTextPassword.getText().toString().equals(mEditTextRepassword.getText().toString()))
        {
            Toast.makeText(this, "两次输入的密码不一致.", Toast.LENGTH_LONG).show();
            return false;
        }
        if ("".equals(mEditTextServer.getText().toString().trim()))
        {
            Toast.makeText(this, "请输入服务器地址.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Toast.makeText(RegisterActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
        }
    };

    public void createAccount(String server, String username, String password)

    {
        Message msg = new Message();
        try
        {
            XMPPConnection connection = XMPPUtil.getXMPPConnection(server);
            UserSearchManager userSearchManager = new UserSearchManager(connection);


            Registration reg = new Registration();
            reg.setType(IQ.Type.SET);
            Map<String, String> attributes = new HashMap<String, String>();
            reg.setTo(connection.getHost());

            attributes.put("username", username);
            attributes.put("password", password);
            //attributes.put("name", "name");
            reg.setAttributes(attributes);
            PacketFilter filter = new AndFilter(new PacketIDFilter(reg.getPacketID()),
                    new PacketTypeFilter(IQ.class));
            PacketCollector collector = connection.createPacketCollector(filter);
            connection.sendPacket(reg);
            IQ result = (IQ) collector.nextResult(connection.getPacketReplyTimeout());

            collector.cancel();

            if (result == null)
            {
                throw new Exception("创建用户失败，服务器没有响应.");
            }
            else if (result.getType() == IQ.Type.RESULT)
            {
                msg.obj = "成功创建用户";
                finish();
            }
            else
            {
                if (result.getError().toString().toLowerCase().contains("conflict"))
                {
                    throw new Exception("该用户已经存在，请使用其他用户名.");

                }
                else
                {

                    throw new Exception("创建用户失败.");
                }
            }

        }
        catch (Exception e)
        {

            msg.obj = e.getMessage();


        }
        finally
        {
            mHandler.sendMessage(msg);
        }
    }

    public void onClick_Register(View view)
    {
        if (!verify())
            return;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {




                createAccount(mEditTextServer.getText().toString(),
                           mEditTextUser.getText().toString(),
                           mEditTextPassword.getText().toString());




            }
        }).start();


    }

    public void onClick_Close(View view)
    {
        finish();
    }

}
