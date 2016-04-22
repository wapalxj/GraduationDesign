package graduationdesign.muguihai.com.v023.ExpandableList;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import graduationdesign.muguihai.com.v023.R;

public class E2Activity extends AppCompatActivity {
    private List<String> group_list;
    private List<List<String>> child_list;
    private List<List<Integer>> child_list2;
    private ExpandableListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e2);
        listView= (ExpandableListView) findViewById(R.id.explist);
        //数据源
        group_list = new ArrayList<>();
        group_list.add("A");
        group_list.add("B");
        group_list.add("C");
        group_list.add("D");

        ArrayList<String> group_list1 = new ArrayList<>();
        group_list1.add("CC");
        group_list1.add("DD");
        group_list1.add("EE");
        ArrayList<String> group_list2 = new ArrayList<>();
        group_list2.add("CC");
        group_list2.add("DD");
        group_list2.add("EE");
        ArrayList<String> group_list3 = new ArrayList<>();
        group_list3.add("GG");
        group_list3.add("HH");
        ArrayList<String> group_list4 = new ArrayList<>();
        group_list4.add("GG");
        group_list4.add("CC");
        group_list4.add("DD");
        group_list4.add("EE");

        child_list = new ArrayList<>();
        child_list.add(group_list1);
        child_list.add(group_list2);
        child_list.add(group_list3);
        child_list.add(group_list4);


        List<Integer> tmp_list = new ArrayList<>();
        tmp_list.add(R.mipmap.ic_launcher);
        tmp_list.add(R.mipmap.ic_launcher);
        tmp_list.add(R.mipmap.ic_launcher);

        child_list2 = new ArrayList<>();
        child_list2.add(tmp_list);
        child_list2.add(tmp_list);
        child_list2.add(tmp_list);
        listView.setAdapter(new E2Adapter(getApplicationContext(),getLayoutInflater(),group_list,child_list,child_list2));
    }

}

