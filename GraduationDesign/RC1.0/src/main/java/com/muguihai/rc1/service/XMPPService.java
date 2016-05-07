package com.muguihai.rc1.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.muguihai.rc1.activity.LoginActivity;
import com.muguihai.rc1.activity.SlideActivity;
import com.muguihai.rc1.dbhelper.ContactOpenHelper;
import com.muguihai.rc1.dbhelper.GroupOpenHelper;
import com.muguihai.rc1.dbhelper.SessionOpenHelper;
import com.muguihai.rc1.dbhelper.SmsOpenHelper;
import com.muguihai.rc1.provider.ContactsProvider;
import com.muguihai.rc1.provider.GroupProvider;
import com.muguihai.rc1.provider.SessionProvider;
import com.muguihai.rc1.provider.SmsProvider;
import com.muguihai.rc1.utils.PinyinUtil;
import com.muguihai.rc1.utils.ThreadUtils;
import com.muguihai.rc1.utils.ToastUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 * 用于管理添加删除别人
 */
public class XMPPService extends Service {
    public static XMPPConnection conn;
    public static String current_account;//当前登录用户的JID
    public static String current_password;//当前登录用户的密码
    private Roster roster;
    private MyRosterlistener rosterlistener;
    private MyConnectionListener connectionListener;

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
        Log.i("XMPPService","service---onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("XMPPService","service---onStartCommand");
        if (checkConnection()){
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

                    //监听联系人的改变
                    rosterlistener = new MyRosterlistener();
                    roster.addRosterListener(rosterlistener);
                    //--------------得到联系人分组--------------
                    Collection<RosterGroup> groups = roster.getGroups();

                    if (groups.isEmpty()){
                        Log.i("roast分组","没有分组");
//                    roster.createGroup("Friends");
                    }else {
                        for (RosterGroup group : groups) {
                            System.out.println("分组名称："+(group.getName()+"----"));
                            System.out.println("分组人数："+group.getEntryCount()+"----");
                            saveOrUpdateGroup(group.getName());

                            Collection<RosterEntry> entries =group.getEntries();
                            for (RosterEntry entry : entries) {
                                System.out.println("account----:"+entry.getUser());
                                System.out.println("nickname----:"+entry.getName() );
                                saveOrUpdateEntry(entry,group);

                            }
                            System.out.println(" ");
                        }
                    }
                    Log.i("XMPPService", "--------同步roster_end------");

                    Log.i("XMPPService", "--------消息监听处理------");
                    //1.获取消息管理者
                    if (chatManager == null) {
                        chatManager = XMPPService.conn.getChatManager();
                    }

                    //会话监听器:当参与者住的发起会话的时候的监听
                    chatManager.addChatListener(myChatManagerListener);

                    Log.i("XMPPService", "--------消息监听处理end------");

                }
            });
        }else {
            ToastUtils.myToast(getApplicationContext(),"网络连接失败，请检查网络！");
        }



        //------------
        connectionListener=new MyConnectionListener();

        XMPPService.conn.addConnectionListener(connectionListener);
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        Log.i("XMPPService","XMPPService---onDestroy");
        //移除联系人监听
        if (roster!=null&&rosterlistener!=null){
            roster.removeRosterListener(rosterlistener);
        }
        if (connectionListener!=null){
            XMPPService.conn.removeConnectionListener(connectionListener);
        }
        if (conn!=null&&checkConnection()){
            XMPPService.conn.disconnect();
            XMPPService.conn=null;
        }
        //移除消息监听
        if (mCurChat !=null&&myMessageListener!=null){
            mCurChat.removeMessageListener(myMessageListener);
        }
        XMPPService.current_account=null;

        super.onDestroy();
    }


    /**
     * 重连监听器
     */
    class MyConnectionListener implements ConnectionListener {
        @Override
        public void connectionClosed() {
            Log.i("connectionClosed","connectionClosed");
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(!checkConnection()){
                            XMPPService.conn.connect();
                            XMPPService.conn.login(XMPPService.current_account,XMPPService.current_password);
                            Thread.sleep(2000);
                        }
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.i("connectionClosedOnError","connectionClosedOnError");
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!checkConnection()){
                            XMPPService.conn.connect();
                            XMPPService.conn.login(XMPPService.current_account,XMPPService.current_password);
                            Thread.sleep(2000);
                        }
                       } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void reconnectingIn(int i) {
            Log.i("reconnectingIn","reconnectingIn");
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i("reconnectionSuccessful","reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.i("reconnectionFailed","reconnectionFailed");
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (checkConnection()){
                            XMPPService.conn.connect();
                            XMPPService.conn.login(XMPPService.current_account,XMPPService.current_password);
                        }
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     *联系人监听器
     */

    class MyRosterlistener implements RosterListener {

        //添加联系人
        @Override
        public void entriesAdded(Collection<String> addrs) {
            Log.i("--XMPPService--","--entriesAdded--");
            //对应更新数据库
            for (String addr:addrs){
                RosterEntry entry=roster.getEntry(addr);
                //更新或插入
            }
        }

        //联系人修改
        @Override
        public void entriesUpdated(Collection<String> addrs) {
            Log.i("--XMPPService--", "--entriesUpdated--");
        }

        //删除联系人
        @Override
        public void entriesDeleted(Collection<String> addrs) {
            Log.i("--XMPPService--", "--entriesDeleted--");
            //对应更新数据库
            for (String account:addrs){
                //删除
                //删除联系人
                getContentResolver().delete(ContactsProvider.URI_CONTACT,
                        ContactOpenHelper.ContactTable.ACCOUNT + "=? and "+ContactOpenHelper.ContactTable.BELONG_TO+ "=?"
                        , new String[]{account,XMPPService.current_account});

                // 删除会话
                getContentResolver().delete(
                        SmsProvider.URI_SMS,
                        SmsOpenHelper.SmsTable.SESSION_ACCOUNT+"=? and "+SmsOpenHelper.SmsTable.SESSION_BELONG_TO+ "=?"
                        ,
                        new String[]{account,XMPPService.current_account});
            }
        }

        //联系人状态
        @Override
        public void presenceChanged(Presence presence) {
            Log.i("--XMPPService--", "--presenceChanged--");
            String from  =presence.getFrom();
            String to  =presence.getTo();
            Presence.Type type=presence.getType();
            Log.i("--presenceChanged--", "--from:"+from+"--to:"+to+"--type:"+type.toString());
            from=filterAccount(from);
            updateEntryPresence(from,type);
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
            //收到消息保存
            String participant=chat.getParticipant();
            saveMessage(participant, message);
            //插入或者更新会话表
            saveOrUpdateSession(participant, message);
        }
    }

    MyChatManagerListener myChatManagerListener;
    /**
     * 会话Chat监听器
     */
    class MyChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean createLocally) {
            Log.i("XMPPService","chatCreated");
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
    private void saveOrUpdateEntry(RosterEntry entry,RosterGroup group){
        ContentValues values=new ContentValues();
        //获取资料
        VCard vCard=new VCard();
        try {
            //防止获取不到
            ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp",
                    new org.jivesoftware.smackx.provider.VCardProvider());
            vCard.load(XMPPService.conn,entry.getUser());
            System.out.println("nickname---"+vCard.getNickName());
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        String account=entry.getUser();
        String nickName=vCard.getNickName();

        String sign=vCard.getField("sign");
        String gender=vCard.getField("gender");
        String tel=vCard.getPhoneHome("tel");
        String addr=vCard.getAddressFieldHome("addr");
        String email=vCard.getEmailHome();

        String pinyinName=PinyinUtil.strToPinyin(account);
        String groupName=group.getName();
        String prensence=Presence.Type.unavailable.toString();
        String belong_to=XMPPService.current_account;
        if (nickName==null||"".equals(nickName)){
            nickName=account.substring(0,account.indexOf("@"));
        }

        values.put(ContactOpenHelper.ContactTable.ACCOUNT,account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME,nickName);
        values.put(ContactOpenHelper.ContactTable.PRESENCE, prensence);
        values.put(ContactOpenHelper.ContactTable.SIGNATURE, sign);
        values.put(ContactOpenHelper.ContactTable.GENDER, gender);
        values.put(ContactOpenHelper.ContactTable.TEL, tel);
        values.put(ContactOpenHelper.ContactTable.ADDR, addr);
        values.put(ContactOpenHelper.ContactTable.EMAIL, email);
        values.put(ContactOpenHelper.ContactTable.PINYIN, pinyinName);
        values.put(ContactOpenHelper.ContactTable.GROUP, groupName);
        values.put(ContactOpenHelper.ContactTable.BELONG_TO, belong_to);

        //先update在insert
        int uCount=getContentResolver().update(ContactsProvider.URI_CONTACT,
                values, ContactOpenHelper.ContactTable.ACCOUNT + "=? and "+ContactOpenHelper.ContactTable.BELONG_TO+ "=?",
                new String[]{account,XMPPService.current_account});


        if (uCount<=0){
            Log.i("chatCreated","插入"+nickName);
            getContentResolver().insert(ContactsProvider.URI_CONTACT,values);
        }
    }


    /**
     * 更新联系人状态
     */
    private void updateEntryPresence(String account,Presence.Type type){
        ContentValues values=new ContentValues();
        String belong_to=XMPPService.current_account;

        values.put(ContactOpenHelper.ContactTable.ACCOUNT,account);
        values.put(ContactOpenHelper.ContactTable.PRESENCE, type.toString());
        values.put(ContactOpenHelper.ContactTable.BELONG_TO, belong_to);

        //先update在insert
        int uCount=getContentResolver().update(ContactsProvider.URI_CONTACT,
                values, ContactOpenHelper.ContactTable.ACCOUNT + "=? and "+ContactOpenHelper.ContactTable.BELONG_TO+ "=?",
                new String[]{account,XMPPService.current_account});

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
        values.put(SmsOpenHelper.SmsTable.READ_STATUS,0);//设置未读
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, sessionAccount);
        values.put(SmsOpenHelper.SmsTable.SESSION_BELONG_TO, session_belong_to);

        getContentResolver().insert(
                SmsProvider.URI_SMS, values
        );

        //发送广播
        Intent session=new Intent(SlideActivity.XMPPReceiver.SESSION_ACTION);
        session.putExtra(SlideActivity.XMPPReceiver.SESSION,0);
        sendBroadcast(session);
    }

    //账户名称过滤
    private String filterAccount(String sessionAccount){
        sessionAccount=sessionAccount.substring(0,sessionAccount.indexOf("@"))+"@"+ LoginActivity.SERVICENAME;
        return sessionAccount;
    }

    /**
     * 更新或者插入组
     */
    private void saveOrUpdateGroup(String groupname){
        ContentValues values=new ContentValues();
        String belong_to=XMPPService.current_account;

        values.put(GroupOpenHelper.GroupTable.GROUPNAME,groupname);
        values.put(GroupOpenHelper.GroupTable.BELONG_TO, belong_to);

        //先update在insert
        int uCount=getContentResolver().update(GroupProvider.URI_GROUP,
                values, GroupOpenHelper.GroupTable.GROUPNAME+ "=? and "+GroupOpenHelper.GroupTable.BELONG_TO+ "=?",
                new String[]{groupname,XMPPService.current_account});


        if (uCount<=0){
            Log.i("chatCreated","插入"+groupname);
            getContentResolver().insert(GroupProvider.URI_GROUP,values);
        }

    }

    /**
     * 更新或者插入会话
     */
    public void saveOrUpdateSession(String sessionAccount,Message msg) {
        if (checkConnection()){
            ContentValues values=new ContentValues();
            //首先过滤
            sessionAccount=filterAccount(sessionAccount);

            VCard vCard=new VCard();
            try {
                //防止获取不到
                ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp",
                        new org.jivesoftware.smackx.provider.VCardProvider());
                vCard.load(XMPPService.conn,sessionAccount);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            String from=msg.getFrom();
            from=filterAccount(from);
            String to=msg.getTo();
            to=filterAccount(to);
            String session_belong_to=XMPPService.current_account;

            String nickName=null;
            if (XMPPService.conn.getRoster().contains(sessionAccount)){
                nickName=vCard.getNickName();
            }
            if (nickName==null||"".equals(nickName)){
                nickName=sessionAccount.substring(0,sessionAccount.indexOf("@"));
            }

            values.put(SessionOpenHelper.SessionTable.FROM_ACCOUNT,from);
            values.put(SessionOpenHelper.SessionTable.TO_ACCOUNT,to);
            values.put(SessionOpenHelper.SessionTable.BODY,msg.getBody());
            values.put(SessionOpenHelper.SessionTable.TYPE,msg.getType().name());
            values.put(SessionOpenHelper.SessionTable.SESSION_ACCOUNT, sessionAccount);
            values.put(SessionOpenHelper.SessionTable.SESSION_NICKNAME, nickName);
            values.put(SessionOpenHelper.SessionTable.SESSION_BELONG_TO, session_belong_to);

            //先update在insert
            int uCount=getContentResolver().update(SessionProvider.URI_SESSION,
                    values,SessionOpenHelper.SessionTable.SESSION_ACCOUNT + "= ? and "+SessionOpenHelper.SessionTable.SESSION_BELONG_TO+ "=?",
                    new String[]{sessionAccount,XMPPService.current_account});

            if (uCount<=0){
                Log.i("chatCreated","插入会话"+nickName);
                getContentResolver().insert(SessionProvider.URI_SESSION,values);
            }
        }else {
            ToastUtils.myToast(getApplicationContext(),"网络连接失败，请检查网络!");
            return;
        }
    }

    public static boolean checkConnection(){
        Log.i("ccccccccccc",XMPPService.conn.isConnected()+"");
        return XMPPService.conn.isConnected();
    }
}
