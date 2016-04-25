package mobile.android.aspark;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import java.util.Set;

import mobile.android.aspark.R.id;
import mobile.android.aspark.R.layout;
import mobile.android.aspark.adapter.FriendListAdapter;
import mobile.android.aspark.common.Const;
import mobile.android.aspark.common.Util;
import mobile.android.aspark.data.DataWarehouse;
import mobile.android.aspark.data.LoginData;
import mobile.android.aspark.data.UserData;
import mobile.android.aspark.utils.Storage;

public class MainActivity extends ParentActivity implements PacketListener, Const, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
    public static FriendListAdapter mFriendListAdapter;

    private PacketFilter mFilter = new MessageTypeFilter(Message.Type.chat);
    private ListView mFriendList;
    private LoginData mLoginData;
    private int mCurrentPosition = -1;
    private Set<String> mChatUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_aspark_main);
        mChatUsers =  DataWarehouse.getGlobalData(this).chatUsers;
        mLoginData = DataWarehouse.getGlobalData(this).loginData;

        //  装载控件
        mFriendList = (ListView) findViewById(id.listview_friends);

        mFriendListAdapter = new FriendListAdapter(this, mXMPPConnection.getRoster().getEntries());
        mFriendList.setAdapter(mFriendListAdapter);

        mFriendList.setOnItemClickListener(this);
        mFriendList.setOnItemLongClickListener(this);
        registerForContextMenu(mFriendList);
        mXMPPConnection.addPacketListener(this, mFilter);
    }
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Message message = (Message) msg.obj;
            String user = Util.extractUserFromChat(message.getFrom());

            if(!mChatUsers.contains(user)) {
                String name = mFriendListAdapter.findName(user);

                String body = message.getBody();
                mChatUsers.add(user);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("name", name);
                intent.putExtra("body", body);
                startActivity(intent);
            }

        }
    };
    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        android.os.Message msg = new android.os.Message();
        msg.obj = packet;

        mHandler.sendMessage(msg);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        Intent intent = null;
        switch (id)
        {
            case R.id.menu_item_remove_friend:
                Toast.makeText(this, String.valueOf(mCurrentPosition), Toast.LENGTH_LONG).show();
                if (mCurrentPosition > -1)
                {
                    mFriendListAdapter.removeUserData(mCurrentPosition);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.aspark_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 1:   //  添加好友
                if (resultCode == 1)
                {
                    String name = data.getStringExtra("name");
                    String user = data.getStringExtra("user");

                    UserData userData = new UserData(name, user);
                    mFriendListAdapter.addUserData(userData);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();
        Intent intent = null;
        switch (id)
        {
            case R.id.add_friend:
                intent = new Intent(this, AddFriendActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.chat_services:
                intent = new Intent(this, ChatServiceActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_item_logout:
                new AlertDialog.Builder(this).setTitle("注销").setMessage("确认要注销当前用户吗？").setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                Storage.putBoolean(MainActivity.this, Const.KEY_AUTO_LOGIN, false);
                                try
                                {
                                    mXMPPConnection.disconnect();
                                }
                                catch (Exception e)
                                {

                                }
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                ).setNegativeButton("取消", null).show();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
        getMenuInflater().inflate(R.menu.friend_list_context_menu, menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent(this, ChatActivity.class);
        String user = mFriendListAdapter.getUser(position);
        String name = mFriendListAdapter.getName(position);

        intent.putExtra("user", user);
        intent.putExtra("name", name);
        Toast.makeText(this, user, Toast.LENGTH_LONG).show();
        mChatUsers.add(user);
        startActivity(intent);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        mCurrentPosition = position;
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mXMPPConnection.removePacketListener(this);
        mFriendListAdapter = null;
    }
}
