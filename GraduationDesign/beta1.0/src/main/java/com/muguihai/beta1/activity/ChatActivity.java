package com.muguihai.beta1.activity;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.PinyinUtil;
import com.muguihai.beta1.utils.ThreadUtils;
import com.muguihai.beta1.utils.ToastUtils;
import com.muguihai.beta1.view.quickaction.ActionItem;
import com.muguihai.beta1.view.quickaction.QuickAction;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    public static final String CHAT_ACCOUNT ="chat_account";
    public static final String CHAT_NICKNAME ="chat_nickname";
    public static final String CHAT_SIGNATURE ="chat_signature";

    private String chat_account;//聊天的对象
    private String chat_nickname;
    private String chat_signature;

    private TextView chat_back;
    private TextView chat_title;
    private ListView mChat_listView;
    private EditText editText;
    private Button btn_send;
    private CursorAdapter mAdapter;

    private XMPPService xmppService;
    private MyServiceConnection conn;

    private TextView chat_roster_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        initView();
        initData();
        initListener();
    }

    private void init() {
        registerContentProvider();//注册监听

        //启动服务
        conn=new MyServiceConnection();
        Intent intent=new Intent(ChatActivity.this,XMPPService.class);
        bindService(intent,conn,BIND_AUTO_CREATE);


        chat_account = getIntent().getStringExtra(ChatActivity.CHAT_ACCOUNT);
        chat_nickname = getIntent().getStringExtra(ChatActivity.CHAT_NICKNAME);
        chat_signature = getIntent().getStringExtra(ChatActivity.CHAT_SIGNATURE);

        chat_back= (TextView) findViewById(R.id.chat_back);
        chat_title= (TextView) findViewById(R.id.chat_title);
        mChat_listView= (ListView) findViewById(R.id.chat_listview);
        editText= (EditText) findViewById(R.id.edittext);
        btn_send= (Button) findViewById(R.id.btn_send);
        chat_roster_setting= (TextView) findViewById(R.id.chat_roster_setting);

        chat_roster_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setting=new Intent(ChatActivity.this,RosterSettingActivity.class);
                setting.putExtra(ChatActivity.CHAT_ACCOUNT,chat_account);
                setting.putExtra(ChatActivity.CHAT_NICKNAME,chat_nickname);
                setting.putExtra(ChatActivity.CHAT_SIGNATURE,chat_signature);
                startActivity(setting);
                finish();
            }
        });

        chat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_click();
            }
        });
    }

    private void initView() {
        chat_title.setText(chat_nickname);

    }

    private void initData() {
        setOrNotifyAdapter();
    }

    private void setOrNotifyAdapter() {
        if (mAdapter!=null){
            Cursor c=mAdapter.getCursor();
            c.requery();
            mChat_listView.setSelection(c.getCount() - 1);
            return;
        }

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = getContentResolver().query(SmsProvider.URI_SMS, null,
                        "(from_account = ? and to_account = ?)or(from_account = ? and to_account = ?)",
                        new String[]{XMPPService.current_account,chat_account,chat_account,XMPPService.current_account},
                        SmsOpenHelper.SmsTable.TIME+" ASC");//时间排序

//                final Cursor cursor = getContentResolver().query(SmsProvider.URI_SMS, null,
//                        null,
//                        null,
//                       null);//时间排序

                //没有数据直接返回
                if (cursor.getCount() < 1) {
                    return;
                }
                //创建adapter
                ThreadUtils.runInUIThread(new Runnable() {
                    public static final int SMS_RECEIVER = 1;
                    public static final int SMS_SENDER = 0;

                    @Override
                    public void run() {
                        mAdapter = new CursorAdapter(ChatActivity.this, cursor) {
                            //如果convertView==null,则执行newView,返回一个根视图
//                            @Override
//                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
//                                TextView tv=new TextView(context);
//                                return tv;
//                            }
//
//                            @Override
//                            public void bindView(View graduationdesign.muguihai.com.v023.view, Context context, Cursor cursor) {
//                                TextView tv= (TextView) graduationdesign.muguihai.com.v023.view;
//                                //具体设置数据
//                                String smsBody=cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
//                                tv.setText(smsBody);
//                            }

                            @Override
                            public int getItemViewType(int position) {
                                //接收----当前account不是消息的创建者
                                //发送----当前account是消息的创建者
                                cursor.moveToPosition(position);

                                //获得消息创建者
                                String fromAccount = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT));
                                if (!XMPPService.current_account.equals(fromAccount)) {
                                    //接收的消息
                                    return SMS_RECEIVER;
                                } else {
                                    //发送的消息
                                    return SMS_SENDER;
                                }
//                                return super.getItemViewType(position);//0和1
                            }

                            @Override
                            public int getViewTypeCount() {
                                return super.getViewTypeCount() + 1;//默认1种，+1为2种
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                ViewHolder holder;
                                //接收的消息
                                if (getItemViewType(position) == SMS_RECEIVER) {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_receive, null);
                                        holder = new ViewHolder();
                                        convertView.setTag(holder);

                                        //holder初始化
                                        holder.time = (TextView) convertView.findViewById(R.id.time);
                                        holder.sms_body = (TextView) convertView.findViewById(R.id.content);
                                        holder.head = (ImageView) convertView.findViewById(R.id.head);
                                    } else {
                                        holder = (ViewHolder) convertView.getTag();
                                    }

                                    //发送的消息
                                } else {
                                    if (convertView == null) {
                                        convertView = View.inflate(ChatActivity.this, R.layout.item_chat_send, null);
                                        holder = new ViewHolder();
                                        convertView.setTag(holder);

                                        //holder初始化
                                        holder.time = (TextView) convertView.findViewById(R.id.time);
                                        holder.sms_body = (TextView) convertView.findViewById(R.id.content);
                                        holder.head = (ImageView) convertView.findViewById(R.id.head);
                                    } else {
                                        holder = (ViewHolder) convertView.getTag();
                                    }

                                }

                                //数据展示
                                cursor.moveToPosition(position);
                                String time = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TIME));
                                String sms_body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                String formatTime = new SimpleDateFormat("yyyy-MM--dd HH:mm:ss").format(new Date(Long.parseLong(time)));

                                holder.time.setText(formatTime);
                                holder.sms_body.setText(sms_body);

                                return super.getView(position, convertView, parent);
                            }

                            @Override
                            public View newView(Context context, Cursor cursor, ViewGroup parent) {

                                return null;
                            }

                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {

                            }

                            class ViewHolder {
                                TextView sms_body;
                                TextView time;
                                ImageView head;
                            }
                        };

                        mChat_listView.setAdapter(mAdapter);
                        mChat_listView.setSelection(mAdapter.getCount() - 1);
                    }
                });

            }
        });

    }

    private void initListener() {
        final QuickAction quickAction = new QuickAction(getApplicationContext(), QuickAction.HORIZONTAL);

        quickAction.addActionItem(new ActionItem(0, "删除"));
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                switch (actionId) {
                    case 0:
                        ToastUtils.myToast(getApplicationContext(),"删除");
//                                removeChat(account);
                        break;
                    default:
                        break;
                }
            }
        });
        mChat_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                quickAction.show(mChat_listView.getChildAt(position));
                return false;
            }
        });
    }

    /**
     * 发送消息按钮
     */
    private void send_click(){

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final String text=editText.getText().toString();
                ToastUtils.myToast(getApplicationContext(), text);
                //3.初始化消息
                Message msg = new Message();
                msg.setFrom(XMPPService.current_account);//从当前登录的用户
                msg.setTo(chat_account);//发送给
                msg.setBody(text);
                msg.setType(Message.Type.chat);//类型
                msg.setProperty("key", "value");//属性，额外的信息

                //TODO 调用Service里面的sendMessage()方法---->boundService
                xmppService.sendMessage(msg);


                //更新或者插入会话表
                xmppService.saveOrUpdateSession(chat_account,msg);
                //4.清空edittext
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        editText.setText(null);
                    }
                });


            }
        });

    }




    @Override
    protected void onDestroy() {
        unRegisterContentProvider();
        if (conn!=null){
            unbindService(conn);
        }
        super.onDestroy();
    }

    /**
     * Creates a content observer.监听数据记录的改变
     *
     */
    MyContentObserver myContentObserver =new MyContentObserver(new Handler());
    //注册监听
    public void registerContentProvider(){
        getContentResolver().registerContentObserver(SmsProvider.URI_SMS,true, myContentObserver);
    }
    //注销监听
    public void unRegisterContentProvider(){
        getContentResolver().unregisterContentObserver(myContentObserver);
    }

    class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 接收到数据记录的改变
         * @param selfChange
         */
        @Override
        public void onChange(boolean selfChange) {
            //设置或更新adapter
            setOrNotifyAdapter();
            super.onChange(selfChange);
        }


    }

    /**
     * MyServiceConnection
     */
    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("onServiceConnected","onServiceConnected");
            XMPPService.MyBinder binder= (XMPPService.MyBinder) service;
            xmppService = binder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("onServiceDisconnected","onServiceDisconnected");

        }
    }
}

