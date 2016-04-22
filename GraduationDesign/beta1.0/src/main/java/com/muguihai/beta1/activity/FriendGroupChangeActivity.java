package com.muguihai.beta1.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.muguihai.beta1.R;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.ToastUtils;
import com.muguihai.beta1.view.spinner.MyAdapter;
import com.muguihai.beta1.view.spinner.User;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FriendGroupChangeActivity extends AppCompatActivity implements View.OnClickListener {

    private String account;
    private String nickname;
    private String currentGroup;

    private TextView moveG_toolbar_back;
    private Button moveG_commit;
    private Spinner spinner;
    private EditText new_group;
    private List<String> list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_group_change);
        init();
        initView();
    }
    private void init(){
        account = getIntent().getStringExtra(ChatActivity.CHAT_ACCOUNT);
        nickname = getIntent().getStringExtra(ChatActivity.CHAT_NICKNAME);
    }
    private void initView(){
        spinner= (Spinner) findViewById(R.id.spinner);
        new_group= (EditText) findViewById(R.id.new_group);
        moveG_toolbar_back= (TextView) findViewById(R.id.moveG_toolbar_back);
        moveG_commit= (Button) findViewById(R.id.moveG_commit);
        moveG_toolbar_back.setOnClickListener(this);
        moveG_commit.setOnClickListener(this);

        String prompt="分组列表";
        spinner.setPrompt(prompt);//需要设置spinnerMode
        //1.数据源

        Collection<RosterGroup> groups = XMPPService.conn.getRoster().getGroups();
        Cursor cursor = getContentResolver().query(ContactsProvider.URI_CONTACT, null,
                            "account = ? and belong_to = ? ",
                            new String[]{account,XMPPService.current_account}, null);
        cursor.moveToFirst();
        currentGroup=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.GROUP));
        ToastUtils.myToast(getApplicationContext(),currentGroup);
        list.add(currentGroup);
        cursor.close();

        for (RosterGroup group : groups){
            if (!group.getName().equals(currentGroup))
            list.add(group.getName());
        }

        list.add("新建分组");
        //2.建立adapter,source
        MyAdapter<String> myAdapter=new MyAdapter<>(this,list,R.layout.spinner_adapter);

        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String gname=list.get(position);
//                Toast.makeText(getApplicationContext(),gname+"",Toast.LENGTH_SHORT).show();
                if (gname.equals("新建分组")){
                    new_group.setVisibility(View.VISIBLE);
                    new_group.setText("");
                }else {
                    new_group.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(),"nnnnnnnnnn",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.moveG_toolbar_back:
                finish();
                break;
            case R.id.moveG_commit:
                String selectedGroup=spinner.getSelectedItem().toString();
//                Toast.makeText(getApplicationContext(),"选中："+selectedGroup,Toast.LENGTH_SHORT).show();
                if (selectedGroup.equals("新建分组")){
                    selectedGroup=new_group.getText().toString();
                }
                if (selectedGroup.equals(currentGroup)){
                    ToastUtils.myToast(getApplicationContext(),"你没有进行任何改动!");
                    return;
                }else if(selectedGroup.equals("")||selectedGroup.equals(null)){
                    ToastUtils.myToast(getApplicationContext(),"请输入一个分组名称!");
                    return;
                } else if(XMPPService.checkConnection()){
                    try {
                        RosterEntry entry=XMPPService.conn.getRoster().getEntry(account);
                        RosterGroup cGroup=XMPPService.conn.getRoster().getGroup(currentGroup);
                        RosterGroup toGroup= XMPPService.conn.getRoster().getGroup(selectedGroup);
                        while (cGroup.contains(entry)){
                            cGroup.removeEntry(entry);
                        }
                        if(toGroup!=null){
                            toGroup.addEntry(entry);
                            Log.i("移动成功!",""+currentGroup+"---->"+selectedGroup);
                        }else {
                            RosterGroup newGroup=XMPPService.conn.getRoster().createGroup(selectedGroup);
                            newGroup.addEntry(entry);
                            Log.i("新建分组",""+currentGroup+"---->"+selectedGroup);
                        }
                        //更改数据
                        updateEntry(account,selectedGroup);
                        finish();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }else{
                    ToastUtils.myToast(getApplicationContext(),"网络连接失败，请检查网络!");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 更新联系人
     */
    private void updateEntry(String account, String to_group){
        ContentValues values=new ContentValues();
        values.put(ContactOpenHelper.ContactTable.GROUP, to_group);
        //update
        getContentResolver().update(ContactsProvider.URI_CONTACT,
                values, ContactOpenHelper.ContactTable.ACCOUNT + "= ? and "+
                        ContactOpenHelper.ContactTable.BELONG_TO+ "= ? ",
                new String[]{account,XMPPService.current_account});
    }
}
