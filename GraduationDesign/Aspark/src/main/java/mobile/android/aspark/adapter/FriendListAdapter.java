package mobile.android.aspark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mobile.android.aspark.R;
import mobile.android.aspark.data.DataWarehouse;
import mobile.android.aspark.data.UserData;

public class FriendListAdapter extends BaseAdapter
{
    private List<UserData> mUsers;
    private Map<String, String> mUserMap;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public FriendListAdapter(Context context, Collection<RosterEntry> entries)
    {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUsers = new ArrayList<UserData>();
        mUserMap = new HashMap<>();
        if (entries != null)
        {
            Iterator<RosterEntry> iterator = entries.iterator();
            while (iterator.hasNext())
            {
                RosterEntry entry = iterator.next();
                if(entry.getUser().indexOf("@") == -1) {
                    UserData userData = new UserData(entry.getName(), entry.getUser());
                    mUserMap.put(entry.getUser(), entry.getName());
                    mUsers.add(userData);
                }
            }
        }

    }

    @Override
    public int getCount()
    {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position)
    {

        return mUsers.get(position);
    }

    public String getName(int position)
    {
        return mUsers.get(position).name;
    }

    public String getUser(int position)
    {
        return mUsers.get(position).user;
    }
    //  根据用户名获取昵称（Name）
    public String findName(String user)
    {
        String name =  mUserMap.get(user);
        if(name == null)
            name = user;
        return name;
    }
    public void addUserData(UserData userData)
    {
        mUsers.add(userData);
        notifyDataSetChanged();
    }
    public void removeUserData(int position)
    {

        XMPPConnection conn = DataWarehouse.getXMPPConnection(mContext);
        RosterEntry entry = conn.getRoster().getEntry(getUser(position));
        if(entry != null)
        {
            try
            {
                conn.getRoster().removeEntry(entry);
            }
            catch (Exception e)
            {
                Toast.makeText(mContext, "删除好友失败.", Toast.LENGTH_LONG).show();
            }
        }
        mUsers.remove(position);

        notifyDataSetChanged();
    }
    @Override
    public long getItemId(int position)
    {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = mLayoutInflater.inflate(R.layout.friend_list_item, null);
        }
        TextView user = (TextView) convertView.findViewById(R.id.textview_friend_list_item_user);
        if (getName(position) == null)
            user.setText(getUser(position));
        else
            user.setText(getName(position));

        return convertView;
    }

}
