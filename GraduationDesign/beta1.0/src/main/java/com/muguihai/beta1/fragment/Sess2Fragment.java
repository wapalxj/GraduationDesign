package com.muguihai.beta1.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.muguihai.beta1.R;
import com.muguihai.beta1.activity.ChatActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.ThreadUtils;
import com.muguihai.beta1.view.swipe_delete.SwipeLayout;
import com.muguihai.beta1.view.swipe_delete.SwipeLayoutManager;


public class Sess2Fragment extends android.app.Fragment {
    private ListView mListview;
    private CursorAdapter adapter;

    public Sess2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("SessionFragment","onCreate");
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("SessionFragment","onCreateView");
        View view=inflater.inflate(R.layout.fragment_sess2, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("SessionFragment","onActivityCreated");
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    private void initView(View view) {
        mListview= (ListView) view.findViewById(R.id.swip_listview);
    }
    private void init() {
        regContentObserver();
    }
    private void initData() {
        setOrUpdateAdapter();
    }
    /**
     * listview联系人点击事件
     */
    private void initListener() {
//        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Cursor cursor=adapter.getCursor();
//                cursor.moveToPosition(position);
//
//                //获取JID:用于发送消息
//                String account=cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
//                //nickname:用于显示
//                String nickname= getNicknameByAccount(account);
//
//                Intent intent =new Intent(getActivity(),ChatActivity.class);
//                intent.putExtra(ChatActivity.CHAT_ACCOUNT,account);
//                intent.putExtra(ChatActivity.CHAT_NICKNAME,nickname);
//                startActivity(intent);
//            }
//        });
    }

    @Override
    public void onDestroy() {
        unRegContentObserver();
        super.onDestroy();
    }

    /**
     * 设置和更新adapter
     */
    private void setOrUpdateAdapter() {
        //判断adapter是否存在
        if (adapter!=null){
            //刷新adapter
            adapter.getCursor().requery();
            return;
        }

        //开启线程，同步roster
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //对应查询记录
                final Cursor cursor = getActivity().getContentResolver().query(SmsProvider.URI_SESSION, null, null,
                        new String[]{XMPPService.current_account, XMPPService.current_account,XMPPService.current_account}, null);

                //没有数据
                if (cursor.getCount() <= 0) {
                    return;
                }
                //设置adapter，显示数据
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter =
                                new CursorAdapter(getActivity(), cursor) {
                                    //如果convertView==null,则执行newView,返回一个根视图
                                    @Override
                                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//                                TextView tv=new TextView(context);
                                        View view = View.inflate(context, R.layout.swipe_adapter_list, null);
                                        return view;
                                    }
                                    //数据的设置和显示
                                    @Override
                                    public void bindView(View view, Context context, Cursor cursor) {
                                        TextView tv_delete;
                                        SwipeLayout swipeLayout;
                                        tv_delete= (TextView) view.findViewById(R.id.tv_delete);
                                        swipeLayout= (SwipeLayout) view.findViewById(R.id.swipelayout);

                                        ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                        TextView tvBody = (TextView) view.findViewById(R.id.body);
                                        TextView tvNickname = (TextView) view.findViewById(R.id.nickname);

                                        String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                                        String account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));

                                        String nickname=getNicknameByAccount(account);
                                        tvBody.setText(body);
                                        tvNickname.setText(nickname);
                                        //SwipeChangeListener
                                        swipeLayout.setTag(cursor.getPosition());
                                        swipeLayout.setOnSwipeChangeListener(new SwipeLayout.OnSwipeChangeListener() {
                                            @Override
                                            public void onOpen(Object tag) {
                                                Toast.makeText(getActivity(), "第"+(Integer) tag+"个swipe被打开了",Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onClose(Object tag) {
                                                Toast.makeText(getActivity(), "第"+(Integer) tag+"个swipe被关闭了",Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        tv_delete.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                        //                    list.remove(position);
                                                SwipeLayoutManager.getInstance().closeCurrentLayout();
                                                notifyDataSetChanged();
                                            }
                                        });
                                    }
                                };
                        mListview.setAdapter(adapter);
                    }
                });
            }
        });
    }

    //获取nickname
    public String getNicknameByAccount(String account){
        String nickname=null;
        Cursor c=getActivity().getContentResolver().query(
                ContactsProvider.URI_CONTACT,null,
                ContactOpenHelper.ContactTable.ACCOUNT+"= ? ",
                new String[]{account},null
        );

        if (c.getCount()>0){//渠道数据
            c.moveToFirst();
            nickname=c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
        }
        return nickname;
    }

    /**
     * 实时监听器，侦听聊天记录数据库数据的改变
     */
    MyContentObserver myContentObserver=new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void regContentObserver(){
        getActivity().getContentResolver().registerContentObserver(SmsProvider.URI_SMS,true,
                myContentObserver);
    }
    /**
     * 注销监听
     */
    public void unRegContentObserver(){
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
            setOrUpdateAdapter();
        }
    }



    class MyAdapter extends BaseAdapter {
        Cursor cursor;

        public MyAdapter(Cursor cursor){
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView==null){
                convertView=View.inflate(getActivity(),R.layout.swipe_adapter_list,null);
            }


            ViewHolder holder=ViewHolder.getHolder(convertView);

            String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
            String account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));

            String nickname=getNicknameByAccount(account);
            holder.tvBody.setText(body);
            holder.tvNickname.setText(nickname);


            //SwipeChangeListener
            holder.swipeLayout.setTag(position);
            holder.swipeLayout.setOnSwipeChangeListener(new SwipeLayout.OnSwipeChangeListener() {
                @Override
                public void onOpen(Object tag) {
                    Toast.makeText(getActivity(), "第"+(Integer) tag+"个swipe被打开了",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onClose(Object tag) {
                    Toast.makeText(getActivity(), "第"+(Integer) tag+"个swipe被关闭了",Toast.LENGTH_SHORT).show();
                }
            });

            holder.tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    list.remove(position);
                    SwipeLayoutManager.getInstance().closeCurrentLayout();
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }

    static class ViewHolder{
        TextView tv_delete;
        SwipeLayout swipeLayout;

        ImageView tvHead;
        TextView tvBody;
        TextView tvNickname;


        public ViewHolder(View convertView){
            tv_delete= (TextView) convertView.findViewById(R.id.tv_delete);
            swipeLayout= (SwipeLayout) convertView.findViewById(R.id.swipelayout);
            tvHead = (ImageView) convertView.findViewById(R.id.head);
            tvBody = (TextView) convertView.findViewById(R.id.body);
            tvNickname = (TextView) convertView.findViewById(R.id.nickname);
        }

        public static ViewHolder getHolder(View convertView){
            ViewHolder holder= (ViewHolder) convertView.getTag();
            if (holder==null){
                holder=new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            return holder;
        }
    }


}
