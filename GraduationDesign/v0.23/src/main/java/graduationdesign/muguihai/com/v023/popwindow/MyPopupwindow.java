package graduationdesign.muguihai.com.v023.popwindow;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graduationdesign.muguihai.com.v023.R;

/**
 * Created by vero on 2016/4/16.
 */
public class MyPopupwindow {
    private Activity activity;
    List<Map<String, String>> moreList;
    private PopupWindow pwMyPopWindow;// popupwindow
    private TextView popupTv;// popupwindow中的textview
    private int NUM_OF_VISIBLE_LIST_ROWS = 1;// 指定popupwindow中Item的数量

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
        View layout = inflater.inflate(R.layout.popup_handle, null);
        popupTv = (TextView) layout.findViewById(R.id.tv_pop_del);
        pwMyPopWindow = new PopupWindow(layout);
        pwMyPopWindow.setFocusable(true);// 加上这个popupwindow中的ListView才可以接收点击事件
        popupTv.setText("删除");

//        popupTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//
//                switch ( moreList.get(position).get("share_key")){
//                    case "添加好友":
//                        Intent intent=new Intent(activity,AddFriendActivity.class);
//                        activity.startActivity(intent);
//                        pwMyPopWindow.dismiss();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });

        // 控制popupwindow的宽度和高度自适应
        popupTv.measure(View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED);
        pwMyPopWindow.setWidth(popupTv.getMeasuredWidth()*2);
        pwMyPopWindow.setHeight((popupTv.getMeasuredHeight() + 20)
                * 2);
       // 控制popupwindow点击屏幕其他地方消失
        pwMyPopWindow.setBackgroundDrawable(activity.getResources().getDrawable(
                R.drawable.bg_popupwindow));// 设置背景图片，不能在布局中设置，要通过代码来设置
        pwMyPopWindow.setOutsideTouchable(true);// 触摸popupwindow外部，popupwindow消失。这个要求你的popupwindow要有背景图片才可以成功，如上
    }

    public PopupWindow getPwMyPopWindow() {
        return pwMyPopWindow;
    }
}
