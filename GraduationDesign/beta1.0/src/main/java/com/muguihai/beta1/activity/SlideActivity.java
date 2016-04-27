package com.muguihai.beta1.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.muguihai.beta1.R;
import com.muguihai.beta1.fragment.ContactFragment;
import com.muguihai.beta1.fragment.MineFragment;
import com.muguihai.beta1.fragment.Sess2Fragment;
import com.muguihai.beta1.fragment.SessionFragment;
import com.muguihai.beta1.fragment.Test2Cont;
import com.muguihai.beta1.service.PacketService;
import com.muguihai.beta1.service.XMPPService;
import com.muguihai.beta1.utils.ToastUtils;
import com.muguihai.beta1.utils.ToolBarUtil;
import com.muguihai.beta1.view.popupwindow.MyPopupwindow;
import com.muguihai.beta1.view.slidemenu.MyLinearLayout;
import com.muguihai.beta1.view.slidemenu.SlideMenu;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class SlideActivity extends AppCompatActivity implements ToolBarUtil.OnToolBarClickListener {
    private ListView mMenu_listview;
    private SlideMenu slideMenu;
    private ImageView main_head;
    private MyLinearLayout myLinearLayout;

    private TextView mMtv_title;
    private String[] toolbar_titles;
    private LinearLayout mLlBottom;
    private ToolBarUtil mToolBarUtil;
    private FragmentManager mManager;
    private FragmentTransaction mTransaction;

    //popwindow
    private PopupWindow mPopupWindow;
    //receiver
    private BroadcastReceiver mReceiver;
    private SharedPreferences notifications_sp;
    private SharedPreferences.Editor notifications_editor;
    private int packet_counts;
    private int session_counts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
        //
        notifications_sp=getSharedPreferences("notifications",MODE_PRIVATE);
        packet_counts=notifications_sp.getInt("packet",0);
        session_counts=notifications_sp.getInt("session",0);
        notifications_editor=notifications_sp.edit();
        //注册广播
        IntentFilter filter=new IntentFilter();
        filter.addAction(XMPPReceiver.MINE_ACTION);
        filter.addAction(XMPPReceiver.SESSION_ACTION);
        mReceiver=new XMPPReceiver();
        registerReceiver(mReceiver,filter);

        initView();
    }

    private void initView(){
        //标题
        mMtv_title= (TextView) findViewById(R.id.main_tv_title);
        mLlBottom= (LinearLayout) findViewById(R.id.main_bottom);
        //bottom
        mToolBarUtil = new ToolBarUtil();
        //toolbar标题
        toolbar_titles=new String []{"消息","联系人","我的"};
        //tooBar图标
        int[] icons = {R.drawable.selector_icon_msg, R.drawable.selector_icon_contact, R.drawable.selector_icon_mine};
        mToolBarUtil.initTooBar(mLlBottom, icons,toolbar_titles);
        //显示消息数
        mToolBarUtil.toolBarNotification(0,session_counts);
        mToolBarUtil.toolBarNotification(1,0);
        mToolBarUtil.toolBarNotification(2,packet_counts);
        //设置默认选中会话
        mToolBarUtil.toolBarSelect(0);
        mToolBarUtil.setmOnToolBarClickListener(this);

        mManager=getFragmentManager();
        mTransaction = mManager.beginTransaction();
        mTransaction.replace(R.id.frame,new SessionFragment());
        mTransaction.commit();
        //

        main_head = (ImageView) findViewById(R.id.iv_head);
        String [] menu={"a1111","a2222","a3333","a4444","a55","a666","a777","a8888","a55","a666","a777","a8888"};
        mMenu_listview= (ListView) findViewById(R.id.menu_listview);
        mMenu_listview.setAdapter(
                new ArrayAdapter<>(
                        getApplicationContext(),
                        android.R.layout.simple_list_item_1,menu
                )
        );

        slideMenu= (SlideMenu) findViewById(R.id.slideMenu);
        slideMenu.setOnDragStateChangeListener(new SlideMenu.OnDragStateChangeListener() {
            @Override
            public void onOpen() {
                Log.i("slideMenu","onOpen");
            }

            @Override
            public void onClose() {
                Log.i("slideMenu","onClose");
                //头像抖动
                ViewPropertyAnimator.animate(main_head)
                        .translationXBy(15)
                        .setInterpolator(new CycleInterpolator(4))
                        .setDuration(500)
                        .start();
            }

            @Override
            public void onDraging(float fraction) {
                Log.i("slideMenu","onDraging"+fraction);
                //main头像透明度变化--->通过接口回调处理
                main_head.setAlpha(1 - fraction);
            }
        });

        main_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideMenu.openMenu();
            }
        });

        //拦截并消耗事件
        myLinearLayout= (MyLinearLayout) findViewById(R.id.my_layout);
        myLinearLayout.setSlideMenu(slideMenu);

        //pop
        initPop();

    }

    //选项卡点击
    @Override
    public void onToolBarClick(int position) {
        mToolBarUtil.toolBarSelect(position);
        mTransaction = mManager.beginTransaction();
        switch (position){
            case 0:
                mTransaction.replace(R.id.frame,new SessionFragment());
                mMtv_title.setText(toolbar_titles[0]);
                break;
            case 1:
                mTransaction.replace(R.id.frame,new Test2Cont());
                mMtv_title.setText(toolbar_titles[1]);
                break;
            case 2:
                mTransaction.replace(R.id.frame,new MineFragment());
                mMtv_title.setText(toolbar_titles[2]);
                break;

        }
        mTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        XMPPService.conn.disconnect();
        //关闭IMService
        Intent intent =new Intent(getApplicationContext(),XMPPService.class);
        stopService(intent);


        //XMPPService
        Intent intent2 =new Intent(getApplicationContext(), PacketService.class);
        stopService(intent2);

        //注销广播
        unregisterReceiver(mReceiver);

        mReceiver=null;
        XMPPService.current_account=null;
        XMPPService.conn=null;
        Log.i("close","XMPPService---------PacketService");
        super.onDestroy();
    }

    /**
     * popw
     */
    private  void initPop(){
        final TextView tv_add= (TextView) findViewById(R.id.tv_add);
        MyPopupwindow pop=new MyPopupwindow(SlideActivity.this);
        mPopupWindow=pop.getPwMyPopWindow();
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.myToast(getApplicationContext(),"pop!");
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();// 关闭
                } else {
                    mPopupWindow.showAsDropDown(tv_add);// 显示
                }

            }
        });
    }

    /**
     * 广播接收器
     */
    public class XMPPReceiver extends BroadcastReceiver {
        public static final String MINE="mine";
        public static final String MINE_ACTION="ynu.mgh.mine_action";

        public static final String SESSION="session";
        public static final String SESSION_ACTION="ynu.mgh.session_action";
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case MINE_ACTION:
                    if(intent.getExtras().getInt(MINE)==0){//未处理，消息+1
                        packet_counts++;
                        notifications_editor.putInt("packet",packet_counts);
                        notifications_editor.commit();
                        mToolBarUtil.toolBarNotification(2,packet_counts);
//                        ToastUtils.myToast(context,"mine_action+1");
                    }else {//处理，消息-1
//                        ToastUtils.myToast(context,"mine_action-1");
                        packet_counts--;
                        if (packet_counts<0){
                            packet_counts=0;
                        }
                        notifications_editor.putInt("packet",packet_counts);
                        notifications_editor.commit();
                        mToolBarUtil.toolBarNotification(2,packet_counts);
                    }
                    break;
                case SESSION_ACTION:
                    if(intent.getExtras().getInt(SESSION)==0){//未处理，消息+1
                        session_counts++;
                        notifications_editor.putInt("session",session_counts);
                        notifications_editor.commit();
                        mToolBarUtil.toolBarNotification(0,session_counts);
//                        ToastUtils.myToast(context,"session_counts+1");
                    }else {//处理，消息-1
//                        ToastUtils.myToast(context,"session_counts-1");
                        session_counts--;
                        if (session_counts<0){
                            session_counts=0;
                        }
                        notifications_editor.putInt("session",session_counts);
                        notifications_editor.commit();
                        mToolBarUtil.toolBarNotification(0,session_counts);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
