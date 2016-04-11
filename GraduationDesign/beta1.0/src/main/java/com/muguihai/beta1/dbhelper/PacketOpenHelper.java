package com.muguihai.beta1.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


/**
 * Created by vero on 2016/4/3.
 * 接收系统消息
 */
public class PacketOpenHelper extends SQLiteOpenHelper {
    public static final String TABLE_PACKET="table_packet";

    public PacketOpenHelper(Context context) {
        super(context, "packet.db", null, 1);
    }

    //实现接口：默认添加一列：_id
    public class Packet_Table implements BaseColumns {
        /**
         * _id:主键
         * packet_account：账号
         * packet_nickname：昵称
         * packet_type:packet类型
         * type:消息类型
         * packet_belong_to
         */
        public static final String PACKET_ACCOUNT_FROM="packet_account_from";
        public static final String PACKET_NICKNAME_FROM="packet_nickname_from";
        public static final String PACKET_TYPE="packet_from_type";
        public static final String TYPE="type";
        public static final String PINYIN ="pinying";
        public static final String PACKET_BELONG_TO="packet_belong_to";


    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        String sql="CREATE TABLE "+TABLE_PACKET+"" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                Packet_Table.PACKET_ACCOUNT_FROM+" TEXT, " +
                Packet_Table.PACKET_NICKNAME_FROM+" TEXT, "+
                Packet_Table.PACKET_TYPE+" TEXT, "+
                Packet_Table.TYPE+" TEXT, "+
                Packet_Table.PINYIN +" TEXT, "+
                Packet_Table.PACKET_BELONG_TO +" TEXT"+
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
