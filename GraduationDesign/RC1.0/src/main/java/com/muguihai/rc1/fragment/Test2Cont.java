package com.muguihai.rc1.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.muguihai.rc1.R;
import com.muguihai.rc1.activity.ChatActivity;
import com.muguihai.rc1.dbhelper.ContactOpenHelper;
import com.muguihai.rc1.provider.ContactsProvider;
import com.muguihai.rc1.service.XMPPService;
import com.muguihai.rc1.utils.ThreadUtils;
import com.muguihai.rc1.utils.ToastUtils;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vero on 2016/4/17.
 */
public class Test2Cont extends Fragment {
    private ExpandableListView exlistView;

    private List<String> group_list;
    private List<List<String>> item_list;
    private List<List<Integer>> child_list2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("ContactFragment", "onCreate");
        super.onCreate(savedInstanceState);
        init();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test_contact, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initListener();
    }

    private void init() {
        regContentObserver();

    }

    /**
     * listview联系人点击事件
     */
    private void initListener() {
        exlistView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String e_account = ((TextView)v.findViewById(R.id.e_account)).getText().toString();
                String e_nickname = ((TextView)v.findViewById(R.id.e_nickname)).getText().toString();
                String e_signature =((TextView)v.findViewById(R.id.e_signature)).getText().toString();
                //获取JID:用于发送消息
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.CHAT_ACCOUNT, e_account);
                intent.putExtra(ChatActivity.CHAT_NICKNAME, e_nickname);
                intent.putExtra(ChatActivity.CHAT_SIGNATURE, e_signature);
                startActivity(intent);
                return true;
            }
        });
    }

    private void initData() {
        //设置adapter
        setOrUpdateAdapter();
    }


    private void initView(View view) {
        exlistView = (ExpandableListView) view.findViewById(R.id.explist);
    }


    /**
     * 设置和更新adapter
     */
    public void setOrUpdateAdapter() {
        //判断adapter是否存在
//        if (adapter != null) {
//            刷新adapter
//            adapter.getCursor().requery();
//            adapter.notifyDataSetChanged();
//            return;
//        }
        if (XMPPService.checkConnection()){
            //开启线程，同步roster
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    //对应查询记录
                    //数据源
                    Collection<RosterGroup> groups = XMPPService.conn.getRoster().getGroups();
                    group_list = new ArrayList<>();
                    item_list = new ArrayList<>();

                    for (RosterGroup group : groups) {
                        System.out.print("分组名称："+(group.getName()+"----"));
                        group_list.add(group.getName());

                        Collection<RosterEntry> entries =group.getEntries();
                        LinkedList<String> group_list = new LinkedList<>();
                        Cursor cursor = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null,
                                "belong_to = ? and Rgroup= ? ",
                                new String[]{XMPPService.current_account,group.getName()}, null);
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()){
                            String account = cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                            group_list.add(account);
                            cursor.moveToNext();
                        }
                        item_list.add(group_list);
                    }

                    List<Integer> tmp_list = new ArrayList<>();
                    tmp_list.add(R.mipmap.ic_launcher);
                    tmp_list.add(R.mipmap.ic_launcher);
                    tmp_list.add(R.mipmap.ic_launcher);

                    child_list2 = new ArrayList<>();
                    child_list2.add(tmp_list);
                    child_list2.add(tmp_list);
                    child_list2.add(tmp_list);

                    //设置adapter，显示数据
                    ThreadUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            E2Adapter adapter=new E2Adapter();
                            exlistView.setAdapter(adapter);
                        }
                    });
                }
            });
        }else{
            ToastUtils.myToast(getActivity(),"网络连接失败，请检查网络!");
            return;
        }

    }


    @Override
    public void onDestroy() {
        //注销监听
        unRegContentObserver();
        super.onDestroy();
    }

    /**
     * 实时监听器，侦听联系人数据库数据的改变
     */

    MyContentObserver myContentObserver = new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void regContentObserver() {
        getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT, true,
                myContentObserver);
    }

    /**
     * 注销监听
     */
    public void unRegContentObserver() {
        getActivity().getContentResolver().unregisterContentObserver(myContentObserver);
    }

    /**
     * 定义监听:数据库数据改变在这里收到通知
     */
    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //刷新adapter
            Log.i("MyContentObserver", "Contact1111111111111");
            setOrUpdateAdapter();
        }
    }


    /**
     * adapter
     */

    class E2Adapter extends BaseExpandableListAdapter {
        public E2Adapter() {

        }

        @Override
        public int getGroupCount() {
            return group_list.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return item_list.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return group_list.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return item_list.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            GroupHolder groupHolder = null;
            if (convertView == null) {
                convertView = (View) getActivity().getLayoutInflater().from(getActivity()).inflate(
                        R.layout.expendlist_group, null);
                groupHolder = new GroupHolder();
                groupHolder.group_name = (TextView) convertView.findViewById(R.id.group);
                groupHolder.nums = (TextView) convertView.findViewById(R.id.nums);
                convertView.setTag(groupHolder);
            } else {
                groupHolder = (GroupHolder) convertView.getTag();
            }

            groupHolder.group_name.setText(group_list.get(groupPosition));
            groupHolder.nums.setText(String.valueOf(getChildrenCount(groupPosition)));
            return convertView;
        }


        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            ItemHolder itemHolder = null;
            if (convertView == null) {
                convertView = (View) getActivity().getLayoutInflater().from(getActivity()).inflate(
                        R.layout.expendlist_item, null);
                itemHolder = new ItemHolder();
                itemHolder.account= (TextView) convertView.findViewById(R.id.e_account);
                itemHolder.nickname = (TextView) convertView.findViewById(R.id.e_nickname);
                itemHolder.head = (ImageView) convertView.findViewById(R.id.head);
                itemHolder.online_state= (TextView) convertView.findViewById(R.id.e_online_state);
                itemHolder.signature= (TextView) convertView.findViewById(R.id.e_signature);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemHolder) convertView.getTag();
            }


            String account=item_list.get(groupPosition).get(childPosition);
            if (XMPPService.checkConnection()){
                Cursor cursor = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null,
                        "account = ? and belong_to= ? ",
                        new String[]{account,XMPPService.current_account}, null);
                cursor.moveToFirst();

                String presence=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.PRESENCE));
                String sign=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.SIGNATURE));
                String nickname=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
                cursor.close();
                itemHolder.account.setText(account);
                itemHolder.nickname.setText(nickname);
                itemHolder.signature.setText(sign==null?"什么都没有":sign);

    //        itemHolder.head.setBackgroundResource(item_list2.get(1).get(1));
//                itemHolder.head.setBackgroundResource(R.mipmap.ic_launcher);
                if (presence.equals(Presence.Type.available.toString())){
                    itemHolder.online_state.setText("[在线]");
                    itemHolder.online_state.setTextColor(Color.BLUE);
                }else {
                    itemHolder.online_state.setText("[离线]");
                    itemHolder.online_state.setTextColor(Color.GRAY);
                }
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    class GroupHolder {
        public TextView group_name;
        public TextView nums;
    }

    class ItemHolder {
        public ImageView head;
        public TextView account;
        public TextView nickname;
        public TextView online_state;
        public TextView signature;
    }


}



