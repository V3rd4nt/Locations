package serveng.jku.at.locations;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Peter on 26.05.2016.
 */
public class PositionCreator {
    String JsonStringUrl;
    JSONArray dataJsonArr;

    public GoogleMap createPositions (GoogleMap googleMap, SharedPreferences sp) {
        GoogleMap mMap = googleMap;
        mMap.clear();

         // set json string url
        JsonStringUrl = "http://"
                + sp.getString("ipKey", "skynet1.myds.me")
                + ":" + sp.getString("portKey", "2010")
                + "/";
        Log.d("NODEJS-SERVER-URL", JsonStringUrl);
        dataJsonArr = null;

        try {
            JsonParser jParser = new JsonParser();
            // get json string from url
            JSONObject json = jParser.getJSONFromUrl(JsonStringUrl);
            // get the array of points
            dataJsonArr = json.getJSONArray("points");
            // loop through all points
            int posNr;
            for (int i = 0; i < dataJsonArr.length(); i++) {
                JSONObject c = dataJsonArr.getJSONObject(i);
                // Storing each json item in LatLng variable
                LatLng position = new LatLng(c.getDouble("latitude"), c.getDouble("longitude"));
                // Add marker
                posNr = i + 1;
                Log.d("NEW-POSITION", "Pos " + posNr + " : " + position.toString());
                mMap.addMarker(new MarkerOptions().position(position).title(String.valueOf(posNr)));
            }
            //TODO: Clustering

            // move the camera over Linz in a specified zoom level
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(48.300412, 14.307861)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mMap;
    }
}
