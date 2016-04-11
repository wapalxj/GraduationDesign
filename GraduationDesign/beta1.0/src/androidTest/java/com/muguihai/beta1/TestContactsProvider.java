package com.muguihai.beta1;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;


/**
 * ContentProvider的测试
 * Created by vero on 2016/3/24.
 */
public class TestContactsProvider extends AndroidTestCase{
    //测试插入
    public void testInsert(){
        ContentValues values=new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT,"vero1@vero");
        values.put(ContactOpenHelper.ContactTable.NICKNAME,"vnix");
        values.put(ContactOpenHelper.ContactTable.AVATAR,"0");
        values.put(ContactOpenHelper.ContactTable.PINYIN,"muguihai");
        getContext().getContentResolver().insert(ContactsProvider.URI_CONTACT,values);
    }

    public void testDelete(){
        getContext().getContentResolver().delete(ContactsProvider.URI_CONTACT,
                ContactOpenHelper.ContactTable.ACCOUNT+"=?",new String[]{"vero1@vero"});
    }

    public void testUpdate(){
        ContentValues values=new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT,"vero1@vero");
        values.put(ContactOpenHelper.ContactTable.NICKNAME,"我是vnix");
        values.put(ContactOpenHelper.ContactTable.AVATAR,"0");
        values.put(ContactOpenHelper.ContactTable.PINYIN,"wos---muguihai");

        getContext().getContentResolver().update(ContactsProvider.URI_CONTACT,
                values, ContactOpenHelper.ContactTable.ACCOUNT+"=?",new String[]{"vero1@vero"});
    }

    public void testQuery(){
        Cursor cursor=getContext().getContentResolver().query(ContactsProvider.URI_CONTACT,null,null,null,null);
        int colCounts=cursor.getColumnCount();//获取列数
        while (cursor.moveToNext()){
            for (int i=0;i<colCounts;i++){
                System.out.print(cursor.getString(i)+"----");
            }
            System.out.println(" ");
        }
    }

    //测试拼音的转换
    public void testPinyin(){
        String ps= PinyinHelper.convertToPinyinString("积极", "", PinyinFormat.WITHOUT_TONE);
        System.out.println(ps);
    }

}
