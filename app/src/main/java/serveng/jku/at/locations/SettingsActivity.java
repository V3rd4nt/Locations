package serveng.jku.at.locations;

/**
 * Created by Peter on 24.05.2016.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    RadioGroup radioGroup;
    RadioButton radioButton;
    Button btnSave;
    int choice;
    SharedPreferences sp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        addListenerOnButton();
        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (sp.contains("checkKey")) {
            switch(sp.getString("checkKey", "")) {
                case "5 Seconds":
                    radioGroup.check(R.id.button_5);
                    break;
                case "10 Seconds":
                    radioGroup.check(R.id.button_10);
                    break;
                case "20 Seconds":
                    radioGroup.check(R.id.button_20);
                    break;
                case "30 Seconds":
                    radioGroup.check(R.id.button_30);
                    break;
                case "1 Minute":
                    radioGroup.check(R.id.button_1m);
                    break;
                case "5 Minutes":
                    radioGroup.check(R.id.button_5m);
                    break;
            }
        }
    }
    public void addListenerOnButton() {

        radioGroup = (RadioGroup) findViewById(R.id.group);

        btnSave = (Button) findViewById(R.id.save);

        btnSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                choice = radioGroup.getCheckedRadioButtonId();

                radioButton = (RadioButton) findViewById(choice);

                Editor editor = sp.edit();
                editor.putString("checkKey", radioButton.getText().toString());
                editor.commit();

                Toast.makeText(getApplicationContext(), "Set the refresh interval to\n"+radioButton.getText().toString(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}