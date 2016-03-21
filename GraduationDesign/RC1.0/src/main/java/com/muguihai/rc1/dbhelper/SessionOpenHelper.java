package com.muguihai.rc1.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by vero on 2016/4/22.
 */
public class SessionOpenHelper extends SQLiteOpenHelper {
    public static final String TABLE_SESSION="table_session";


    public class SessionTable implements BaseColumns {
        /**
         * 表结构
         * from_account:发送者
         * to_account:接受者
         * body:消息体
         * status:发送状态
         * type:类型
         * session_account:会话id
         * session_belong_to:会话属于谁
         */
        public static final String FROM_ACCOUNT="from_account";
        public static final String TO_ACCOUNT="to_account";
        public static final String BODY="body";
        public static final String STATUS="status";
        public static final String TYPE="type";
        public static final String SESSION_ACCOUNT="session_account";
        public static final String SESSION_NICKNAME="session_nickname";
        public static final String SESSION_BELONG_TO="session_belong_to";
    }


    public SessionOpenHelper(Context context) {
        super(context, "session.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="CREATE TABLE "+TABLE_SESSION+"(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                SessionTable.FROM_ACCOUNT+" TEXT," +
                SessionTable.TO_ACCOUNT+" TEXT,"  +
                SessionTable.BODY+" TEXT," +
                SessionTable.STATUS+" TEXT," +
                SessionTable.TYPE+" TEXT," +
                SessionTable.SESSION_ACCOUNT+" TEXT," +
                SessionTable.SESSION_NICKNAME+" TEXT," +
                SessionTable.SESSION_BELONG_TO+" TEXT" +
                ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

