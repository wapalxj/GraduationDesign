package graduationdesign.muguihai.com.v023.popwindow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import graduationdesign.muguihai.com.v023.R;

public class PopActivity extends AppCompatActivity {

    private PopupWindow mPopupWindow;
    private Button handle_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);
        initPop();
    }

    private  void initPop(){
        handle_btn= (Button) findViewById(R.id.handle);
        MyPopupwindow pop=new MyPopupwindow(PopActivity.this);
        mPopupWindow=pop.getPwMyPopWindow();
        handle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.myToast(getApplicationContext(),"pop!");
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();// 关闭
                } else {
                    mPopupWindow.showAsDropDown(handle_btn);// 显示
                }

            }
        });
    }
}
