package mobile.android.aspark;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mobile.android.aspark.adapter.ChatServiceListAdapter;
import mobile.android.aspark.common.Const;


public class ChatServiceActivity extends ParentActivity implements Const, AdapterView.OnItemClickListener
{

    private ListView mListViewChatServices;
    private ChatServiceListAdapter mChatServiceListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_service);

        mListViewChatServices = (ListView) findViewById(R.id.listview_chat_services);
        mChatServiceListAdapter = new ChatServiceListAdapter(this, getHostRooms());
        mListViewChatServices.setOnItemClickListener(this);
        mListViewChatServices.setAdapter(mChatServiceListAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        String jid = mChatServiceListAdapter.getJID(position);
        Intent intent = new Intent(this, MultiUserChatRoomsActivity.class);
        intent.putExtra(JID,jid);
        startActivity(intent);
    }

    private List<HostedRoom> getHostRooms()
    {
        Collection<HostedRoom> hostrooms;

        List<HostedRoom> chatServices = new ArrayList<HostedRoom>();
        try
        {
            hostrooms = MultiUserChat.getHostedRooms(mXMPPConnection,
                    mXMPPConnection.getServiceName());
            for (HostedRoom entry : hostrooms)
            {

                chatServices.add(entry);

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return chatServices;
    }

}
