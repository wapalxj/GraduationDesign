package service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.preference.Preference;
import android.util.Log;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import dbhelper.ContactOpenHelper;
import dbhelper.PacketOpenHelper;
import graduationdesign.muguihai.com.v022.LoginActivity;
import provider.ContactsProvider;
import provider.PacketProvider;
import utils.PinyinUtil;

/**
 * 接收openfire的全局消息
 */

public class PushService extends Service {

    public PushService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i("PushService", "onCreate");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("PushService", "onStartCommand");
        IMService.conn.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                //Packet:消息包,Message的父类
                if (packet instanceof Presence){
                    Log.i("PushService---packet", packet.toXML());
                    Presence presence = (Presence) packet;
                    Presence.Type type = presence.getType();
                    if (type.equals(Presence.Type.subscribe)){
                        String pid = presence.getPacketID();
                        String from = presence.getFrom();
                        String nickName=from.substring(0, from.indexOf("@"));
                        Log.i("presence", "type:" + type + "------id:" + pid + "------from:" + from + "-----nickname:" + nickName);
                        saveOrUpdateEntry(presence);
                    }
                }


                //获取propertyNames集合:接收不到
//                Collection<String> propertyNames =packet.getPropertyNames();
//
//                for (String str : propertyNames){
//                    Log.i("propertyNames",""+packet.getProperty(str));
//                }

//                Message message = (Message) packet;
//                String body = (String) message.getProperty("body");//获取不到：null
//                String body1 = message.getBody();//可以获取到
//                Log.i("propertyNames", "body:" + body + "------body1:" + body1);


//                if(Roster.getDefaultSubscriptionMode().equals(
//                        Roster.SubscriptionMode.manual)) {
                //回复一个presence信息给用户:添加好友
//                    Presence subscription = new Presence(Presence.Type.subscribed);
//                    subscription.setTo(packet.getFrom());
//                    Log.i("subscription", "packetfrom:" + packet.getFrom() + "-----to:" + subscription.getTo());
//
//                    IMService.conn.sendPacket(subscription);
//                }


            }

        }, null);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("PushService", "PushService---onDestroy");
        super.onDestroy();
    }

    /**
     * 更新或者插入联系人
     */
    private void saveOrUpdateEntry(Presence presence){
        ContentValues values=new ContentValues();
        String account = presence.getFrom();
        String nickName=account.substring(0, account.indexOf("@"));
        Presence.Type type =presence.getType();
        String t=type.toString();
        String packet_type="presence";
        String pinyin=PinyinUtil.strToPinyin(nickName);

        values.put(PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM,account);
        values.put(PacketOpenHelper.Packet_Table.PACKET_NICKNAME_FROM,nickName);
        values.put(PacketOpenHelper.Packet_Table.PACKET_TYPE,packet_type);
        values.put(PacketOpenHelper.Packet_Table.TYPE, t);
        values.put(PacketOpenHelper.Packet_Table.PINYIN, pinyin);
        values.put(PacketOpenHelper.Packet_Table.PACKET_BELONG_TO, IMService.current_account);
        //先update在insert
        int uCount=getContentResolver().update(PacketProvider.URI_PACKET,
                values, PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM + "=?", new String[]{account});

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
