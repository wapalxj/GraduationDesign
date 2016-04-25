package mobile.android.aspark;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.util.List;

import mobile.android.aspark.adapter.MultiUserChatRoomListAdapter;
import mobile.android.aspark.common.Const;


public class MultiUserChatRoomsActivity extends ParentActivity implements Const, AdapterView.OnItemClickListener
{
    private ListView mListViewChatRooms;
    private MultiUserChatRoomListAdapter mMultiUserChatRoomListAdapter;
    private ServiceDiscoveryManager mServiceDiscoveryManager;
    private String mChatServiceJID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_chat_rooms);
        mListViewChatRooms = (ListView) findViewById(R.id.listview_chat_rooms);
        mChatServiceJID = getIntent().getStringExtra(Const.JID);
        setTitle(mChatServiceJID);
        mServiceDiscoveryManager = ServiceDiscoveryManager
                .getInstanceFor(mXMPPConnection);

        try
        {

            mMultiUserChatRoomListAdapter = new MultiUserChatRoomListAdapter(this,mServiceDiscoveryManager.discoverItems(mChatServiceJID).getItems());
            mListViewChatRooms.setAdapter(mMultiUserChatRoomListAdapter);
            mListViewChatRooms.setOnItemClickListener(this);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //  新建聊天室
    public void onClick_New_Room(View view)
    {

        final EditText editText = new EditText(this);

        new AlertDialog.Builder(this).setTitle("请输入聊天室名称").setView(
                editText).setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String roomName = editText.getText().toString().trim();
                if ("".equals(roomName))
                {
                    Toast.makeText(MultiUserChatRoomsActivity.this, "聊天室名称不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                MultiUserChat muc = new MultiUserChat(mXMPPConnection, roomName + "@" + mChatServiceJID);

                //创建聊天室
                try
                {
                    muc.create(roomName);
                    Form form = muc.getConfigurationForm();
                    Form submitForm = form.createAnswerForm();
                    List<FormField> fields = form.getFields();
                    for (FormField field : fields)
                    {
                        if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null)
                        {
                            submitForm.setDefaultAnswer(field.getVariable());
                        }
                    }
                    // 这里设置聊天室为公共聊天室
                    submitForm.setAnswer("muc#roomconfig_publicroom", true);
                    // 这里设置聊天室是永久存在的
                    submitForm.setAnswer("muc#roomconfig_persistentroom", true);
                    muc.sendConfigurationForm(submitForm);


                    mMultiUserChatRoomListAdapter.updateChatRooms(mServiceDiscoveryManager.discoverItems(mChatServiceJID).getItems());
                    Toast.makeText(MultiUserChatRoomsActivity.this, "成功创建聊天室", Toast.LENGTH_LONG).show();

                }
                catch (Exception e)
                {
                    Toast.makeText(MultiUserChatRoomsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        })
                .setNegativeButton("取消", null).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_item_chat_rom_refresh)
        {
            try
            {
                mMultiUserChatRoomListAdapter.updateChatRooms(mServiceDiscoveryManager.discoverItems(mChatServiceJID).getItems());
                Toast.makeText(this, "成功刷新", Toast.LENGTH_LONG).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        DiscoverItems.Item item = mMultiUserChatRoomListAdapter.getChatRoom(position);
        Intent intent = new Intent(this, MultiUserChatRoomActivity.class);
        intent.putExtra(JID, mChatServiceJID);
        intent.putExtra(CHAT_ROOM_NAME, item.getName());
        startActivity(intent);

    }
}
