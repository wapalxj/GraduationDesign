package com.muguihai.beta1.utils;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muguihai.beta1.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by vero on 2016/3/21.
 */
public class ToolBarUtil {

    private List<TextView> mTextViews=new ArrayList<>();
    public void initTooBar(LinearLayout container,int []icons,String []titles){

        for(int i=0;i<icons.length;i++){
            RelativeLayout view=
                    (RelativeLayout) View.inflate(container.getContext(), R.layout.toolbar,null);
            TextView tv= (TextView) view.getChildAt(0);
            //设置tga:设置为选项卡TV的TAG
            TextView tv2= (TextView) view.getChildAt(1);
            tv.setText(titles[i]);
            tv.setTag(tv2);
            //图片设置在top
            tv.setCompoundDrawablesWithIntrinsicBounds(0,icons[i],0,0);
            //设置weight
            int width=0;
            int height=LinearLayout.LayoutParams.MATCH_PARENT;
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(width,height);
            params.weight=1;
            container.addView(view, params);
            tv.setEnabled(true);
            //保存TextView到集合中
            mTextViews.add(tv);

            //设置toolbar按钮点击
            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //不同模块之间传值需要接口回调
                //3.需要传值的地方，用接口对象调用方法
                mOnToolBarClickListener.onToolBarClick(finalI);
                }
            });
        }

    }
    //toolBar选中效果
    public void toolBarSelect(int position){
        //还原所有的颜色
        for (TextView tv: mTextViews) {
            tv.setSelected(false);
            tv.setTextColor(Color.BLACK);
        }
        mTextViews.get(position).setSelected(true);
        mTextViews.get(position).setTextColor(Color.GREEN);
    }
    //toolBar消息提示
    public void toolBarNotification(int position,int notice_count){
        //获取tga:即提示的TV
        TextView tv2= (TextView) mTextViews.get(position).getTag();
        tv2.setText(String.valueOf(notice_count));
        if (Integer.valueOf(tv2.getText().toString())>0){
            tv2.setVisibility(View.VISIBLE);
        }else {
            tv2.setVisibility(View.GONE);
        }
    }


    /**
     * 接口回调
     */
    //1.创建接口
    public interface  OnToolBarClickListener{
        void onToolBarClick(int position);
    }
    //2.定义接口变量
    OnToolBarClickListener mOnToolBarClickListener;

    //4.暴露公共方法


    public void setmOnToolBarClickListener(OnToolBarClickListener mOnToolBarClickListener) {
        this.mOnToolBarClickListener = mOnToolBarClickListener;
    }
}
