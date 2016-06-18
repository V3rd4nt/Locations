package serveng.jku.at.locations;

import android.app.AlertDialog;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.Handler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    GoogleMap mMap = null;
    SharedPreferences sp;
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    long period;
    PositionCreator pos = null;
    Context context = this;
    String JsonStringUrl;
    int timeOut;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    boolean connectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // set json string url
        JsonStringUrl = setJsonStringUrl();
        // set connection timeout
        timeOut = sp.getInt("timeOutValue", Integer.valueOf(context.getResources().getString(R.string.timeout_default)));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // connect google api client
        buildGoogleApiClient();
        // test connection to server
        connectionStatus = testConnection(JsonStringUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    private void setInitialCamPos() {
        //move map camera over Linz
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(48.306, 14.306)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.5f));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Check permissions and enable gps to find user location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        // set intial map view at first map load
        setInitialCamPos();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    public void startTimer() {
        if (connectionStatus) {
            period = sp.getLong("checkValue", 60000L);
            Log.i("MAIN-START-TIMER", "Update interval value: " + String.valueOf(period));
            Toast.makeText(getApplicationContext(), sp.getString("checkKey", "1 Minute"), Toast.LENGTH_SHORT).show();
            timer = new Timer();
            initializeTimerTask();
            timer.schedule(timerTask, 0L, period);
        }
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private String setJsonStringUrl() {
        return "http://" + sp.getString("ipKey", context.getResources().getString(R.string.ip_default)) +
                ":" + sp.getString("portKey", context.getResources().getString(R.string.port_default)) + "/login";
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        Log.i("MAIN-INIT-TIMER-TASK", "Timer set to: " + period);
                        mMap.clear();
                        pos = new PositionCreator();
                        try {
                            pos.createPositions(mMap, context, timer, timeOut, JsonStringUrl);
                        } catch (NullPointerException e) {
                            Log.e("MAIN-INIT-TIMER-TASK", e.toString());
                            menu_quit();
                        }
                    }
                });
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.resetView:
                setInitialCamPos();
                break;
            case R.id.startTimer:
                Toast.makeText(getApplicationContext(), "Enabled refresh", Toast.LENGTH_SHORT).show();
                startTimer();
                break;
            case R.id.stopTimer:
                stopTimer();
                pos.disconnectFromServer();
                Toast.makeText(getApplicationContext(), "Disabled refresh", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                stopTimer();
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
                .setMessage("Created by\n  " +
                        "Group 1\n  " +
                        "Service Engineering SS16\n  " +
                        "18.06.2016\n  " +
                        "Version 2.1")
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

    public boolean testConnection(String StringUrl) {
        try {
            URL url = new URL(StringUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(timeOut);
            Log.i("MAIN-TEST-CONNECTION", String.valueOf(con.getResponseCode()));
        }
        catch (Exception e) {
            Log.e("MAIN-TEST-CONNECTION", e.toString());
            stopTimer();
            new AlertDialog.Builder(context)
                    .setTitle("Unknown Host")
                    .setMessage("Could not connect to host.\n" +
                            "Do you want to reset settings to\n" +
                            "default and try again?")
                    .setPositiveButton("Yes, reset", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editor editor = sp.edit();
                            editor.putString("ipKey", getResources().getString(R.string.ip_default));
                            editor.putString("portKey", getResources().getString(R.string.port_default));
                            editor.commit();
                            JsonStringUrl = setJsonStringUrl();
                            connectionStatus = true;
                            startTimer();
                        }
                    }).setNegativeButton("No", null)
                    .show();
            return false;
        }
        return true;
    }
}
