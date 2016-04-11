package com.muguihai.beta1.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.muguihai.beta1.activity.LoginActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.utils.PinyinUtil;
import com.muguihai.beta1.utils.ThreadUtils;
import com.muguihai.beta1.utils.ToastUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class XMPPService extends Service {
    public static XMPPConnection conn;
    public static String current_account;//当前登录用户的JID
    private Roster roster;
    private MyRosterlistener rosterlistener;

    private ChatManager chatManager;
    private Chat mCurChat;
    private Map<String,Chat> mChatMap;//存储所有chat

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public XMPPService getService(){
            return XMPPService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i("service","service---onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("service","service---onStartCommand");
        //同步roster
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                mChatMap=new HashMap<>();
                myMessageListener=new MyMessageListener();
                myChatManagerListener =new MyChatManagerListener();
                Log.i("service", "--------同步roster_begin------");
                //获取所有联系人
                //连接对象
                //获取Roster
                roster = XMPPService.conn.getRoster();
                //得到所有联系人
                final Collection<RosterEntry> entries = roster.getEntries();
                //打印所有联系人
                for (RosterEntry entry : entries) {
                    System.out.print(entry.toString() + "----");
                    System.out.print(entry.getUser() + "----");
                    System.out.print(entry.getName() + "----");
                    System.out.print(entry.getStatus() + "----");
                    System.out.print(entry.getType() + "----");
                    System.out.println(" ");
                }
                //监听联系人的改变
                rosterlistener = new MyRosterlistener();
                roster.addRosterListener(rosterlistener);


                for (RosterEntry entry : entries) {
                    saveOrUpdateEntry(entry);
                }

                Log.i("service", "--------同步roster_end------");

                Log.i("service", "--------消息监听处理------");
                //1.获取消息管理者
                if (chatManager == null) {
                    chatManager = XMPPService.conn.getChatManager();
                }

                //会话监听器:当参与者住的发起会话的时候的监听
                chatManager.addChatListener(myChatManagerListener);

                Log.i("service", "--------消息监听处理end------");


            }
        });
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        Log.i("service","IMService---onDestroy");
        //移除联系人监听
        if (roster!=null&&rosterlistener!=null){
            roster.removeRosterListener(rosterlistener);
        }
        //移除消息监听
        if (mCurChat !=null&&myMessageListener!=null){
            mCurChat.removeMessageListener(myMessageListener);
        }

        super.onDestroy();
    }


    /**
     *联系人监听器
     */

    class MyRosterlistener implements RosterListener {

        //联系人添加
        @Override
        public void entriesAdded(Collection<String> addrs) {
            Log.i("--entriesAdded--","--entriesAdded--");
            //对应更新数据库
            for (String addr:addrs){
                RosterEntry entry=roster.getEntry(addr);
                //更新或插入
                saveOrUpdateEntry(entry);
            }
        }

        //联系人修改
        @Override
        public void entriesUpdated(Collection<String> addrs) {
            Log.i("--entriesUpdated--", "--entriesUpdated--");
            //对应更新数据库
            for (String addr:addrs){
                RosterEntry entry=roster.getEntry(addr);
                //更新或插入
                saveOrUpdateEntry(entry);
            }
        }

        //联系人删除
        @Override
        public void entriesDeleted(Collection<String> addrs) {
            Log.i("--entriesDeleted--", "--entriesDeleted--");
            //对应更新数据库
            for (String account:addrs){
                //删除
                getContentResolver().delete(ContactsProvider.URI_CONTACT,
                        ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
            }
        }

        //联系人状态
        @Override
        public void presenceChanged(Presence presence) {
            Log.i("--presenceChanged--", "--presenceChanged--");
        }
    }

    //    MyMessageListener myMessageListener=new MyMessageListener();
    MyMessageListener myMessageListener;

    /**
     *消息监听器
     */
    class MyMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            //message:接收到的message
            String body=message.getBody();
            ToastUtils.myToast(getApplicationContext(), body);
            Log.i("message", "body:" + message.getBody());
            Log.i("message", "type:" + message.getType());
            Log.i("message","from:"+message.getFrom());
            Log.i("message","to:"+message.getTo());
            Log.i("message","pro:"+message.getProperty("key"));

            //收到消息保存
            String participant=chat.getParticipant();
            saveMessage(participant, message);
        }
    }
    //    MyChatManagerListener myChatManagerListener =new MyChatManagerListener();
    MyChatManagerListener myChatManagerListener;
    /**
     * 会话Chat监听器
     */
    class MyChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean createLocally) {
            Log.i("chatCreated","chatCreated");
            //判断chat是否存在
            String participant=chat.getParticipant();
            participant=participant.substring(0,participant.indexOf("@"));

            if (!mChatMap.containsKey(participant)){
                //保存chat
                mChatMap.put(participant,chat);
                chat.addMessageListener(myMessageListener);
            }

            if (createLocally){
                Log.i("chatCreated","我："+XMPPService.current_account+":创建的chat,jid="+chat.getParticipant());
                //jid:v1@vero
            }else {
                Log.i("chatCreated","别人创建的chat,jid="+chat.getParticipant());
                //jid:v1@vero/Spark 2.6.3
            }
        }
    }

    /**
     * 更新或者插入联系人
     */
    private void saveOrUpdateEntry(RosterEntry entry){
        ContentValues values=new ContentValues();
        String account=entry.getUser();
        String nickName=entry.getName();
        String pinyinName=entry.getName();
        String belong_to=XMPPService.current_account;
        PinyinUtil.strToPinyin(account);
        if (nickName==null||"".equals(nickName)){
            nickName=account.substring(0,account.indexOf("@"));
        }

        values.put(ContactOpenHelper.ContactTable.ACCOUNT,account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME,nickName);
        values.put(ContactOpenHelper.ContactTable.AVATAR, "0");
        values.put(ContactOpenHelper.ContactTable.PINYIN, pinyinName);
        values.put(ContactOpenHelper.ContactTable.BELONG_TO, belong_to);

        //先update在insert
        int uCount=getContentResolver().update(ContactsProvider.URI_CONTACT,
                values, ContactOpenHelper.ContactTable.ACCOUNT + "=? and "+ContactOpenHelper.ContactTable.BELONG_TO+ "=?",
                new String[]{account,XMPPService.current_account});

        if (uCount<=0){
            getContentResolver().insert(ContactsProvider.URI_CONTACT,values);
        }
    }


    /**
     * 发送消息
     */
    public void sendMessage(final Message msg){

        //判断chat对象是否存在,当前chat和临时chat
        //当前chat实时改变
        //2.创建聊天对象
        //chatManager.createChat("被聊天对象的JID",msg_listener);

        Chat chat=null;
        String toAccount=msg.getTo();
        if (mChatMap.containsKey(toAccount)){
            chat=mChatMap.get(toAccount);
        }else {
            chat = chatManager.createChat(toAccount, myMessageListener);
            mChatMap.put(toAccount,chat);

        }
        mCurChat=chat;
        //发送
        try {
            mCurChat.sendMessage(msg);
            //保存消息
            saveMessage(msg.getTo(), msg);
        } catch (XMPPException e) {
            ToastUtils.myToast(getApplicationContext(), "发送失败");
            e.printStackTrace();
        }


    }


    /**
     * 保存message:contentResolver--->contentProvider--->sqlite
     * @param msg
     */
    private void saveMessage(String sessionAccount,Message msg) {
        ContentValues values=new ContentValues();

        //首先过滤
        sessionAccount=filterAccount(sessionAccount);
        String from=msg.getFrom();
        from=filterAccount(from);
        String to=msg.getTo();
        to=filterAccount(to);
        String session_belong_to=XMPPService.current_account;

        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT,from);
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT, to);
        values.put(SmsOpenHelper.SmsTable.BODY,msg.getBody());
        values.put(SmsOpenHelper.SmsTable.STATUS,"online");
        values.put(SmsOpenHelper.SmsTable.TYPE,msg.getType().name());
        values.put(SmsOpenHelper.SmsTable.TIME,System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, sessionAccount);
        values.put(SmsOpenHelper.SmsTable.SESSION_BELONG_TO, session_belong_to);

        getContentResolver().insert(
                SmsProvider.URI_SMS, values
        );
    }

    //账户名称过滤
    private String filterAccount(String sessionAccount){
        sessionAccount=sessionAccount.substring(0,sessionAccount.indexOf("@"))+"@"+ LoginActivity.SERVICENAME;
        return sessionAccount;
    }


}
