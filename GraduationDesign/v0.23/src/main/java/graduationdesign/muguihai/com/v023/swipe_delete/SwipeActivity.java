package graduationdesign.muguihai.com.v023.swipe_delete;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import graduationdesign.muguihai.com.v023.R;

public class SwipeActivity extends AppCompatActivity {
    private ListView mListView;
    private ArrayList<String> list =new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);
        initView();
        initData();
    }

    private void initView(){
        mListView= (ListView) findViewById(R.id.listview);
    }

    private void initData(){
        for (int i=0;i<30;i++){
            list.add("name-"+i);
        }
        mListView.setAdapter(new MyAdapter());

        //滚动监听
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    //如果垂直滑动，则关闭已经打开的swipe
                    SwipeLayoutManager.getInstance().closeCurrentLayout();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }


    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=View.inflate(SwipeActivity.this,R.layout.adapter_list,null);
            }
            ViewHolder holder=ViewHolder.getHolder(convertView);
            holder.tv_name.setText(list.get(position));

            //SwipeChangeListener
            holder.swipeLayout.setTag(position);
            holder.swipeLayout.setOnSwipeChangeListener(new SwipeLayout.OnSwipeChangeListener() {
                @Override
                public void onOpen(Object tag) {
                    Toast.makeText(SwipeActivity.this, "第"+(Integer) tag+"个swipe被打开了",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onClose(Object tag) {
                    Toast.makeText(SwipeActivity.this, "第"+(Integer) tag+"个swipe被关闭了",Toast.LENGTH_SHORT).show();
                }
            });



            return convertView;
        }
    }

    static class ViewHolder{
        TextView tv_name,tv_delete;
        SwipeLayout swipeLayout;

        public ViewHolder(View convertView){
            tv_name= (TextView) convertView.findViewById(R.id.tv_name);
            tv_delete= (TextView) convertView.findViewById(R.id.tv_delete);
            swipeLayout= (SwipeLayout) convertView.findViewById(R.id.swipelayout);

        }

        public static ViewHolder getHolder(View convertView){
            ViewHolder holder= (ViewHolder) convertView.getTag();
            if (holder==null){
                holder=new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            return holder;
        }
    }
}
