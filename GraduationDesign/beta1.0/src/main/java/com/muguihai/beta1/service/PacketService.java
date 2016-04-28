package com.muguihai.beta1.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.muguihai.beta1.activity.LoginActivity;
import com.muguihai.beta1.activity.SlideActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.PacketOpenHelper;
import com.muguihai.beta1.dbhelper.SessionOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.PacketProvider;
import com.muguihai.beta1.provider.SessionProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.utils.PinyinUtil;
import com.muguihai.beta1.utils.ThreadUtils;
import com.muguihai.beta1.utils.ToastUtils;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.packet.VCard;

import java.util.Iterator;

/**
 * 用于管理被添加被删除
 */
public class PacketService extends Service {

    public PacketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i("PacketService", "onCreate");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("PacketService", "onStartCommand");
        XMPPService.conn.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                //Packet:消息包,Message的父类
                if (packet instanceof Presence){
//                    Log.i("PacketService---packet", packet.toXML());
                    Presence presence = (Presence) packet;
                    Presence.Type type = presence.getType();

                    String pid = presence.getPacketID();
                    String from = presence.getFrom();
                    String nickName=from.substring(0, from.indexOf("@"));
                    Log.i("presence", "type:" + type + "------id:" + pid + "------from:" + from + "-----nickname:" + nickName);
                    if (type.equals(Presence.Type.subscribe)){
                        //被添加好友
                        //名单中没有则创建请求信息
                        if (!XMPPService.conn.getRoster().contains(from)){
                            saveOrUpdatePacket(presence);
                        }
                    }else if (type.equals(Presence.Type.subscribed)){
                        //对方同意加我为好友
                        RosterEntry entry=XMPPService.conn.getRoster().getEntry(from);
                        //加入组
                        RosterGroup group=XMPPService.conn.getRoster().getGroup("Friends");
                        try {
                            Presence subscribed = new Presence(Presence.Type.subscribed);
                            subscribed.setTo(from);
                            XMPPService.conn.sendPacket(subscribed);
                            group.addEntry(entry);
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }

                        //更新数据库
                        saveOrUpdateEntry(entry);
                        getContentResolver().notifyChange(ContactsProvider.URI_CONTACT,null);

                    }else if (type.equals(Presence.Type.unsubscribe)){
                        //被好友删除:我们也删除对方
                        try {
                            Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
                            unsubscribe.setTo(from);
                            XMPPService.conn.sendPacket(unsubscribe);
                            RosterEntry entry =XMPPService.conn.getRoster().getEntry(from);
                            XMPPService.conn.getRoster().removeEntry(entry);
                        } catch (XMPPException e) {
                            e.printStackTrace();
                        }


                        //删除联系人
                        getContentResolver().delete(ContactsProvider.URI_CONTACT,
                                ContactOpenHelper.ContactTable.ACCOUNT + "=? and "+ContactOpenHelper.ContactTable.BELONG_TO+ "=?"
                                , new String[]{from,XMPPService.current_account});

                        // 删除聊天记录
                        getContentResolver().delete(
                                SmsProvider.URI_SMS,
                                SmsOpenHelper.SmsTable.SESSION_ACCOUNT+"=? and "+SmsOpenHelper.SmsTable.SESSION_BELONG_TO+ "=?"
                                ,
                                new String[]{from,XMPPService.current_account});

                        // 删除会话
                        getContentResolver().delete(
                                SessionProvider.URI_SESSION,
                                SessionOpenHelper.SessionTable.SESSION_ACCOUNT+"=? and "+SessionOpenHelper.SessionTable.SESSION_BELONG_TO+ "=?"
                                ,
                                new String[]{from,XMPPService.current_account});
                    }
                }


            }

        }, null);


        //为防止卡顿，放在子线程中执行，但是执行时间会有点长
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //--------------离线消息处理--------------
                Log.i("XMPPService", "--------离线消息处理-----");
                    OfflineMessageManager offManager=new OfflineMessageManager(XMPPService.conn);
                    try {
//                    Log.i("offfffffffffffffff","离线消息数量: " + offManager.getMessageCount());
//                    offManager.supportsFlexibleRetrieval();
                        Iterator<Message> it = offManager.getMessages();
                        while (it.hasNext()) {
                            Message message = it.next();
//                            Log.i("收到离线消息", "from【" + message.getFrom() + "】 message: " + message.getBody());
                           if (message.getFrom().contains("@")){
                               String sessionAccount=message.getFrom().substring(0,message.getFrom().indexOf("@"))+"@"+LoginActivity.SERVICENAME;
                               Log.i("offffffffffff",sessionAccount);
                               saveMessage(sessionAccount,message);
                               saveOrUpdateSession(sessionAccount,message);
                           }
                        }
                        offManager.deleteMessages();//最后进行删除处理
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                    //设置上线
                    Presence presence = new Presence(Presence.Type.available);
                    XMPPService.conn.sendPacket(presence);
            }
        });


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("PacketService", "PacketService---onDestroy");
        super.onDestroy();
    }

    /**
     * 更新或者插入packet
     */
    private void saveOrUpdatePacket(Presence presence){
        ContentValues values=new ContentValues();
        String account = presence.getFrom();
        String nickName=account.substring(0, account.indexOf("@"));
        Presence.Type type =presence.getType();
        String t=type.toString();
        String packet_type="presence";
        String pinyin= PinyinUtil.strToPinyin(nickName);
        int handle_state= 0;//未处理

        values.put(PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM,account);
        values.put(PacketOpenHelper.Packet_Table.PACKET_NICKNAME_FROM,nickName);
        values.put(PacketOpenHelper.Packet_Table.PACKET_TYPE,packet_type);
        values.put(PacketOpenHelper.Packet_Table.TYPE, t);
        values.put(PacketOpenHelper.Packet_Table.PINYIN, pinyin);
        values.put(PacketOpenHelper.Packet_Table.HANDLE_STATE,handle_state);
        values.put(PacketOpenHelper.Packet_Table.PACKET_BELONG_TO, XMPPService.current_account);

        //先update在insert
        int uCount=getContentResolver().update(PacketProvider.URI_PACKET,
                values, PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM + "=? and "+PacketOpenHelper.Packet_Table.PACKET_BELONG_TO+"=? "
                , new String[]{account,XMPPService.current_account});

        if (uCount<=0){
            //插入
            getContentResolver().insert(PacketProvider.URI_PACKET,values);
            //发送广播
            Intent session=new Intent(SlideActivity.XMPPReceiver.MINE_ACTION);
            session.putExtra(SlideActivity.XMPPReceiver.MINE,0);
            sendBroadcast(session);
        }


    }

    //账户名称过滤
    private String filterAccount(String sessionAccount){
        sessionAccount=sessionAccount.substring(0,sessionAccount.indexOf("@"))+"@"+ LoginActivity.SERVICENAME;
        return sessionAccount;
    }

    /**
     * 更新或者插入联系人
     */
    private void saveOrUpdateEntry(RosterEntry entry){
        ContentValues values=new ContentValues();
        String account=entry.getUser();
        String nickName=entry.getName();
        String pinyinName=PinyinUtil.strToPinyin(account);
        String groupName="Friends";
        String presence=Presence.Type.unavailable.toString();
        String belong_to=XMPPService.current_account;
        if (nickName==null||"".equals(nickName)){
            nickName=account.substring(0,account.indexOf("@"));
        }

        values.put(ContactOpenHelper.ContactTable.ACCOUNT,account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME,nickName);
        values.put(ContactOpenHelper.ContactTable.PRESENCE, presence);
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

    /**
     * 更新或者插入会话
     */
    public void saveOrUpdateSession(String sessionAccount,Message msg) {
        ContentValues values=new ContentValues();

        //首先过滤
        sessionAccount=filterAccount(sessionAccount);

        String from=msg.getFrom();
        from=filterAccount(from);
        String to=msg.getTo();
        to=filterAccount(to);
        String session_belong_to=XMPPService.current_account;

        String nickName=null;

        if (XMPPService.conn.getRoster().contains(sessionAccount)){
            nickName=XMPPService.conn.getRoster().getEntry(sessionAccount).getName();
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
    }
}

