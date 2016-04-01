package fragments;


import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import dbhelper.ContactOpenHelper;
import graduationdesign.muguihai.com.v022.ChatActivity;
import graduationdesign.muguihai.com.v022.R;
import provider.ContactsProvider;
import utils.ThreadUtils;

/**
 * A simple {@link Fragment} subclass.
 * 使用V4包的Fragment
 */
public class ContactFragment extends Fragment {

    private ListView listView;
    private CursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_contact, container, false);
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor=adapter.getCursor();
                cursor.moveToPosition(position);

                //获取JID:用于发送消息
                String account=cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                //nickname:用于显示
                String nicknama= cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));

                Intent intent =new Intent(getActivity(),ChatActivity.class);
                intent.putExtra(ChatActivity.CHAT_ACCOUNT,account);
                intent.putExtra(ChatActivity.CHAT_NICKNAME,nicknama);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        //设置adapter
        setOrUpdateAdapter();
    }


    private void initView(View view) {
        listView = (ListView) view.findViewById(R.id.listview);
    }



    /**
     * 设置和更新adapter
     */
    public void setOrUpdateAdapter(){
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
                final Cursor cursor = getActivity().getContentResolver().query(ContactsProvider.URI_CONTACT, null, null, null, null);

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
                                        View view = View.inflate(context, R.layout.item_contact, null);
                                        return view;
                                    }

                                    //数据的设置和显示
                                    @Override
                                    public void bindView(View view, Context context, Cursor cursor) {
//                                TextView tv= (TextView) view;
                                        ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                        TextView tvAccount = (TextView) view.findViewById(R.id.account);
                                        TextView tvNickname = (TextView) view.findViewById(R.id.nickname);

                                        String account = cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.ACCOUNT));
                                        String nickname = cursor.getString(cursor.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));

                                        tvAccount.setText(account);
                                        tvNickname.setText(nickname);
                                    }
                                };

                        listView.setAdapter(adapter);
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
     * 实时监听器，侦听聊天记录数据库数据的改变
     */

    MyContentObserver myContentObserver=new MyContentObserver(new Handler());

    /**
     * 注册监听
     */
    public void regContentObserver(){
        getActivity().getContentResolver().registerContentObserver(ContactsProvider.URI_CONTACT,true,
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
    class MyContentObserver extends ContentObserver{
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


}
