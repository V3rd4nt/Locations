package serveng.jku.at.locations;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PositionCreator {
    String JsonStringUrl;
    JSONArray dataJsonArr;
    ClusterManager<Position> clusterManager;

    public GoogleMap createPositions (GoogleMap mMap, SharedPreferences sp, Context context) {
        mMap.clear();
        clusterManager = new ClusterManager<>(context, mMap);

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

            for (int i = 0; i < dataJsonArr.length(); i++) {

                JSONObject c = dataJsonArr.getJSONObject(i);
                // Store each json item in Position object
                Position position = new Position(c.getDouble("latitude"), c.getDouble("longitude"));
                // Add position to the cluster
                clusterManager.addItem(position);
            }
            Log.d("CLUSTERMANAGER", "All Positions successfully set");
            // updates the cluster at each camera change
            mMap.setOnCameraChangeListener(clusterManager);
            // force recluster after each refresh interval
            clusterManager.cluster();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mMap;
    }
}
