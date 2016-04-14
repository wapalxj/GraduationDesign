package graduationdesign.muguihai.com.v023.ExpandableList;

import android.content.Context;
import android.database.*;
import android.database.Cursor;
import android.widget.SimpleCursorTreeAdapter;

/**
 * Created by vero on 2016/4/13.
 */
public class TestCursor extends SimpleCursorTreeAdapter {
    public TestCursor(Context context, Cursor cursor,
                      int groupLayout,
                      String[] groupFrom,
                      int[] groupTo,
                      int childLayout,
                      String[] childFrom,
                      int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);


    }
//
//    public TestCursor(Context context, Cursor cursor) {
//        super(context, cursor);
//
//    }

    @Override
    protected android.database.Cursor getChildrenCursor(Cursor groupCursor) {
        return null;
    }
}
