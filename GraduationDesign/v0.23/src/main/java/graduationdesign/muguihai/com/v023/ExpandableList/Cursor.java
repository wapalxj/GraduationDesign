package graduationdesign.muguihai.com.v023.ExpandableList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorTreeAdapter;

/**
 * Created by vero on 2016/4/13.
 */
public class Cursor extends SimpleCursorTreeAdapter {


    public Cursor(Context context, android.database.Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
    }

    @Override
    protected android.database.Cursor getChildrenCursor(android.database.Cursor groupCursor) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    }

    @Override
    protected void bindChildView(View view, Context context, android.database.Cursor cursor, boolean isLastChild) {
        super.bindChildView(view, context, cursor, isLastChild);
    }
}
