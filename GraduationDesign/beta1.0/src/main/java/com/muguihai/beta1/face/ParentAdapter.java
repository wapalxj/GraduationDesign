package com.muguihai.beta1.face;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ParentAdapter extends BaseAdapter
{
    protected Context mContext;
    protected LayoutInflater mLayoutInflater;

    public ParentAdapter(Context context)
    {
        mContext = context;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount()
    {
        return 0;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return null;
    }
}
