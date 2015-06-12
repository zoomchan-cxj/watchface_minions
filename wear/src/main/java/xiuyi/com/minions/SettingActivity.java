package xiuyi.com.minions;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class SettingActivity extends Activity {


    String TEMPERATURE_TYPE;
    String last_radioGroup_checked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);



        RadioButton button1 = (RadioButton) findViewById(R.id.radioF);
        RadioButton button2 = (RadioButton) findViewById(R.id.radioC);

        last_radioGroup_checked="1";

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sp.getString("last_radioGroup_checked","").equals("")){
            last_radioGroup_checked = sp.getString("last_radioGroup_checked","");

        }



        if(last_radioGroup_checked.equals("2")){
            button2.setChecked(true);
        }else{
            button1.setChecked(true);
        }

        RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioF){
                    TEMPERATURE_TYPE = "1";
                }else{
                    TEMPERATURE_TYPE = "2";
                }
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit();
                editor.putString("last_radioGroup_checked",TEMPERATURE_TYPE);
                editor.putString("temperature_type", TEMPERATURE_TYPE);
                editor.commit();

                Intent intent = new Intent("android.intent.action.SETTING_CHANGED");
                intent.putExtra("temperature_type", TEMPERATURE_TYPE);
                sendBroadcast(intent);




            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
