package mobile.android.aspark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.jivesoftware.smackx.muc.HostedRoom;

import java.util.ArrayList;
import java.util.List;

import mobile.android.aspark.R;
import mobile.android.aspark.data.ChatServiceData;

//  用于聊天服务的Adapter
public class ChatServiceListAdapter extends BaseAdapter
{
    private Context mContext;
    private List<ChatServiceData> mChatServiceList;
    private LayoutInflater mLayoutInflater;

    public ChatServiceListAdapter(Context context, List<HostedRoom> chatServiceList)
    {
        mChatServiceList = new ArrayList<ChatServiceData>();
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 0; i < chatServiceList.size();i++)
        {
            String jid = chatServiceList.get(i).getJid();
            if(jid.startsWith("proxy") || jid.startsWith("pubsub"))
            {
                continue;
            }
            ChatServiceData data = new ChatServiceData();
            data.hostedRoom = chatServiceList.get(i);
            mChatServiceList.add(data);

        }

    }

    @Override
    public int getCount()
    {
        return mChatServiceList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return (position < mChatServiceList.size()) ? mChatServiceList.get(position) : null;
    }


    public String getJID(int position)
    {
        return (position < mChatServiceList.size()) ? mChatServiceList.get(position).hostedRoom.getJid() : null;
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
            convertView = mLayoutInflater.inflate(R.layout.chat_service_item, null);
        }
        TextView charService = (TextView) convertView.findViewById(R.id.textview_char_service_item);
        HostedRoom hostedRoom = mChatServiceList.get(position).hostedRoom;

        charService.setText(hostedRoom.getJid());


        return convertView;
    }
}
