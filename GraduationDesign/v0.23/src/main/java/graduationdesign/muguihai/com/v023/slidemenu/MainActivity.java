package graduationdesign.muguihai.com.v023.slidemenu;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import graduationdesign.muguihai.com.v023.R;


public class MainActivity extends AppCompatActivity {
    private ListView mMenu_listview;
    private ListView mMain_listview;
    private SlideMenu slideMenu;
    private ImageView main_head;
    private MyLinearLayout myLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        main_head = (ImageView) findViewById(R.id.iv_head);
        String [] menu={"a1111","a2222","a3333","a4444","a55","a666","a777","a8888","a55","a666","a777","a8888"};
        mMenu_listview= (ListView) findViewById(R.id.menu_listview);
        mMenu_listview.setAdapter(
                new ArrayAdapter<>(
                        getApplicationContext(),
                android.R.layout.simple_list_item_1,menu
                )
        );

        String [] main={"a1111","a2222","a3333","a4444","a55","a666","a777","a8888","a55","a666","a777","a8888","a777","a8888","a55","a666","a777","a8888","a777","a8888","a55","a666","a777","a8888"};
        mMain_listview= (ListView) findViewById(R.id.main_listview);
        mMain_listview.setAdapter(
                new ArrayAdapter<String>(
                        getApplicationContext(),
                        android.R.layout.simple_list_item_1,main
                ){
                    //重写getView设置文字颜色
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view= convertView==null?super.getView(position, convertView, parent):convertView;
                        ((TextView)view).setTextColor(Color.BLACK);
                        //item伴随动画
                        //先缩小
                        ViewHelper.setScaleX(view,0.5f);
                        ViewHelper.setScaleY(view, 0.5f);
                        //再以属性动画放大
                        ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350).start();
                        ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350).start();
                        return view;
                    }
                }
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
}
