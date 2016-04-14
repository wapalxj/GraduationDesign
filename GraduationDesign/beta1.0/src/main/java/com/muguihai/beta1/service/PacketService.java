package com.muguihai.beta1.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.muguihai.beta1.activity.LoginActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.PacketOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.PacketProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.utils.PinyinUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

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
                    Log.i("PacketService---packet", packet.toXML());
                    Presence presence = (Presence) packet;
                    Presence.Type type = presence.getType();

                    String pid = presence.getPacketID();
                    String from = presence.getFrom();
                    String nickName=from.substring(0, from.indexOf("@"));
                    Log.i("presence", "type:" + type + "------id:" + pid + "------from:" + from + "-----nickname:" + nickName);
                    if (type.equals(Presence.Type.subscribe)){
                        //被添加好友
                        saveOrUpdateEntry(presence);

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

                        // 删除会话
                        getContentResolver().delete(
                                SmsProvider.URI_SMS,
                                SmsOpenHelper.SmsTable.SESSION_ACCOUNT+"=? and "+SmsOpenHelper.SmsTable.SESSION_BELONG_TO+ "=?"
                                ,
                                new String[]{from,XMPPService.current_account});
                    }
                }


            }

        }, null);
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
    private void saveOrUpdateEntry(Presence presence){
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
            getContentResolver().insert(PacketProvider.URI_PACKET,values);
        }
    }

    //账户名称过滤
    private String filterAccount(String sessionAccount){
        sessionAccount=sessionAccount.substring(0,sessionAccount.indexOf("@"))+"@"+ LoginActivity.SERVICENAME;
        return sessionAccount;
    }
}

