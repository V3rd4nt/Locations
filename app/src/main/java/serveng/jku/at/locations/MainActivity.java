package serveng.jku.at.locations;

/**
 * Created by Peter on 24.05.2016.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    SharedPreferences sp;
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    long period;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    public void startTimer() {
        period = sp.getLong("checkValue", 60000L);
        Toast.makeText(getApplicationContext(), sp.getString("checkKey", ""), Toast.LENGTH_LONG).show();
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0L, period);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        mMap.clear();
                        // Add a marker in Linz and move the camera
                        LatLng position = new LatLng(48.308351, 14.284837);
                        mMap.addMarker(new MarkerOptions().position(position).title("Linz Nibelungenbrücke"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
                    }
                });
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.startTimer:
                startTimer();
                break;
            case R.id.stopTimer:
                stopTimer();
                Toast.makeText(getApplicationContext(), "Turned off refresh", Toast.LENGTH_LONG).show();
                break;
            case R.id.settings:
                menu_settings();
                break;
            case R.id.about:
                menu_about();
                break;
            case R.id.quit:
                menu_quit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void menu_settings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    public void menu_about() {
        new AlertDialog.Builder(this)
                .setTitle("About:")
                .setMessage("This app was created by\nGroup 1\nService Engineering SS16\n26.05.2016\nVersion 1.3")
                .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public void menu_quit() {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
}
