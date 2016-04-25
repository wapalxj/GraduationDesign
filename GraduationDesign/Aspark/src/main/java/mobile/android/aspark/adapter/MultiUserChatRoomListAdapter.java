package mobile.android.aspark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.jivesoftware.smackx.disco.packet.DiscoverItems;

import java.util.ArrayList;
import java.util.List;

import mobile.android.aspark.R;
import mobile.android.aspark.data.ChatRoomData;

//  用于显示聊天室列表
public class MultiUserChatRoomListAdapter extends BaseAdapter
{
    private Context mContext;
    private List<ChatRoomData> mChatRoomList;

    private LayoutInflater mLayoutInflater;

    public MultiUserChatRoomListAdapter(Context context, List<DiscoverItems.Item> chatRoomList)
    {

        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChatRoomList = new ArrayList<ChatRoomData>();
        for(int i = 0; i < chatRoomList.size();i++)
        {
            ChatRoomData data = new ChatRoomData();
            data.item = chatRoomList.get(i);
            mChatRoomList.add(data);

        }

    }

    @Override
    public int getCount()
    {
        return mChatRoomList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }


    public DiscoverItems.Item getChatRoom(int position)
    {
        return (position < mChatRoomList.size()) ? mChatRoomList.get(position).item : null;
    }
    public void updateChatRooms(List<DiscoverItems.Item> chatRoomList)
    {

        mChatRoomList.clear();
        for(int i = 0; i < chatRoomList.size();i++)
        {
            ChatRoomData data = new ChatRoomData();
            data.item = chatRoomList.get(i);
            mChatRoomList.add(data);

        }


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
            convertView = mLayoutInflater.inflate(R.layout.multi_user_chat_room_item, null);
        }
        TextView charService = (TextView) convertView.findViewById(R.id.textview_char_room_item);
        DiscoverItems.Item item = mChatRoomList.get(position).item;

        charService.setText(item.getName());
        return convertView;
    }
}
