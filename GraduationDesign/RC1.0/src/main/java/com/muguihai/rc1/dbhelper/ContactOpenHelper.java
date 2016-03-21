package com.muguihai.rc1.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by vero on 2016/3/24.
 */
public class ContactOpenHelper extends SQLiteOpenHelper{

    public static String TABLE_CONTACT="table_contact";

    //实现接口：默认添加一列：_id
    public class ContactTable implements BaseColumns{
        /**
         * _id:主键
         * account：账号
         * nickname：昵称
         * presence：在线状态
         * pinying：账号拼音
         * group：分组
         * blong_to:属于哪个用户的
         *
         * signature
         * gender
         * tel
         * addr
         * email
         */
        public static final String ACCOUNT="account";
        public static final String NICKNAME="nickname";
        public static final String PRESENCE="presence";
        public static final String SIGNATURE="signature";
        public static final String GENDER="gender";
        public static final String TEL="tel";
        public static final String ADDR="addr";
        public static final String EMAIL="email";
        public static final String PINYIN ="pinyin";
        public static final String GROUP ="Rgroup";
        public static final String BELONG_TO ="belong_to";

    }

    public ContactOpenHelper(Context context) {
        super(context, "contact.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//       String tbl_name=TABLE_CONTACT+XMPPService.current_account.substring(0,2);
        //建表
        String sql="CREATE TABLE "+TABLE_CONTACT+"" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                ContactTable.ACCOUNT+" TEXT, " +
                ContactTable.NICKNAME+" TEXT, "+
                ContactTable.PRESENCE+" TEXT, "+
                ContactTable.SIGNATURE+" TEXT, "+
                ContactTable.GENDER+" TEXT, "+
                ContactTable.TEL+" TEXT, "+
                ContactTable.ADDR+" TEXT, "+
                ContactTable.EMAIL+" TEXT, "+
                ContactTable.PINYIN +" TEXT, "+
                ContactTable.GROUP +" TEXT, "+
                ContactTable.BELONG_TO +" TEXT"+
                ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}