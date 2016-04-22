package test.com.quickaction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        btn= (Button) findViewById(R.id.quick);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuickAction quickAction = new QuickAction(getApplicationContext(), QuickAction.HORIZONTAL);
                quickAction
                        .addActionItem(new ActionItem(0, "删除"));
                quickAction.addActionItem(new ActionItem(1,"指定"));

                quickAction
                        .setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {

                            @Override
                            public void onItemClick(QuickAction source, int pos,
                                                    int actionId) {
                                switch (actionId) {
                                    case 0:
                                        Toast.makeText(getApplicationContext(),"1111",Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Toast.makeText(getApplicationContext(),"22222",Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                quickAction.show(btn);
            }
        });
    }
}
