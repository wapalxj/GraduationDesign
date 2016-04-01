package dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by vero on 2016/3/24.
 */
public class ContactOpenHelper extends SQLiteOpenHelper{

    public static final String TABLE_CONTACT="table_contact";

    //实现接口：默认添加一列：_id
    public class ContactTable implements BaseColumns{
        /**
         * _id:主键
         * account：账号
         * nickname：昵称
         * avatar：头像
         * pinying：账号拼音
         */
        public static final String ACCOUNT="account";
        public static final String NICKNAME="nickname";
        public static final String AVATAR="avatar";
        public static final String PINYIN ="pinying";

    }

    public ContactOpenHelper(Context context) {
        super(context, "contact.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        String sql="CREATE TABLE "+TABLE_CONTACT+"" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                ContactTable.ACCOUNT+" TEXT, " +
                ContactTable.NICKNAME+" TEXT, "+
                ContactTable.AVATAR+" TEXT, "+
                ContactTable.PINYIN +" TEXT"+
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
