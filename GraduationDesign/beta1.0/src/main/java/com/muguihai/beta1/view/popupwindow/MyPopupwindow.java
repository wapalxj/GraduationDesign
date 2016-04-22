package com.muguihai.beta1.view.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.muguihai.beta1.R;
import com.muguihai.beta1.activity.AddFriendActivity;
import com.muguihai.beta1.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vero on 2016/4/16.
 */
public class MyPopupwindow {
    private Activity activity;
    List<Map<String, String>> moreList;
    private PopupWindow pwMyPopWindow;// popupwindow
    private ListView popupListview;// popupwindow中的ListView
    private int NUM_OF_VISIBLE_LIST_ROWS = 3;// 指定popupwindow中Item的数量

    public MyPopupwindow(Activity activity){
        this.activity=activity;
        iniData();
        iniPopupWindow();
    }
    //数据
    private void iniData() {

        moreList = new ArrayList<>();
        Map<String, String> map;
        map = new HashMap<>();
        map.put("share_key", "添加好友");
        moreList.add(map);
        map = new HashMap<>();
        map.put("share_key", "删除");
        moreList.add(map);
        map = new HashMap<>();
        map.put("share_key", "修改");
        moreList.add(map);
    }

    private void iniPopupWindow() {

        LayoutInflater inflater = (LayoutInflater)activity
                .getSystemService(activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_add, null);
        popupListview = (ListView) layout.findViewById(R.id.pop_add);
        pwMyPopWindow = new PopupWindow(layout);
        pwMyPopWindow.setFocusable(true);// 加上这个popupwindow中的ListView才可以接收点击事件

        popupListview.setAdapter(new SimpleAdapter(activity, moreList,
                R.layout.popup_item, new String[] { "share_key" },
                new int[] { R.id.tv_list_item }));
        popupListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                ToastUtils.myToast(activity, moreList.get(position).get("share_key"));
                switch ( moreList.get(position).get("share_key")){
                    case "添加好友":
                        Intent intent=new Intent(activity,AddFriendActivity.class);
                        activity.startActivity(intent);
                        pwMyPopWindow.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });

        // 控制popupwindow的宽度和高度自适应
        popupListview.measure(View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED);
        pwMyPopWindow.setWidth(popupListview.getMeasuredWidth());
        pwMyPopWindow.setHeight((popupListview.getMeasuredHeight() + 20)
                * (NUM_OF_VISIBLE_LIST_ROWS+1));
       // 控制popupwindow点击屏幕其他地方消失
        pwMyPopWindow.setBackgroundDrawable(activity.getResources().getDrawable(
                R.drawable.bg_popupwindow));// 设置背景图片，不能在布局中设置，要通过代码来设置
        pwMyPopWindow.setOutsideTouchable(true);// 触摸popupwindow外部，popupwindow消失。这个要求你的popupwindow要有背景图片才可以成功，如上
    }

    public PopupWindow getPwMyPopWindow() {
        return pwMyPopWindow;
    }
}
