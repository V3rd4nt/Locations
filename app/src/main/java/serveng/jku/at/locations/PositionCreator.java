package serveng.jku.at.locations;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;

public class PositionCreator {
    JSONArray dataJsonArr;
    JSONObject json, c;
    ClusterManager<Position> clusterManager;
    JsonParser jParser = null;

    public GoogleMap createPositions (GoogleMap mMap, String JsonStringUrl, Context context, Timer timer, int timeOut) {
        mMap.clear();
        clusterManager = new ClusterManager<>(context, mMap);
        Log.d("NODEJS-SERVER-URL", JsonStringUrl);
        dataJsonArr = null;

        try {
            jParser = new JsonParser();
            // get json string from url
            json = jParser.getJSONFromUrl(JsonStringUrl, context, timer, timeOut);
            // get the array of points
            dataJsonArr = json.getJSONArray("points");
            // loop through all points

            for (int i = 0; i < dataJsonArr.length(); i++) {

                c = dataJsonArr.getJSONObject(i);
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

    public void disconnectFromServer() {
        if (jParser != null) {
            jParser.disconnect();
        }
    }
}
