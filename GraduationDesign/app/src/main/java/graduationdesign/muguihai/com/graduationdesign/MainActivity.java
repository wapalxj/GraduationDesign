package graduationdesign.muguihai.com.graduationdesign;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * 在一个Activity中关闭另外一个Activity
 */
public class MainActivity extends AppCompatActivity {
    private Intent intent;
    private Button start2;
    public static MainActivity instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance=MainActivity.this;

        start2= (Button) findViewById(R.id.start2);
        intent=new Intent(MainActivity.this,Main2Activity.class);
    }
    public void start2(View v){
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("11111111", "MainActivity被关闭了");
    }
}
