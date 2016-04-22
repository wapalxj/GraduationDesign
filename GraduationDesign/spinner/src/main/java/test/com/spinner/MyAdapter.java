package test.com.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2015/9/23.
 */
public class MyAdapter <T>extends BaseAdapter {
    private Context context;
    private List<T> list;
    private LayoutInflater inflater;
    private int resource;

    public MyAdapter(Context context, List<T> ele,int resource) {
        this.context = context;
        this.list = ele;
        this.inflater = LayoutInflater.from(this.context);
        this.resource=resource;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView=this.inflater.inflate(this.resource, null);
        TextView group_name=(TextView)convertView.findViewById(R.id.group_name);
        User u=(User)getItem(position);
        group_name.setText(u.getName().toString());
        return convertView;
    }

}
