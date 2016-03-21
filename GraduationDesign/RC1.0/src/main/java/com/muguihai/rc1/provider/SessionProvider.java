package com.muguihai.rc1.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.muguihai.rc1.dbhelper.SessionOpenHelper;

public class SessionProvider extends ContentProvider {
    public static final String AUTHORITIES= SessionProvider.class.getCanonicalName();

    private SessionOpenHelper mHelper;
    static UriMatcher mUriMatcher;

    public static final int SESSION=1;
    //对应会话session表的URI常量
    public static Uri URI_SESSION=Uri.parse("content://"+AUTHORITIES+"/session");

    static {
        mUriMatcher =new UriMatcher(UriMatcher.NO_MATCH);
        //添加匹配规则
        mUriMatcher.addURI(AUTHORITIES, "/session", SESSION);//session表
    }

    public SessionProvider() {
    }

    @Override
    public boolean onCreate() {
        //创建数据库和表
        mHelper =new SessionOpenHelper(getContext());
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
            case SESSION:
                long id= mHelper.getWritableDatabase()
                        .insert(SessionOpenHelper.TABLE_SESSION,"",values);
                if (id>0){
                    Log.i("session_insert","session插入成功");
                    //拼接uri
                    uri= ContentUris.withAppendedId(uri,id);

                    //发送数据改变信号
                    getContext().getContentResolver().notifyChange(SessionProvider.URI_SESSION,null);
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
            case SESSION:
                deleteRows= mHelper.getWritableDatabase()
                        .delete(SessionOpenHelper.TABLE_SESSION,selection,selectionArgs);
                if (deleteRows>0){
                    Log.i("session_delete","session删除成功");
                    //发送数据改变信号
                    getContext().getContentResolver().notifyChange(SessionProvider.URI_SESSION, null);
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
            case SESSION:
                updateRows= mHelper.getWritableDatabase()
                        .update(SessionOpenHelper.TABLE_SESSION, values, selection, selectionArgs);
                if (updateRows>0){
                    Log.i("session_update","session更新成功");
                    //发送数据改变信号
                    getContext().getContentResolver().notifyChange(SessionProvider.URI_SESSION,null);
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
            case SESSION:
                cursor= mHelper.getWritableDatabase()
                        .query(SessionOpenHelper.TABLE_SESSION, projection, selection,
                                selectionArgs,null,null,sortOrder);
                Log.i("session_query","session查询成功");
                break;
            default:
                break;
        }
        return cursor;
    }


}
