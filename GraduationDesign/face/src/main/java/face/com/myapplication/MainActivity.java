package face.com.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ImageView imageButton;
    private EditText editText;
    private Button send;
    private TextView chat;


    private FaceListAdapter mFaceListAdapter;
    private GridView mGridViewFaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initView(){
        imageButton= (ImageView) findViewById(R.id.face_btn);
        editText= (EditText) findViewById(R.id.edittext);
        send= (Button) findViewById(R.id.send);
        chat= (TextView) findViewById(R.id.chat);

        mGridViewFaces = (GridView)findViewById(R.id.gridview_faces);
        mFaceListAdapter = new FaceListAdapter(this);
        mGridViewFaces.setAdapter(mFaceListAdapter);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageButton.isSelected()){
                    imageButton.setSelected(false);
                    mGridViewFaces.setVisibility(View.GONE);
                }else {
                    imageButton.setSelected(true);
                    mGridViewFaces.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initListener(){
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),editText.getText().toString(),Toast.LENGTH_SHORT).show();
                chat.setText(editText.getText().toString());
                FaceUtil.updateFacesForTextView(getApplication(),chat);
                editText.setText(null);
            }
        });

        mGridViewFaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int faceId=position+1;
                if (faceId != -1)
                {
                    String faceResName = Face_Const.FACE_PREFIX + faceId;

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                            FaceUtil.getResourceIdFromName(R.drawable.class, faceResName));

                    Matrix matrix = new Matrix();
                    matrix.postScale(1f, 1f);
                    Bitmap smallBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                            matrix, true);
                    ImageSpan imageSpan = new ImageSpan(MainActivity.this, smallBitmap);
                    String faceText = Face_Const.FACE_TEXT_PREFIX + faceId + Face_Const.FACE_TEXT_SUFFIX;
//                    editText.setText(faceText);//插入字符
                    SpannableString spannableString = new SpannableString(faceText);
                    spannableString.setSpan(imageSpan, 0, faceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editText.getText().insert(editText.getSelectionStart(), spannableString);
                }
            }
        });
    }
}
