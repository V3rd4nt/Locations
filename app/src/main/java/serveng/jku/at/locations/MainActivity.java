package serveng.jku.at.locations;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    SharedPreferences sp;
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    long period;
    PositionCreator pos = null;
    Context context = this;
    String JsonStringUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // set json string url
        JsonStringUrl = "http://" + sp.getString("ipKey", "skynet1.myds.me") + ":" + sp.getString("portKey", "2010") + "/";
        testConnection(JsonStringUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //initial setting of the map view and position
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(48.306, 14.306)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.5f));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    public void startTimer() {
        period = sp.getLong("checkValue", 60000L);
        Log.d("UPDATE-INTERVAL-VALUE", String.valueOf(period));
        Toast.makeText(getApplicationContext(), sp.getString("checkKey", "1 Minute"), Toast.LENGTH_SHORT).show();
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
                        pos = new PositionCreator();
                        pos.createPositions(mMap, JsonStringUrl, context, timer);
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
                Toast.makeText(getApplicationContext(), "Re-Enabled refresh", Toast.LENGTH_SHORT).show();
                startTimer();
                break;
            case R.id.stopTimer:
                stopTimer();
                pos.disconnectFromServer();
                Toast.makeText(getApplicationContext(), "Disabled refresh", Toast.LENGTH_SHORT).show();
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
                .setTitle("About this app:")
                .setMessage("Created by\n  Group 1\n  Service Engineering SS16\n  28.05.2016\n  Version 1.6")
                .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public void menu_quit() {
        if (pos != null) pos.disconnectFromServer();
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    public void testConnection(String StringUrl) {
        try {
            URL url = new URL(StringUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(sp.getInt("timeOutValue", 3000));
            Log.i("STATUS", String.valueOf(con.getResponseCode()));
        }
        catch (Exception e) {
            Log.e("STATUS", e.toString());
            menu_quit();
        }
    }
}

