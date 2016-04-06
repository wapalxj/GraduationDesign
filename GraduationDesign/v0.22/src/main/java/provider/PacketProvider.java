package provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import dbhelper.ContactOpenHelper;
import dbhelper.PacketOpenHelper;

public class PacketProvider extends ContentProvider {

    //主机地址常量:当前类的完整路径
    public static final String AUTHORITIES =PacketProvider.class.getCanonicalName();//获取类的完整路径
    //地址匹配对象
    public static UriMatcher mUriMatcher;
    //对应packet表的URI常量
    public static final Uri URI_PACKET=Uri.parse("content://"+AUTHORITIES+"/packet");

    public static final int PACKET=1;

    static {
        mUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        //匹配规则
        mUriMatcher.addURI(AUTHORITIES,"/packet",PACKET);
        //content://provider.PacketProvider/packet
    }

    private PacketOpenHelper mHelper;

    public PacketProvider() {
    }


    @Override
    public boolean onCreate() {
        mHelper=new PacketOpenHelper(getContext());
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
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //数据是存到sqlite-->创建db文件，建表-->sqliteOpenHelper
        switch (mUriMatcher.match(uri)){
            case PACKET:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                long _id=db.insert(PacketOpenHelper.TABLE_PACKET,"",values);
                if (_id!=-1){
                    Log.i("PacketProvider", "插入成功");
                    //拼接最新的Uri
                    //content://provider.ContactsProvider/contact/_id
                    uri= ContentUris.withAppendedId(uri, _id);

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
            case PACKET:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                //返回影响的行数
                del_count=db.delete(PacketOpenHelper.TABLE_PACKET, selection, selectionArgs);
                if (del_count>0){
                    Log.i("PacketProvider","删除成功");

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
            case PACKET:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                //返回更新的行数
                up_count=db.update(PacketOpenHelper.TABLE_PACKET, values, selection, selectionArgs);
                if (up_count>0){
                    Log.i("PacketProvider", "更改成功");

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
            case PACKET:
                SQLiteDatabase db=mHelper.getWritableDatabase();
                //返回更新的行数
                cursor=db.query(PacketOpenHelper.TABLE_PACKET, projection, selection, selectionArgs,null,null,sortOrder);
                Log.i("PacketProvider","查询成功");
                break;
            default:
                break;
        }
        return cursor;
    }

    public void notifyObserver(){
        //通知observer数据改变了
        Log.i("PacketProvider","notifyObserver");
        getContext().getContentResolver().notifyChange(PacketProvider.URI_PACKET,null);
    }
}
