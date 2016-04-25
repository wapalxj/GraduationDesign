package com.muguihai.beta1.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.activity.LoginActivity;
import com.muguihai.beta1.dbhelper.ContactOpenHelper;
import com.muguihai.beta1.dbhelper.PacketOpenHelper;
import com.muguihai.beta1.dbhelper.SmsOpenHelper;
import com.muguihai.beta1.provider.ContactsProvider;
import com.muguihai.beta1.provider.PacketProvider;
import com.muguihai.beta1.provider.SmsProvider;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.PinyinUtil;
import com.muguihai.beta1.utils.ThreadUtils;
import com.muguihai.beta1.utils.ToastUtils;
import com.muguihai.beta1.view.newquickaction.ActionItem;
import com.muguihai.beta1.view.newquickaction.QuickAction;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;


public class MineFragment extends Fragment {
    public static final String ADD_FRIEND="add_friend";
    private static final int DELETE = 0;

    private ListView mListView;
    private CursorAdapter mAdapter;


    public MineFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mine, container, false);
        initView(view);
        return view;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);

    }

    private void initListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView view1 = (TextView) view.findViewById(R.id.name);
                TextView tvState = (TextView) view.findViewById(R.id.state);
                if (view1.getTag().equals(ADD_FRIEND)) {
                    if (tvState.getVisibility()==View.VISIBLE){
                        return;
                    }
                    String account=view1.getText().toString();
                    showAddFriendDialog(account,view);
                }
            }
        });


        ActionItem addItem 	= new ActionItem(DELETE, "删除", getResources().getDrawable(R.drawable.ic_delete));
        final QuickAction mQuickAction 	= new QuickAction(getActivity());
        mQuickAction.addActionItem(addItem);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {

                mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

                    @Override
                    public void onItemClick(QuickAction source, int pos, int actionId) {
                        switch (actionId) {
                            case 0:
                                String packet_id=((TextView) view.findViewById(R.id.packet_id)).getText().toString();
                                //删除信息记录通过(id)
                                getActivity().getContentResolver().delete(
                                        PacketProvider.URI_PACKET,
                                        PacketOpenHelper.Packet_Table._ID+ "=? and "+
                                                PacketOpenHelper.Packet_Table.PACKET_BELONG_TO+"=? ",
                                        new String[]{packet_id,XMPPService.current_account}
                                );
//                                setOrUpdateAdapter();
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

    /**
     * 好友请求dialog
     * @param account
     */
    private void showAddFriendDialog(final String account, final View view) {
        final TextView tvState= (TextView) view.findViewById(R.id.state);
        final String[] acc = {account};
        new AlertDialog.Builder(getActivity())
                .setMessage(account + "请求添加您为好友")
                .setTitle("提示")
                .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 接受请求
                        String nickname=acc[0];
                        acc[0] +="@"+ LoginActivity.SERVICENAME;
                        Presence subscription = new Presence(Presence.Type.subscribed);
                        subscription.setTo(acc[0]);
                        XMPPService.conn.sendPacket(subscription);
                        //插入联系人
                        String account=acc[0];
                        insertEntry(account,nickname);
                        tvState.setVisibility(View.VISIBLE);
                        tvState.setText("已同意");
                        updateState(acc[0],1);
                        setOrUpdateAdapter();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 拒绝请求
                        Presence unsubscription = new Presence(Presence.Type.unsubscribe);
                        acc[0] +="@"+ LoginActivity.SERVICENAME;
                        unsubscription.setTo(acc[0]);
                        XMPPService.conn.sendPacket(unsubscription);
                        tvState.setVisibility(View.VISIBLE);
                        tvState.setText("已拒绝");
                        updateState(acc[0],2);
                        setOrUpdateAdapter();

                    }
                }).show();

    }



    private void initData() {
        //设置adapter
        setOrUpdateAdapter();
    }



    private void init() {
        regPacketObserver();
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.addf_listview);
    }
    /**
     * 设置和更新adapter
     */
    private void setOrUpdateAdapter() {
        //判断adapter是否存在
        if (mAdapter!=null){
            Log.i("MyPacketObserver", "mAdapter存在");
            //刷新adapter
            mAdapter.getCursor().requery();
//            mAdapter.notifyDataSetChanged();
            return;
        }
        Log.i("MyPacketObserver", "创建mAdapter");
        //开启线程，同步roster
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //对应查询记录
                final Cursor cursor = getActivity().getContentResolver().query(PacketProvider.URI_PACKET, null,
                        "packet_belong_to= ?", new String[]{XMPPService.current_account}, null);

                //没有数据
                if (cursor.getCount() <= 0) {
                    return;
                }


                //设置adapter，显示数据
                ThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter =
                                new CursorAdapter(getActivity(), cursor) {
                                    //如果convertView==null,则执行newView,返回一个根视图
                                    @Override
                                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                        View view = View.inflate(context, R.layout.item_packet_subscription, null);
                                        return view;
                                    }
                                    //数据的设置和显示
                                    @Override
                                    public void bindView(View view, Context context, Cursor cursor) {
                                        TextView tvNickname = (TextView) view.findViewById(R.id.name);
                                        TextView packet_id = (TextView) view.findViewById(R.id.packet_id);

                                        tvNickname.setTag(ADD_FRIEND);
//                                        tvNickname.setTag(1,cursor.getString(cursor.getColumnIndex(PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM)));
                                        String nickname  = cursor.getString(cursor.getColumnIndex(PacketOpenHelper.Packet_Table.PACKET_NICKNAME_FROM));
                                        int handle_state  = cursor.getInt(cursor.getColumnIndex(PacketOpenHelper.Packet_Table.HANDLE_STATE));
                                        String _id  = cursor.getString(cursor.getColumnIndex(PacketOpenHelper.Packet_Table._ID));

                                        if (handle_state!=0){
                                            TextView tvState= (TextView) view.findViewById(R.id.state);
                                            tvState.setVisibility(View.VISIBLE);
                                            if (handle_state==1){
                                                tvState.setText("已同意");
                                            }else if (handle_state==2){
                                                tvState.setText("已拒绝");
                                            }
                                        }
                                        packet_id.setText(_id);
                                        tvNickname.setText(nickname);

                                    }
                                };

                        mListView.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        //注销监听
        unRegPacketObserver();
        super.onDestroy();
    }
    /**
     * 实时监听器，侦听packet数据库数据的改变
     */

    MyPacketObserver myPacketObserver=new MyPacketObserver(new Handler());

    /**
     * 注册监听
     */
    public void regPacketObserver(){
        getActivity().getContentResolver().registerContentObserver(PacketProvider.URI_PACKET,true,
                myPacketObserver);
    }
    /**
     * 注销监听
     */
    public void unRegPacketObserver(){
        getActivity().getContentResolver().unregisterContentObserver(myPacketObserver);
    }

    /**
     * 定义监听:数据库数据改变在这里收到通知
     */
    class MyPacketObserver extends ContentObserver {
        public MyPacketObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.i("MyPacketObserver", "监听Packet更新");
            //刷新adapter
            setOrUpdateAdapter();
        }
    }


    /**
     * 插入联系人
     */
    private void insertEntry(String account,String nickname){
        ContentValues values=new ContentValues();
        String pinyinName=PinyinUtil.strToPinyin(nickname);
        String groupName="Friends";
        String belong_to=XMPPService.current_account;

        if (nickname==null||"".equals(nickname)){
            nickname=account.substring(0,account.indexOf("@"));
        }

        values.put(ContactOpenHelper.ContactTable.ACCOUNT,account);
        values.put(ContactOpenHelper.ContactTable.NICKNAME,nickname);
        values.put(ContactOpenHelper.ContactTable.AVATAR, "0");
        values.put(ContactOpenHelper.ContactTable.PINYIN, pinyinName);
        values.put(ContactOpenHelper.ContactTable.GROUP, groupName);
        values.put(ContactOpenHelper.ContactTable.BELONG_TO, belong_to);


        getActivity().getContentResolver().insert(ContactsProvider.URI_CONTACT,values);
    }

    /**
     * 更改处理状态
     */
    private void updateState(String account,int state_id) {
        Log.i("updateState","状态更新成功");
        ContentValues values=new ContentValues();
        int handle_state= state_id;//已处理
        values.put(PacketOpenHelper.Packet_Table.HANDLE_STATE,handle_state);

        int uCount=getActivity().getContentResolver().update(PacketProvider.URI_PACKET,
                values, PacketOpenHelper.Packet_Table.PACKET_ACCOUNT_FROM + "=? and "+PacketOpenHelper.Packet_Table.PACKET_BELONG_TO+"=? "
                , new String[]{account,XMPPService.current_account});

        if (uCount>0){
            Log.i("updateState","状态更新成功");
        }
    }

}
