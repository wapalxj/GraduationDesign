package graduationdesign.muguihai.com.v022;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fragments.ContactFragment;
import fragments.MineFragment;
import fragments.SessionFragment;
import service.IMService;
import service.PushService;
import utils.ToolBarUtil;

public class MainActivity extends AppCompatActivity {

    //注解的方式寻找控件
    @InjectView(R.id.main_bottom)
    LinearLayout mLlBottom;
    @InjectView(R.id.main_tv_title)
    TextView mTvTitle;
    @InjectView(R.id.main_viewpager)
    ViewPager mViewPager;

    private List<Fragment> mFragments = new ArrayList<>();
    private ToolBarUtil mToolBarUtil;
    private String[] mTitle = new String[]{"会话", "联系人", "我的"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        init();
        initPagerListener();//滑动
    }

    public void init() {
//        regReceiver();
        //viewPager---->view----->pagerAdapter
        //viewPager---->fragment---->fragmentPagerAdapter:选这个，因为frag数量比较少
        //viewPager---->fragment---->fragmentStatePagerAdapter

        //把fragments装起来
        mFragments.add(new SessionFragment());
        mFragments.add(new ContactFragment());
        mFragments.add(new MineFragment());
        mViewPager.setAdapter(new MinePagerAdapter(getSupportFragmentManager()));

        //bottom
        mToolBarUtil = new ToolBarUtil();
        //tooBar图标
        int[] icons = {R.drawable.selector_icon_msg, R.drawable.selector_icon_contact, R.drawable.selector_icon_mine};
        mToolBarUtil.initTooBar(mLlBottom, icons);

        //设置默认选中会话
        mToolBarUtil.toolBarSelect(0);

    }

    public void initPagerListener() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //修改图标样式和标题
                mToolBarUtil.toolBarSelect(position);
                mTvTitle.setText(mTitle[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mToolBarUtil.setmOnToolBarClickListener(new ToolBarUtil.OnToolBarClickListener() {
            @Override
            public void onToolBarClick(int position) {
//                mToolBarUtil.toolBarSelect(position);
                mViewPager.setCurrentItem(position);
            }
        });
    }


    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            //3个fragments
            return mTitle.length;
        }
    }

    class MinePagerAdapter extends FragmentStatePagerAdapter {

        public MinePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            //3个fragments
            return 3;
        }
    }

//    /**
//     * test
//     */
//
//    private ContacterReceiver receiver;
//    @Override
//    protected void onDestroy() {
//        unregisterReceiver(receiver);
//        super.onDestroy();
//    }
//
//    private class ContacterReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Message message = (Message) intent.getSerializableExtra("notice");
//             String action = intent.getAction();
////            inviteNotices.add(notice);
////            refresh();
//            Log.i("ContacterReceiver", "" + action);
//        }
//    }
//
//
//    public void regReceiver(){
//        // 注册广播接收器
//        IntentFilter filter = new IntentFilter();
//        // 好友请求
//        filter.addAction("roster.subscribe");
//        filter.addAction("roster.subscribe.from");
//        receiver = new ContacterReceiver();
//        registerReceiver(receiver, filter);
//    }


    @Override
    protected void onDestroy() {
        IMService.conn.disconnect();
        //关闭IMService
        Intent intent =new Intent(getApplicationContext(),IMService.class);
        stopService(intent);

        //关闭pushService
        Intent intent2 =new Intent(getApplicationContext(), PushService.class);
        stopService(intent2);
        super.onDestroy();
    }
}