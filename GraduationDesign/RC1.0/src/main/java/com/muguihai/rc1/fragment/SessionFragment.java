package com.muguihai.rc1.fragment;

import android.app.Fragment;
import android.content.ContentValues;
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

import com.muguihai.rc1.R;
import com.muguihai.rc1.activity.ChatActivity;
import com.muguihai.rc1.activity.SlideActivity;
import com.muguihai.rc1.dbhelper.ContactOpenHelper;
import com.muguihai.rc1.dbhelper.SessionOpenHelper;
import com.muguihai.rc1.dbhelper.SmsOpenHelper;
import com.muguihai.rc1.provider.ContactsProvider;
import com.muguihai.rc1.provider.SessionProvider;
import com.muguihai.rc1.provider.SmsProvider;
import com.muguihai.rc1.service.XMPPService;
import com.muguihai.rc1.utils.ThreadUtils;
import com.muguihai.rc1.utils.ToastUtils;
import com.muguihai.rc1.view.newquickaction.ActionItem;
import com.muguihai.rc1.view.newquickaction.QuickAction;


/**
 * 使用V4包的Fragmen
 */

public class SessionFragment extends Fragment {
    private ListView mListview;
    private CursorAdapter adapter;

    private static int OPEN=0;
    private static int DELETE=1;
    private static int IGNORE=2;

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

                ActionItem addItem 		= new ActionItem(OPEN, "打开", getResources().getDrawable(R.drawable.ic_open));
                ActionItem acceptItem 	= new ActionItem(DELETE, "删除", getResources().getDrawable(R.drawable.ic_delete));
                ActionItem ignoreItem 	= new ActionItem(IGNORE, "全部忽略", getResources().getDrawable(R.drawable.ic_delete));
                final QuickAction mQuickAction 	= new QuickAction(getActivity());

                mQuickAction.addActionItem(addItem);
                mQuickAction.addActionItem(acceptItem);
                mQuickAction.addActionItem(ignoreItem);

                mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

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
                                        ToastUtils.myToast(getActivity(),account);
                                        removeSession(account);
                                        break;
                                    case 2:
                                        ToastUtils.myToast(getActivity(),"全部忽略");
                                        updateMessage(account);
                                        TextView session_notify= (TextView) view.findViewById(R.id.session_notify);
                                        session_notify.setText(String.valueOf(0));
                                        session_notify.setVisibility(View.GONE);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                mQuickAction.show(view);
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
                                        TextView session_notify= (TextView) view.findViewById(R.id.session_notify);

                                        String body = cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.BODY));
                                        String account = cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.SESSION_ACCOUNT));
                                        String nickname = cursor.getString(cursor.getColumnIndex(SessionOpenHelper.SessionTable.SESSION_NICKNAME));
                                        tvaccount.setText(account);
                                        tvBody.setText(body);
                                        tvNickname.setText(nickname);

                                        Cursor sCursor=getActivity().getContentResolver().query(SmsProvider.URI_SMS, null,
                                                "read_status = 0 and session_account = ? and session_belong_to= ?",
                                                new String[]{account,XMPPService.current_account}, null);
                                        int counts=sCursor.getCount();
                                        session_notify.setText(String.valueOf(counts));
                                        sCursor.close();

                                        //设置消息提示的可见
                                        if (Integer.parseInt(session_notify.getText().toString())<=0){
                                            session_notify.setText(String.valueOf(0));
                                            session_notify.setVisibility(View.GONE);
                                        }else {
                                            session_notify.setVisibility(View.VISIBLE);
                                        }
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

        if (nickname==null||"".equals(nickname)){
            nickname=account.substring(0,account.indexOf("@"));
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


    /**
     * 更改已读状态
     */
    private void updateMessage(String account) {
        ContentValues values=new ContentValues();
        values.put(SmsOpenHelper.SmsTable.READ_STATUS,1);//设置已读

        //先update在insert
        int uCount=getActivity().getContentResolver().update(SmsProvider.URI_SMS,
                values, SmsOpenHelper.SmsTable.SESSION_ACCOUNT+ "= ? and "
                        +SmsOpenHelper.SmsTable.READ_STATUS+"= 0 and "
                        +SmsOpenHelper.SmsTable.SESSION_BELONG_TO+"= ? ",
                new String[]{account,XMPPService.current_account});

        if (uCount>0){
            //发送广播
            Intent session=new Intent(SlideActivity.XMPPReceiver.SESSION_ACTION);
            session.putExtra(SlideActivity.XMPPReceiver.SESSION,2);
            session.putExtra(SlideActivity.XMPPReceiver.SESSION_ALL,uCount);
            getActivity().sendBroadcast(session);
        }
    }


}
