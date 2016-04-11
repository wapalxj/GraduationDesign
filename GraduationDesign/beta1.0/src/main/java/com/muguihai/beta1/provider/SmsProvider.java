package com.muguihai.beta1.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.muguihai.beta1.dbhelper.SmsOpenHelper;

public class SmsProvider extends ContentProvider {
    public static final String AUTHORITIES= SmsProvider.class.getCanonicalName();

    private SmsOpenHelper mHelper;
    static UriMatcher mUriMatcher;

    public static final int SMS=1;
    public static final int SESSION=2;
    //对应信息表的URI常量
    public static Uri URI_SMS=Uri.parse("content://"+AUTHORITIES+"/sms");
    //对应会话session表的URI常量
    public static Uri URI_SESSION=Uri.parse("content://"+AUTHORITIES+"/session");

    static {
        mUriMatcher =new UriMatcher(UriMatcher.NO_MATCH);
        //添加匹配规则
        mUriMatcher.addURI(AUTHORITIES, "/sms", SMS);//sms表
        mUriMatcher.addURI(AUTHORITIES, "/session", SESSION);//session表
    }

    public SmsProvider() {
    }

    @Override
    public boolean onCreate() {
        //创建数据库和表
        mHelper =new SmsOpenHelper(getContext());
        if (mHelper !=null){
            return true;
        }
        return false;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)){
            case SMS:
                long id= mHelper.getWritableDatabase()
                        .insert(SmsOpenHelper.TABLE_SMS,"",values);
                if (id>0){
                    Log.i("sms_insert","sms插入成功");
                    //拼接uri
                    uri= ContentUris.withAppendedId(uri,id);

                    //发送数据改变信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteRows=0;
        switch (mUriMatcher.match(uri)){
            case SMS:
                deleteRows= mHelper.getWritableDatabase()
                        .delete(SmsOpenHelper.TABLE_SMS,selection,selectionArgs);
                if (deleteRows>0){
                    Log.i("sms_delete","sms删除成功");
                    //发送数据改变信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS, null);
                }
                break;
            default:
                break;
        }
        return deleteRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int updateRows=0;
        switch (mUriMatcher.match(uri)){
            case SMS:
                updateRows= mHelper.getWritableDatabase()
                        .update(SmsOpenHelper.TABLE_SMS, values, selection, selectionArgs);
                if (updateRows>0){
                    Log.i("sms_update","sms更新成功");
                    //发送数据改变信号
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS,null);
                }
                break;
            default:
                break;
        }
        return updateRows;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor=null;
        switch (mUriMatcher.match(uri)){
            case SMS:
                cursor= mHelper.getWritableDatabase()
                        .query(SmsOpenHelper.TABLE_SMS, projection, selection,
                                selectionArgs,null,null,sortOrder);
                Log.i("sms_query","sms查询成功");
                break;
            case SESSION:
                cursor= mHelper.getWritableDatabase().rawQuery(
                        "SELECT * FROM ( " +
                                " SELECT * FROM table_sms WHERE " +
                                " (from_account= ? OR to_account= ?) and (session_belong_to= ?) " +
                                " ORDER BY time ASC)" +
                                " GROUP BY session_account"
                        ,selectionArgs);
//                        .query(SmsOpenHelper.TABLE_SMS, projection, selection,
//                                selectionArgs,null,null,sortOrder);
                Log.i("session_query","session查询成功");
                break;
            default:
                break;
        }
        return cursor;
    }


}
