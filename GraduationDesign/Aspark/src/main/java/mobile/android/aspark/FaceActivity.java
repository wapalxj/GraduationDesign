package mobile.android.aspark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import mobile.android.aspark.adapter.FaceListAdapter;
import mobile.android.aspark.common.Const;


public class FaceActivity extends Activity implements AdapterView.OnItemClickListener,Const
{
    private FaceListAdapter mFaceListAdapter;
    private GridView mGridViewFaces;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        mGridViewFaces = (GridView)findViewById(R.id.gridview_faces);
        mFaceListAdapter = new FaceListAdapter(this);
        mGridViewFaces.setAdapter(mFaceListAdapter);
        mGridViewFaces.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent();
        intent.putExtra(KEY_FACE_ID, position + 1);
        setResult(1, intent);
        finish();
    }

    public void onClick_Close(View view)
    {
        finish();
    }


}
