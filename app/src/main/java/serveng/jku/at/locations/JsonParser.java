package serveng.jku.at.locations;

import android.content.Context;
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
    final String URL_SOURCE_DEFAULT = "http://skynet1.myds.me:2010/";
    HttpURLConnection urlConnection = null;
    SharedPreferences sp;

    public JSONObject getJSONFromUrl(String urlSource, Context context, Timer timer) {
        // Load shared preferences to get timeout value
        sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Make HTTP request
        try {
            URL url = new URL(urlSource);
            urlConnection = (HttpURLConnection) url.openConnection();

            // Time to wait if server is unreachable until throwing exception
            urlConnection.setConnectTimeout(sp.getInt("timeOutValue", 3000));
            // Http-GET Requesrt
            urlConnection.setRequestMethod("GET");
            urlConnection.setChunkedStreamingMode(0);
            inputStream = new BufferedInputStream(urlConnection.getInputStream());

        // Unsupported Character Encoding
        } catch (UnsupportedEncodingException e) {
            Log.e("JSON-PARSER", e.toString());

        // Malformer IP-address and/or port
        } catch (UnknownHostException e) {
            Log.e("JSON-PARSER", e.toString());
            Log.w("JSON-PARSER", "Setting urlSource to default: " + URL_SOURCE_DEFAULT);
            getJSONFromUrl(URL_SOURCE_DEFAULT, context, timer);

        // Connection to server lost
        } catch (IOException e) {
            Log.e("JSON-PARSER", e.toString());
            urlConnection.disconnect();
            Toast.makeText(context, "Lost connection to server", Toast.LENGTH_SHORT).show();
            timer.cancel();
        }
        Log.d("JSON-PARSER", "Connected to the server");

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
            Log.e("JSON-PARSER", "Error converting result " + e.toString());
        }

        // try to parse the string to a JSON object
        try {
            jObj = new JSONObject(json);

        } catch (JSONException e) {
            Log.e("JSON-PARSER", "Error parsing data " + e.toString());
        }
        return jObj;// return JSON String
    }

    public void disconnect() {
        if (urlConnection != null) {
            urlConnection.disconnect();
            Log.d("JSON-PARSER", "Disconnecting from server successful");
        } else Log.w("JSON-PARSER", "Disconnecting from server failed");
    }
}