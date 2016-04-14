package com.muguihai.beta1.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CursorAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.activity.ChatActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.ThreadUtils;

import org.jivesoftware.smack.RosterGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestContactFragment extends Fragment {
    private ExpandableListView exlistView;
    private SimpleCursorTreeAdapter adapter;

    private List<String> group_list;
    private List<List<String>> item_list;
    private List<List<Integer>> item_list2;

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
        View view=inflater.inflate(R.layout.fragment_test_contact, container, false);
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
//        regContentObserver();
    }

    /**
     * listview联系人点击事件
     */
    private void initListener() {
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Cursor cursor=adapter.getCursor();
//                cursor.moveToPosition(position);
//
//                //获取JID:用于发送消息
//                String account=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
//                //nickname:用于显示
//                String nickname= cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
//
//                Intent intent =new Intent(getActivity(),ChatActivity.class);
//                intent.putExtra(ChatActivity.CHAT_ACCOUNT,account);
//                intent.putExtra(ChatActivity.CHAT_NICKNAME,nickname);
//                startActivity(intent);
//            }
//        });
    }

    private void initData() {
        //设置adapter
        setOrUpdateAdapter();
    }


    private void initView(View view) {
//        listView = (ListView) view.findViewById(R.id.listview);
        exlistView= (ExpandableListView) view.findViewById(R.id.explist);
    }



    /**
     * 设置和更新adapter
     */
    public void setOrUpdateAdapter(){
        //判断adapter是否存在
        if (adapter!=null){
            //刷新adapter
//            adapter.getCursor().requery();
            adapter.notifyDataSetChanged();
            return;
        }

        //开启线程，同步roster
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //对应查询记录
                final Cursor cursor = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null,
                        "belong_to = ?",
                        new String[]{XMPPService.current_account}, null);

//                //没有数据
                if (cursor.getCount() <= 0) {
                    return;
                }

                //设置adapter，显示数据
                ThreadUtils.runInUIThread(new Runnable() {

                    @Override
                    public void run() {

                        adapter=new SimpleCursorTreeAdapter(
                                getActivity(),
                                cursor,
                                R.layout.expendlist_group,
                                new String[]{ContactOpenHelper.ContactTable.ACCOUNT},
                                new int[]{R.id.group},
                                R.layout.expendlist_item,
                                new String[]{ContactOpenHelper.ContactTable.ACCOUNT},
                                new int[]{android.R.id.text1}
                        ) {
                            @Override
                            public void onGroupCollapsed(int groupPosition) {
                                Log.i("gggggggggggg",""+groupPosition);
//                                super.onGroupCollapsed(groupPosition);//这行注释掉，不然会GG
                            }

                            @Override
                            public int getGroupCount() {
                                Collection<RosterGroup> groups = XMPPService.conn.getRoster().getGroups();
                                return groups.size();
                            }

                            @Override
                            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                                View view=View.inflate(getActivity(),R.layout.expendlist_group,null);
                                TextView groupname = (TextView) view.findViewById(R.id.group);
                                groupname.setText("Friends");
                                return view;
                            }

                            @Override
                            protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
                                TextView groupname = (TextView) view.findViewById(R.id.group);
                                groupname.setText("Friends");
                            }

                            @Override
                            protected Cursor getChildrenCursor(Cursor groupCursor) {
                                return groupCursor;
                            }

                            @Override
                            protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
                                super.bindChildView(view, context, cursor, isLastChild);
                                ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                TextView tvAccount = (TextView) view.findViewById(R.id.e_nickname);
                                TextView tvNickname = (TextView) view.findViewById(R.id.e_signature);

                                String account = cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                                String nickname = cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));

                                tvAccount.setText(account);
                                tvNickname.setText(nickname);
                            }
                        };
                        exlistView.setAdapter(adapter);
//
                    }
                });
            }
        });
    }

//    @Override
//    public void onDestroy() {
//        //注销监听
//        unRegContentObserver();
//        super.onDestroy();
//    }
//
//    /**
//     * 实时监听器，侦听联系人数据库数据的改变
//     */
//
//    MyContentObserver myContentObserver=new MyContentObserver(new Handler());
//
//    /**
//     * 注册监听
//     */
//    public void regContentObserver(){
//        getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT,true,
//                myContentObserver);
//    }
//    /**
//     * 注销监听
//     */
//    public void unRegContentObserver(){
//        getActivity().getContentResolver().unregisterContentObserver(myContentObserver);
//    }
//
//    /**
//     * 定义监听:数据库数据改变在这里收到通知
//     */
//    class MyContentObserver extends ContentObserver {
//        public MyContentObserver(Handler handler) {
//            super(handler);
//        }
//
//        @Override
//        public void onChange(boolean selfChange) {
//            super.onChange(selfChange);
//            //刷新adapter
//            setOrUpdateAdapter();
//        }
//    }


    /**
     * adapter
     */


    class E2Adapter extends BaseExpandableListAdapter {



        //        public E2Adapter(Context context,LayoutInflater inflater,List<String> group_list,List<List<String>> item_list,List<List<Integer>> item_list2) {
//            this.context = context;
//            this.inflater=inflater;
//            this.group_list=group_list;
//            this.item_list=item_list;
//            this.item_list2=item_list2;
//        }
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
                itemHolder.nickname = (TextView) convertView.findViewById(R.id.e_nickname);
                itemHolder.head = (ImageView) convertView.findViewById(R.id.head);
                itemHolder.online_state= (TextView) convertView.findViewById(R.id.e_online_state);
                itemHolder.signature= (TextView) convertView.findViewById(R.id.e_signature);
                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ItemHolder) convertView.getTag();
            }
            itemHolder.nickname.setText("vero");
//        itemHolder.head.setBackgroundResource(item_list2.get(1).get(1));
            itemHolder.head.setBackgroundResource(R.mipmap.ic_launcher);
            itemHolder.online_state.setText("[离线]");
            itemHolder.signature.setText("我用PHP开发安卓");
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
        public TextView nickname;
        public TextView online_state;
        public TextView signature;
    }
}



