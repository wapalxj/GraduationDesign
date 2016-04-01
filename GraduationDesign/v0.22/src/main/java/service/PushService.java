package service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

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
        Log.i("PushService","onCreate");
        IMService.conn.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                //Packet:消息包,Message的父类
                Log.i("PushService---packet",packet.toXML());

                //获取propertyNames集合:接收不到
//                Collection<String> propertyNames =packet.getPropertyNames();
//
//                for (String str : propertyNames){
//                    Log.i("propertyNames",""+packet.getProperty(str));
//                }
                Message message= (Message) packet;
                String body= (String) message.getProperty("body");//获取不到：null
                String body1= message.getBody();//可以获取到
                Log.i("propertyNames","body:"+body+"------body1:"+body1);

            }

        },null);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("PushService","onDestroy");
        super.onDestroy();
    }
}
