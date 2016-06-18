package serveng.jku.at.locations;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;
import java.io.*;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.Timer;

public class JsonParser {

    static InputStream inputStream = null;
    static JSONObject jObj = null;
    static String json = "";
    HttpURLConnection con = null;
    boolean connection = true;

    public JSONObject getJSONFromUrl(String urlSource, Context context, Timer timer, int timeOut) {
        // Make HTTP request
        try {
            URL url = new URL(urlSource);
            con = (HttpURLConnection) url.openConnection();

            // Time to wait if server is unreachable until throwing exception
            con.setReadTimeout(timeOut);
            con.setConnectTimeout(timeOut);

            // Http-POST Request
            con.setRequestMethod("POST");
            con.setChunkedStreamingMode(0);
            con.setDoInput(true);
            con.setDoOutput(true);

            // Creating query to be sent
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("username", "locations")
                    .appendQueryParameter("password", "servengss16");
            String query = builder.build().getEncodedQuery();

            OutputStream os = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            // Send query - username + password
            writer.write(query);
            writer.flush();
            writer.close();

            // Show Respond message
            Log.d("JSON-PARSER", con.getResponseMessage());

            inputStream = new BufferedInputStream(con.getInputStream());

        // Unsupported Character Encoding
        } catch (UnsupportedEncodingException e) {
            Log.e("JSON-PARSER", e.toString());

        // Malformed IP-address and/or port or turned off Wifi or 3G/4G
        } catch (UnknownHostException e) {
            Log.e("JSON-PARSER", e.toString());

            // Possible reason malformed input settings
            if (connection) {
                Log.w("JSON-PARSER", "Setting urlSource to default: " +
                        "http://" + context.getResources().getString(R.string.ip_default) + ":" + context.getResources().getString(R.string.port_default) + "/");
                connection = false;

                // Try again with default settings
                getJSONFromUrl("http://" + context.getResources().getString(R.string.ip_default) + ":" + context.getResources().getString(R.string.port_default)
                        + "/", context, timer, timeOut);

            // otherwise disable refresh and close connection
            } else {
                lostConnection(context, e, con, timer);
            }


        // Connection to server lost
        } catch (IOException e) {

            // Disable Refresh and close connection
            lostConnection(context, e, con, timer);
        }
        Log.d("JSON-PARSER", "Connecting to server...");
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
        if (con != null) {
            con.disconnect();
        }
    }

    private void lostConnection(Context context, Exception e, HttpURLConnection con, Timer timer) {
        Log.e("JSON-PARSER", e.toString());
        con.disconnect();
        Toast.makeText(context, "Lost connection to server", Toast.LENGTH_SHORT).show();
        timer.cancel();
    }
}