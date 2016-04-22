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
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.muguihai.beta1.R;
import com.muguihai.beta1.activity.ChatActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.SessionOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.SessionProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.ThreadUtils;
import com.muguihai.beta1.utils.ToastUtils;
import com.muguihai.beta1.view.quickaction.ActionItem;
import com.muguihai.beta1.view.quickaction.QuickAction;


/**
 * 使用V4包的Fragmen
 */

public class SessionFragment extends Fragment {
    private ListView mListview;
    private CursorAdapter adapter;

    public SessionFragment() {
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
        View view=inflater.inflate(R.layout.fragment_session, container, false);
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
        mListview= (ListView) view.findViewById(R.id.session_listview);
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
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor=adapter.getCursor();
                cursor.moveToPosition(position);

                //获取JID:用于发送消息
                String account=cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.SESSION_ACCOUNT));
                //nickname:用于显示
                String nickname=cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.SESSION_NICKNAME));

                Intent intent =new Intent(getActivity(),ChatActivity.class);
                intent.putExtra(ChatActivity.CHAT_ACCOUNT,account);
                intent.putExtra(ChatActivity.CHAT_NICKNAME,nickname);


                startActivity(intent);
            }
        });

        mListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                Cursor cursor=adapter.getCursor();
                cursor.moveToPosition(position);

                //获取JID:用于发送消息
                final String account=cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                //nickname:用于显示
                final String nickname= getNicknameByAccount(account);

                QuickAction quickAction = new QuickAction(getActivity(), QuickAction.HORIZONTAL);
                quickAction.addActionItem(new ActionItem(0, "打开"));
                quickAction.addActionItem(new ActionItem(1,"删除"));
                quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

                            @Override
                            public void onItemClick(QuickAction source, int pos,
                                                    int actionId) {
                                switch (actionId) {
                                    case 0:
                                        Intent intent =new Intent(getActivity(),ChatActivity.class);
                                        intent.putExtra(ChatActivity.CHAT_ACCOUNT,account);
                                        intent.putExtra(ChatActivity.CHAT_NICKNAME,nickname);
                                        startActivity(intent);
                                        break;
                                    case 1:
//                                        TextView tvAccount= (TextView) view.findViewById(R.id.account_session);
//                                        String account=tvAccount.getText().toString();
                                        ToastUtils.myToast(getActivity(),account);
                                        removeSession(account);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                quickAction.show(mListview.getChildAt(position));
                return false;
            }
        });
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
//                //对应查询记录
//                final Cursor cursor = getActivity().getContentResolver().query(SmsProvider.URI_SESSION, null, null,
//                        new String[]{XMPPService.current_account, XMPPService.current_account,XMPPService.current_account}, null);

                //对应查询记录
                final Cursor cursor = getActivity().getContentResolver().query(SessionProvider.URI_SESSION, null,
                        "session_belong_to= ?",
                        new String[]{XMPPService.current_account}, null);


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
                                        View view = View.inflate(context, R.layout.item_session, null);
                                        return view;
                                    }
                                    //数据的设置和显示
                                    @Override
                                    public void bindView(View view, Context context, Cursor cursor) {
                                        ImageView ivHead = (ImageView) view.findViewById(R.id.head);
                                        TextView tvaccount = (TextView) view.findViewById(R.id.account_session);
                                        TextView tvBody = (TextView) view.findViewById(R.id.body_session);
                                        TextView tvNickname = (TextView) view.findViewById(R.id.nickname_session);

//                                        String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
//                                        String account = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));

                                        String body = cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.BODY));
                                        String account = cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.SESSION_ACCOUNT));
                                        String nickname = cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.SESSION_NICKNAME));
                                        tvaccount.setText(account);
//                                        String nickname=getNicknameByAccount(account);
                                        tvBody.setText(body);
                                        tvNickname.setText(nickname);
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
            Log.i("Session","Session变化");
            setOrUpdateAdapter();
        }
    }

    private void removeSession(String account){
        // 删除会话
        getActivity().getContentResolver().delete(
                SessionProvider.URI_SESSION,
                SessionOpenHelper.SessionTable.SESSION_ACCOUNT+"=? and "+SessionOpenHelper.SessionTable.SESSION_BELONG_TO+ "=?"
                ,
                new String[]{account,XMPPService.current_account}
        );
        setOrUpdateAdapter();
    }


}
