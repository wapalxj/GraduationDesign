package mobile.android.aspark;

import android.app.Activity;
import android.os.Bundle;

import org.jivesoftware.smack.XMPPConnection;

import mobile.android.aspark.data.DataWarehouse;


public class ParentActivity extends Activity
{
    protected XMPPConnection mXMPPConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mXMPPConnection = DataWarehouse.getXMPPConnection(this);
    }



}
