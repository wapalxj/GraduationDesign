package com.muguihai.beta1.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by vero on 2016/4/14.
 */
public class GroupOpenHelper extends SQLiteOpenHelper {

    public static String TABLE_GROUP="table_group";

    //实现接口：默认添加一列：_id
    public class GroupTable implements BaseColumns {
        /**
         * _id:主键
         * groupname：分组名称
         * blong_to:属于哪个用户的
         */
        public static final String GROUPNAME="groupname";
        public static final String BELONG_TO ="belong_to";

    }

    public GroupOpenHelper(Context context) {
        super(context, "group.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//       String tbl_name=TABLE_CONTACT+XMPPService.current_account.substring(0,2);
        //建表
        String sql="CREATE TABLE "+TABLE_GROUP+"" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                GroupTable.GROUPNAME+" TEXT, " +
                GroupTable.BELONG_TO +" TEXT"+
                ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
