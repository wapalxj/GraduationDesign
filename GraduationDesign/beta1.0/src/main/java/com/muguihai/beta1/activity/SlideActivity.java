package com.muguihai.beta1.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.muguihai.beta1.R;
import com.muguihai.beta1.fragment.ContactFragment;
import com.muguihai.beta1.fragment.MineFragment;
import com.muguihai.beta1.fragment.SessionFragment;
import com.muguihai.beta1.utils.ToolBarUtil;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
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
                mTransaction.replace(R.id.frame,new ContactFragment());
                mMtv_title.setText(toolbar_titles[1]);
                break;
            case 2:
                mTransaction.replace(R.id.frame,new MineFragment());
                mMtv_title.setText(toolbar_titles[2]);
                break;

        }
        mTransaction.commit();
    }
}
