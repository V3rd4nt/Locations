package serveng.jku.at.locations;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.Timer;

public class JsonParser {

    static InputStream inputStream = null;
    static JSONObject jObj = null;
    static String json = "";
    HttpURLConnection urlConnection = null;
    boolean connection = true;

    public JSONObject getJSONFromUrl(String urlSource, Context context, Timer timer, int timeOut) {
        // Make HTTP request
        try {
            URL url = new URL(urlSource);
            urlConnection = (HttpURLConnection) url.openConnection();

            // Time to wait if server is unreachable until throwing exception
            urlConnection.setConnectTimeout(timeOut);
            // Http-GET Requesrt
            urlConnection.setRequestMethod("GET");
            urlConnection.setChunkedStreamingMode(0);
            inputStream = new BufferedInputStream(urlConnection.getInputStream());

        // Unsupported Character Encoding
        } catch (UnsupportedEncodingException e) {
            Log.e("JSON-PARSER-GET-JSON", e.toString());

        // Malformed IP-address and/or port or turned off Wifi or 3G/4G
        } catch (UnknownHostException e) {
            Log.e("JSON-PARSER-GET-JSON", e.toString());

            // Possible reason malformed input settings
            if (connection) {
                Log.w("JSON-PARSER-GET-JSON", "Setting urlSource to default: " +
                        "http://" + context.getResources().getString(R.string.ip_default) + ":" + context.getResources().getString(R.string.port_default) + "/");
                connection = false;

                // Try again with default settings
                getJSONFromUrl("http://" + context.getResources().getString(R.string.ip_default) + ":" + context.getResources().getString(R.string.port_default) + "/", context, timer, timeOut);

            // otherwise disable refresh and close connection
            } else {
                lostConnection(context, e, urlConnection, timer);
            }


        // Connection to server lost
        } catch (IOException e) {

            // Disable Refresh and close connection
            lostConnection(context, e, urlConnection, timer);
        }
        Log.d("JSON-PARSER-GET-JSON", "Connecting to server...");
        connection = true;

        //Read JSON data from inputStream
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();
            json = sb.toString();

        } catch (Exception e) {
            Log.e("JSON-PARSER-GET-JSON", "Error converting result " + e.toString());
        }

        // try to parse the string to a JSON object
        try {
            jObj = new JSONObject(json);

        } catch (JSONException e) {
            Log.e("JSON-PARSER-GET-JSON", "Error parsing data " + e.toString());
        }
        return jObj;// return JSON String
    }

    public void disconnect() {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    private void lostConnection(Context context, Exception e, HttpURLConnection urlConnection, Timer timer) {
        Log.e("JSON-PARSER-LOST-CON", e.toString());
        urlConnection.disconnect();
        Toast.makeText(context, "Lost connection to server", Toast.LENGTH_SHORT).show();
        timer.cancel();
    }
}