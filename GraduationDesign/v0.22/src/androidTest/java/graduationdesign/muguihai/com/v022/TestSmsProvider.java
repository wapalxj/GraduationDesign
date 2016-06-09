package graduationdesign.muguihai.com.v022;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

import dbhelper.SmsOpenHelper;
import provider.SmsProvider;
import service.IMService;

/**
 * Created by vero on 2016/3/29.
 */
public class TestSmsProvider extends AndroidTestCase{
    public void testInsert(){
        /**
         * 表结构
         * from_account:发送者
         * to_account:接受者
         * body:消息体
         * status:发送状态
         * type:类型
         * time:发送时间
         * session_account:会话id
         */

        ContentValues values=new ContentValues();
        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT,"vv1@vero");
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT,"eeeeeeeeeee@vero");
        values.put(SmsOpenHelper.SmsTable.BODY,"66666");
        values.put(SmsOpenHelper.SmsTable.STATUS,"offline");
        values.put(SmsOpenHelper.SmsTable.TYPE,"chat");
        values.put(SmsOpenHelper.SmsTable.TIME,System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT,"eeeeeeeeeee@vero");

        getContext().getContentResolver().insert(
                SmsProvider.URI_SMS,values
        );
    }

    public void testDelete(){
        getContext().getContentResolver().delete(
                SmsProvider.URI_SMS, SmsOpenHelper.SmsTable.FROM_ACCOUNT+"=?",
                new String[]{"vv1@vero"}
        );
    }

    public void testUpdate(){
        ContentValues values=new ContentValues();
        values.put(SmsOpenHelper.SmsTable.BODY,"999999999999999999999999");
        values.put(SmsOpenHelper.SmsTable.TIME,System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT,"eeeeeeeeeee@vero");

        getContext().getContentResolver().update(
                SmsProvider.URI_SMS, values,SmsOpenHelper.SmsTable.FROM_ACCOUNT+"=?",
                new String[]{"vv1@vero"}
        );
    }

    public void testQuery(){
        Cursor cursor=getContext().getContentResolver().query(
                SmsProvider.URI_SMS, null, null, null, null
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
