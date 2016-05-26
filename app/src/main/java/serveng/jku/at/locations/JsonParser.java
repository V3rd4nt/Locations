package serveng.jku.at.locations;

/**
 * Created by Peter on 26.05.2016.
 */

import android.util.Log;

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

public class JsonParser {
    static InputStream inputStream = null;
    static JSONObject jObj = null;
    static String json = "";
    final String URL_SOURCE_DEFAULT = "http://skynet1.myds.me:2010/";

    public JSONObject getJSONFromUrl(String urlSource) {
        //make HTTP request
        try {
            URL url = new URL(urlSource);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            // set true for POST, false for GET
            urlConnection.setDoOutput(false);
            urlConnection.setChunkedStreamingMode(0);
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
        } catch (UnsupportedEncodingException e) {
            Log.e("JSON-PARSER", e.toString());
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.e("JSON-PARSER", e.toString());
            Log.w("JSON-PARSER", "Setting urlSource to default: " + URL_SOURCE_DEFAULT);
            getJSONFromUrl(URL_SOURCE_DEFAULT);
        } catch (IOException e) {
            Log.e("JSON-PARSER", e.toString());
        }

        //Read JSON data from inputStream
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("JSON-PARSER", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON-PARSER", "Error parsing data " + e.toString());
        }
        return jObj;// return JSON String
    }
}