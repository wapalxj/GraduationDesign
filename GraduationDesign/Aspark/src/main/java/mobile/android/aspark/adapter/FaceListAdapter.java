package mobile.android.aspark.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import mobile.android.aspark.R;
import mobile.android.aspark.common.Const;
import mobile.android.aspark.common.Util;

public class FaceListAdapter extends ParentAdapter implements Const
{
    private int mFaceCount;   //  表情图像总数
    private Context mContext;
    public FaceListAdapter(Context context)
    {
        super(context);
        calculateFaceCount();
    }
    private void calculateFaceCount()
    {
        int i = 0;
        while(true)
        {
            i++;
            String faceName = FACE_PREFIX + i;
            try
            {
                R.drawable.class.getField(faceName);
            }
            catch (Exception e)
            {
                break;
            }
        }
        i--;
        mFaceCount = i;
    }
    @Override
    public int getCount()
    {
        return mFaceCount;
    }
    public int getFace(int position)
    {
        position++;
        if(position > mFaceCount)
            return -1;
        try
        {

            return Util.getResourceIdFromName(R.drawable.class, FACE_PREFIX + position);
        }
        catch (Exception e)
        {
            return -1;
        }
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
        if(convertView == null)
        {
            convertView =  mLayoutInflater.inflate(R.layout.face, null);
        }

        ImageView face = (ImageView)convertView;
        face.setImageResource(getFace(position));
        return convertView;
    }
}
