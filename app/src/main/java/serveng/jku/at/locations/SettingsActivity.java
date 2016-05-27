package serveng.jku.at.locations;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    RadioGroup radioGroup;
    RadioButton radioButton;
    Button btnSave;
    TextView ipTv, portTv;
    int choice;
    SharedPreferences sp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        addListenerOnButton();
        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        ipTv = (TextView) findViewById(R.id.ip);
        portTv = (TextView) findViewById(R.id.port);

        if (sp.contains("ipKey")) ipTv.setText(sp.getString("ipKey", ""));
        if (sp.contains("portKey")) portTv.setText(sp.getString("portKey", ""));
        if (sp.contains("checkKey")) {
            switch(sp.getString("checkKey", "")) {
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
                case "3 Minutes":
                    radioGroup.check(R.id.button_3m);
                    break;
                case "5 Minutes":
                    radioGroup.check(R.id.button_5m);
                    break;
            }
        }
    }

    public void save (){
        Toast.makeText(getApplicationContext(), "Settings saved!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
                editor.putString("ipKey", ipTv.getText().toString());
                editor.putString("portKey",  portTv.getText().toString());
                editor.putString("checkKey", radioButton.getText().toString());
                editor.putLong("checkValue", Long.parseLong(radioButton.getContentDescription().toString()));
                editor.commit();
                save();
            }
        });
    }
}