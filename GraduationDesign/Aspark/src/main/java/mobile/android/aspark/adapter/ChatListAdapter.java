package mobile.android.aspark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobile.android.aspark.R;
import mobile.android.aspark.common.Const;
import mobile.android.aspark.common.Util;
import mobile.android.aspark.data.ChatData;

//  用于显示聊天记录列表的Adapter
public class ChatListAdapter extends BaseAdapter implements Const
{
    private Context mContext;
    private List<ChatData> mChatDataList;
    private LayoutInflater mLayoutInflater;

    public ChatListAdapter(Context context)
    {
        mContext = context;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mChatDataList = new ArrayList<ChatData>();
    }

    @Override
    public int getCount()
    {
        return mChatDataList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return (position < mChatDataList.size()) ? mChatDataList.get(position) : null;
    }
    public void addItem(ChatData item)
    {
        mChatDataList.add(item);
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
        if(convertView == null)
        {
            convertView = mLayoutInflater.inflate(R.layout.chat_list_item, null);
        }
        View left = null;
        View right = null;
        if(convertView.getTag(R.id.linearlayout_chat_left) == null) {
            left = convertView.findViewById(R.id.linearlayout_chat_left);
            right = convertView.findViewById(R.id.linearlayout_chat_right);
            convertView.setTag(R.id.linearlayout_chat_left, left);
            convertView.setTag(R.id.linearlayout_chat_right, right);
        }
        else
        {
            left = (View) convertView.getTag(R.id.linearlayout_chat_left);
            right = (View) convertView.getTag(R.id.linearlayout_chat_right);
        }

        TextView leftChatText = (TextView)convertView.findViewById(R.id.textview_chat_text_left);
        TextView rightChatText = (TextView)convertView.findViewById(R.id.textview_chat_text_right);
        TextView leftUserName = (TextView)convertView.findViewById(R.id.textview_left_user_name);
        TextView rightUserName = (TextView)convertView.findViewById(R.id.textview_right_user_name);
        ImageView leftHeadPortrait = (ImageView)convertView.findViewById(R.id.imageview_chat_left_head_portrait);
        ImageView rightHeadPortrait = (ImageView)convertView.findViewById(R.id.imageview_chat_right_head_portrait);

        left.setVisibility(View.GONE);
        right.setVisibility(View.GONE);
        ChatData chatData = mChatDataList.get(position);
        TextView currentChatText;
        TextView currentUserName;
        ImageView currentHeadPortrait;
        View currentChatItem;
        if(chatData.isOwner)
        {
            right.setVisibility(View.VISIBLE);
            currentChatText = rightChatText;
            currentHeadPortrait = rightHeadPortrait;
            currentChatItem = right;
            currentUserName = rightUserName;
        }
        else
        {
            left.setVisibility(View.VISIBLE);
            currentChatText = leftChatText;
            currentHeadPortrait = leftHeadPortrait;
            currentChatItem = left;
            currentUserName = leftUserName;
        }
        currentUserName.setText(chatData.name);
        currentChatText.setText(chatData.text);
        Util.updateFacesForTextView(mContext, currentChatText);



        return convertView;
    }
}
