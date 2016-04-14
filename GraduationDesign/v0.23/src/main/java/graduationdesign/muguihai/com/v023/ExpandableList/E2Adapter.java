package graduationdesign.muguihai.com.v023.ExpandableList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import graduationdesign.muguihai.com.v023.R;

/**
 * Created by vero on 2016/4/12.
 */
public class E2Adapter extends BaseExpandableListAdapter {

    private List<String> group_list;
    private List<List<String>> item_list;
    private List<List<Integer>> item_list2;

    private Context context;
    private LayoutInflater inflater;

    public E2Adapter(Context context,LayoutInflater inflater,List<String> group_list,List<List<String>> item_list,List<List<Integer>> item_list2) {
        this.context = context;
        this.inflater=inflater;
        this.group_list=group_list;
        this.item_list=item_list;
        this.item_list2=item_list2;
    }

    @Override
    public int getGroupCount() {
        return group_list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return item_list.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group_list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return item_list.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            convertView = (View) inflater.from(context).inflate(
                    R.layout.expendlist_group, null);
            groupHolder = new GroupHolder();
            groupHolder.group_name = (TextView) convertView.findViewById(R.id.txt);
            groupHolder.nums = (TextView) convertView.findViewById(R.id.nums);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        groupHolder.group_name.setText(group_list.get(groupPosition));
        groupHolder.nums.setText(String.valueOf(getChildrenCount(groupPosition)+"/"+String.valueOf(getChildrenCount(groupPosition))));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ItemHolder itemHolder = null;
        if (convertView == null) {
            convertView = (View) inflater.from(context).inflate(
                    R.layout.expendlist_item, null);
            itemHolder = new ItemHolder();
            itemHolder.nickname = (TextView) convertView.findViewById(R.id.nickname);
            itemHolder.head = (ImageView) convertView.findViewById(R.id.head);
            itemHolder.online_state= (TextView) convertView.findViewById(R.id.online_state);
            itemHolder.signature= (TextView) convertView.findViewById(R.id.signature);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
        }
        itemHolder.nickname.setText("vero");
//        itemHolder.head.setBackgroundResource(item_list2.get(1).get(1));
        itemHolder.head.setBackgroundResource(R.mipmap.ic_launcher);
        itemHolder.online_state.setText("[离线]");
        itemHolder.signature.setText("我用PHP开发安卓");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

class GroupHolder {
    public TextView group_name;
    public TextView nums;
}

class ItemHolder {
    public ImageView head;
    public TextView nickname;
    public TextView online_state;
    public TextView signature;
}