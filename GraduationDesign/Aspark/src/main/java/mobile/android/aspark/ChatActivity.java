package mobile.android.aspark;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

import mobile.android.aspark.adapter.ChatListAdapter;
import mobile.android.aspark.common.Const;
import mobile.android.aspark.common.Util;
import mobile.android.aspark.data.ChatData;
import mobile.android.aspark.data.DataWarehouse;
import mobile.android.aspark.data.LoginData;


public class ChatActivity extends ParentActivity implements PacketListener, Const
{
    private String mUser;               //  账号
    private String mName;               //  别名
    private String mServiceName;        //  服务名，聊天需要账号@服务名

    private ChatListAdapter mChatListAdapter;


    private LoginData mLoginData;

    private EditText mEditTextChatText;
    private ListView mListViewChatList;

    private PacketFilter mFilter = new MessageTypeFilter(Message.Type.chat);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        mLoginData = DataWarehouse.getGlobalData(this).loginData;
        mUser = getIntent().getStringExtra("user");
        mName = getIntent().getStringExtra("name");

        mServiceName = mXMPPConnection.getServiceName();
        mEditTextChatText = (EditText) findViewById(R.id.edittext_chat_text);
        mListViewChatList = (ListView) findViewById(R.id.listview_ChatList);



        mXMPPConnection.addPacketListener(this, mFilter);

        //http://www.4byte.cn/question/71362/delivery-receipt-requests-not-working-for-xmpp-android-asmack.html
        //http://stackoverflow.com/questions/23191896/how-to-implement-message-listener-in-xmpp-asmack
        mChatListAdapter = new ChatListAdapter(this);

        mListViewChatList.setAdapter(mChatListAdapter);
        mListViewChatList.setDivider(null);

        String body = getIntent().getStringExtra("body");
        if(body != null)
        {
            ChatData item = new ChatData();

            item.text = body;
            item.user = mUser;

            item.name = mName;

            mChatListAdapter.addItem(item);
        }
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

    public void onClick_Face(View view)
    {
        Intent intent = new Intent(this, FaceActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (resultCode)
        {
            case 1:
                int faceId = data.getIntExtra(KEY_FACE_ID, -1);
                if (faceId != -1)
                {
                    String faceResName = FACE_PREFIX + faceId;

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                            Util.getResourceIdFromName(R.drawable.class, faceResName));

                    Matrix matrix = new Matrix();
                    matrix.postScale(0.6f, 0.6f);
                    Bitmap smallBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                            matrix, true);
                    ImageSpan imageSpan = new ImageSpan(this, smallBitmap);
                    String faceText = FACE_TEXT_PREFIX + faceId + FACE_TEXT_SUFFIX;
                    SpannableString spannableString = new SpannableString(faceText);

                    spannableString.setSpan(imageSpan, 0, faceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mEditTextChatText.getText().insert(mEditTextChatText.getSelectionStart(), spannableString);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mXMPPConnection.removePacketListener(this);
        DataWarehouse.getGlobalData(this).chatUsers.remove(mUser);
    }

    public void onClick_Send(View view)
    {
        try
        {
            String text = mEditTextChatText.getText().toString().trim();
            if (!"".equals(text))
            {

                Message msg = new Message(mUser + "@" + mServiceName, Message.Type.chat);
                msg.setBody(text);
                DeliveryReceiptManager.addDeliveryReceiptRequest(msg);
                mXMPPConnection.sendPacket(msg);
                mEditTextChatText.setText("");

                ChatData item = new ChatData();
                item.text = text;
                item.name = mLoginData.username;
                item.user = mLoginData.username;
                item.isOwner = true;
                mChatListAdapter.addItem(item);
              //  mListViewChatList.setSelection(mListViewChatList.getAdapter().getCount() - 1);
            }
            else
            {
                Toast.makeText(this, "请输入要发送的文本.", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            super.handleMessage(msg);
            Message message = (Message) msg.obj;
            if(Util.extractUserFromChat(message.getFrom()).equals(mUser)) {
                String body = message.getBody();
                ChatData item = new ChatData();

                item.text = body;
                item.user = mUser;

                item.name = mName;

                mChatListAdapter.addItem(item);
                mListViewChatList.setSelection(mListViewChatList.getAdapter().getCount() - 1);
            }
        }
    };

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException
    {
        android.os.Message msg = new android.os.Message();
        msg.obj = packet;

        mHandler.sendMessage(msg);

    }
}
