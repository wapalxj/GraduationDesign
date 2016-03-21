package com.muguihai.rc1.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.muguihai.rc1.dbhelper.GroupOpenHelper;

public class GroupProvider extends ContentProvider {

    //主机地址常量:当前类的完整路径
    public static final String AUTHORITIES =GroupProvider.class.getCanonicalName();//获取类的完整路径
    //地址匹配对象
    public static UriMatcher mUriMatcher;
    //对应联系人表的URI常量
    public static final Uri URI_GROUP=Uri.parse("content://"+AUTHORITIES+"/group");

    public static final int GROUP=1;

    static {
        mUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        //匹配规则
        mUriMatcher.addURI(AUTHORITIES,"/group",GROUP);
        //content://provider.GroupProvider/group
    }

    private GroupOpenHelper mHelper;

    public GroupProvider() {
    }

    @Override
    public boolean onCreate() {
        mHelper = new GroupOpenHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * CRUD
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //数据是存到sqlite-->创建db文件，建表-->sqliteOpenHelper
        switch (mUriMatcher.match(uri)){
            case GROUP:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                long _id=db.insert(GroupOpenHelper.TABLE_GROUP,"",values);
                if (_id!=-1){
                    Log.i("ContactProvider","插入成功");
                    //拼接最新的Uri
                    //content://provider.ContactsProvider/contact/_id
                    uri= ContentUris.withAppendedId(uri,_id);

                    //通知observer数据改变了
                    notifyObserver();
                }
                break;
            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int del_count=0;
        switch (mUriMatcher.match(uri)){
            case GROUP:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                //返回影响的行数
                del_count=db.delete(GroupOpenHelper.TABLE_GROUP, selection, selectionArgs);
                if (del_count>0){
                    Log.i("ContactProvider","删除成功");

                    //通知observer数据改变了
                    notifyObserver();
                }
                break;
            default:
                break;
        }
        return del_count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int up_count=0;
        switch (mUriMatcher.match(uri)){
            case GROUP:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                //返回更新的行数
                up_count=db.update(GroupOpenHelper.TABLE_GROUP, values, selection, selectionArgs);
                if (up_count>0){
                    Log.i("ContactProvider", "更改成功");

                    //通知observer数据改变了
                    notifyObserver();
                }
                break;
            default:
                break;
        }
        return up_count;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor=null;
        switch (mUriMatcher.match(uri)){
            case GROUP:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                //返回更新的行数
                cursor=db.query(GroupOpenHelper.TABLE_GROUP, projection, selection, selectionArgs,null,null,sortOrder);
                Log.i("GroupProvider","查询成功");
                break;
            default:
                break;
        }
        return cursor;
    }

    public void notifyObserver(){
        //通知observer数据改变了
        getContext().getContentResolver().notifyChange(GroupProvider.URI_GROUP,null);
    }


}
