package graduationdesign.muguihai.com.v022;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import dbhelper.PacketOpenHelper;
import dbhelper.SmsOpenHelper;
import provider.PacketProvider;
import provider.SmsProvider;

/**
 * Created by vero on 2016/4/3.
 */
public class TestPacketProvider extends AndroidTestCase {

    public void testInsert(){
        /**
         * _id:主键
         * packet_account：账号
         * packet_nickname：昵称
         * type:消息类型
         */

        ContentValues values=new ContentValues();
        values.put(PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM,"packet1@vero");
        values.put(PacketOpenHelper.Packet_Table.PACKET_NICKNAME_FROM,"packet1");
        values.put(PacketOpenHelper.Packet_Table.TYPE,"sub");
        values.put(PacketOpenHelper.Packet_Table.PINYIN,"pying");

        getContext().getContentResolver().insert(
                PacketProvider.URI_PACKET,values
        );
    }

    public void testDelete(){
        getContext().getContentResolver().delete(
                PacketProvider.URI_PACKET, PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM+"=?",
                new String[]{"packet1@vero"}
        );
    }

    public void testUpdate(){
        ContentValues values=new ContentValues();
        values.put(PacketOpenHelper.Packet_Table.PINYIN,"pppppppppppp");

        getContext().getContentResolver().update(
                PacketProvider.URI_PACKET, values,PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM+"=?",
                new String[]{"packet1@vero"}
        );
    }

    public void testQuery(){
        Cursor cursor=getContext().getContentResolver().query(
                PacketProvider.URI_PACKET, null, null, null, null
        );
        int cols=cursor.getColumnCount();
        while (cursor.moveToNext()){
            for (int i=0;i<cols;i++){
//                Log.i("sms_query",cursor.getString())
                System.out.print(cursor.getString(i)+"---");
            }
            System.out.println("");
        }
    }
}
