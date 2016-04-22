package com.muguihai.beta1.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.activity.ChatActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.GroupProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.ThreadUtils;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
                    ArrayList<String> group_list = new ArrayList<>();
//                    for (RosterEntry entry : entries) {
//                        System.out.print(entry.getName() + "----");
//                        group_list.add(entry.getUser());
//                    }
//                    item_list.add(group_list);
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

//                ArrayList<String> group_list1 = new ArrayList<>();
//                group_list1.add("CC");
//                group_list1.add("DD");
//                group_list1.add("EE");
//                ArrayList<String> group_list2 = new ArrayList<>();
//                group_list2.add("CC");
//                group_list2.add("DD");
//                group_list2.add("EE");
//                ArrayList<String> group_list3 = new ArrayList<>();
//                group_list3.add("GG");
//                group_list3.add("HH");
//                ArrayList<String> group_list4 = new ArrayList<>();
//                group_list4.add("GG");
//                group_list4.add("CC");
//                group_list4.add("DD");
//                group_list4.add("EE");
//
//                item_list = new ArrayList<>();
//                item_list.add(group_list1);
//                item_list.add(group_list2);
//                item_list.add(group_list3);
//                item_list.add(group_list4);


                List<Integer> tmp_list = new ArrayList<>();
                tmp_list.add(R.mipmap.ic_launcher);
                tmp_list.add(R.mipmap.ic_launcher);
                tmp_list.add(R.mipmap.ic_launcher);

                child_list2 = new ArrayList<>();
                child_list2.add(tmp_list);
                child_list2.add(tmp_list);
                child_list2.add(tmp_list);



//                //没有数据
//                if (cursor.getCount() <= 0) {
//                    return;
//                }

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
            groupHolder.nums.setText(String.valueOf(getChildrenCount(groupPosition)+"/"+String.valueOf(getChildrenCount(groupPosition))));
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
            RosterEntry entry;
            if (XMPPService.checkConnection()){
                entry=XMPPService.conn.getRoster().getEntry(account);
                itemHolder.account.setText(account);
                itemHolder.nickname.setText(entry.getName()==null ? account.substring(0,account.indexOf("@")) :entry.getName());

    //        itemHolder.head.setBackgroundResource(item_list2.get(1).get(1));
                itemHolder.head.setBackgroundResource(R.mipmap.ic_launcher);
                itemHolder.online_state.setText("[离线]");
                itemHolder.signature.setText("我用PHP开发安卓");
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



