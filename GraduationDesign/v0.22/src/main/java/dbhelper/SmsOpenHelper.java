package dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * 数据库和表的创建
 * Created by vero on 2016/3/29.
 */
public class SmsOpenHelper extends SQLiteOpenHelper{
    public static final String TABLE_SMS="table_sms";


    public class SmsTable implements BaseColumns{
        /**
         * 表结构
         * from_account:发送者
         * to_account:接受者
         * body:消息体
         * status:发送状态
         * type:类型
         * time:发送时间
         * session_account:会话id
         * session_belong_to:会话属于谁
         */
        public static final String FROM_ACCOUNT="from_account";
        public static final String TO_ACCOUNT="to_account";
        public static final String BODY="body";
        public static final String STATUS="status";
        public static final String TYPE="type";
        public static final String TIME="time";
        public static final String SESSION_ACCOUNT="session_account";
    }


    public SmsOpenHelper(Context context) {
        super(context, "sms.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="CREATE TABLE "+TABLE_SMS+"(" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SmsTable.FROM_ACCOUNT+" TEXT," +
                    SmsTable.TO_ACCOUNT+" TEXT,"  +
                    SmsTable.BODY+" TEXT," +
                    SmsTable.STATUS+" TEXT," +
                    SmsTable.TYPE+" TEXT," +
                    SmsTable.TIME+" TEXT," +
                    SmsTable.SESSION_ACCOUNT+" TEXT" +
                    ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
