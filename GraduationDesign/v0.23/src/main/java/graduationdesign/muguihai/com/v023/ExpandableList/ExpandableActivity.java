package graduationdesign.muguihai.com.v023.ExpandableList;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graduationdesign.muguihai.com.v023.R;

public class ExpandableActivity  extends ExpandableListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        //数据源
        List<HashMap<String,String>> groupData =new ArrayList<>();

        HashMap<String,String> _map1=new HashMap<>();
        _map1.put("name", "vnix");
        _map1.put("name2","vero");
        _map1.put("hobby","gggg");
        groupData.add(_map1);//add的数目必须和 childData.add的数目一样:因为groupData和childData的外层List是对应的
        groupData.add(_map1);

        List<List<HashMap<String,String>>> childData=new ArrayList<>();
        List<HashMap<String,String>> childItem=new ArrayList<HashMap<String,String>>();
        HashMap<String,String> _map2=new HashMap<>();
        _map2.put("name","vnix");
        _map2.put("name2","vero");
        _map2.put("hobby", "gggg");
        childItem.add(_map2);
        childItem.add(_map2);
        childData.add(childItem);//add的数目必须和 groupData.add的数目一样
        childData.add(childItem);

        //simple_expandable_list_item_1只有1个text,id是text1,simple_expandable_list_item_2有2个text
        SimpleExpandableListAdapter adapter=new SimpleExpandableListAdapter(
                this,
                groupData,
                R.layout.this_expandable_list_item,
                new String[]{"hobby","hobby"},
                new int[] {android.R.id.text1,android.R.id.text2},
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{"name","hobby"},
                new int[] {android.R.id.text1,android.R.id.text2}
        );



        //使用自定义layout
//        SimpleExpandableListAdapter adapter=new SimpleExpandableListAdapter(this,groupData,R.layout.this_expanable_my_adapter,
//                new String[]{"hobby","hobby"},new int[] {R.id.textView,R.id.textView2},
//                childData,R.layout.this_expanable_my_adapter,
//                new String[]{"name","hobby"},new int[] {R.id.textView,R.id.textView2});
        setListAdapter(adapter);
    }

    //时间的重写
    //1.缩起
    @Override
    public void onGroupCollapse(int groupPosition) {
        super.onGroupCollapse(groupPosition);
        Toast.makeText(this,"第"+groupPosition+"个group被收缩了",Toast.LENGTH_SHORT).show();
    }
    //2.展开
    @Override
    public void onGroupExpand(int groupPosition) {
        super.onGroupExpand(groupPosition);
        Toast.makeText(this,"第"+groupPosition+"个group被展开了",Toast.LENGTH_SHORT).show();
    }
    //3.字控件点击
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        //参数：当前视图listView，group,第几个child,child--id,
        Toast.makeText(this,"第"+childPosition+"个child被点击了",Toast.LENGTH_SHORT).show();
        return super.onChildClick(parent, v, groupPosition, childPosition, id);
    }

}

