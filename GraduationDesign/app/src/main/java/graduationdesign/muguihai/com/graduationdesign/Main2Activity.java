package graduationdesign.muguihai.com.graduationdesign;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends AppCompatActivity {
    private Button close1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        close1= (Button) findViewById(R.id.close1);
    }
    public void close1(View v){
        MainActivity.instance.finish();
    }

}
