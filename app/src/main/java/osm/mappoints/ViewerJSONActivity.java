package osm.mappoints;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ViewerJSONActivity extends AppCompatActivity {

    private static final String TAG = "ViewerJSONActivity";

    // Request parameteres
    private static final String KEY_PARAMETER_LOCATION = "location";
    private static final String KEY_PARAMETER_LONGITUDE = "longitude";
    private static final String KEY_PARAMETER_LATITUDE = "latitude";
    private static final String KEY_PARAMETER_RADIUS = "radius";

    private static final double long1 = 13.38187;
    private static final double lat1 = 52.514508;
    private static final double radius = 1000;

    private ArrayList<Station> loadedStations;

    private GeoPoint location = new GeoPoint(lat1, long1);


    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_j_s_o_n);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        tv = findViewById(R.id.jsonBody);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new BVGClientGetStations(ViewerJSONActivity.this).postToBVGToGetStationsNearby();

//                tv.setText(newText);
            }
        });
    }

    /**
     * Method for building testing json string
     * @return testing json string
     */
    public static String buildJsonString() {
        String jsonString = "";

        try {
            jsonString = new JSONObject()
                    .put(KEY_PARAMETER_LOCATION, new JSONObject().put(KEY_PARAMETER_LONGITUDE, long1)
                    .put(KEY_PARAMETER_LATITUDE, lat1)
                    .put(KEY_PARAMETER_RADIUS, radius))
                    .toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonString;
    }

    /**
     * Called according to result of response to get stations
     * @param stations
     */
    public void onClientPostResult(ArrayList<Station> stations) {
        if (stations == null) return;

        StringBuilder toDisplay = new StringBuilder("");
        toDisplay.append("Stations' size: ").append(stations.size()).append("\n");

        for (Station s : stations) {
            toDisplay.append(s.toString());
        }

        loadedStations = stations;


        int[] d = computeAverageDistanceBetweenCenterStationsAndRestOfTheStations(loadedStations);


        tv.setText("size of list:" + loadedStations.size() + " \naverage distance from center: " + d[0] + "meters\n" +
                "min value: " + d[1] + "meters\n" + "max value: " + d[2] + "meters");
    }

    private int[] computeAverageDistanceBetweenCenterStationsAndRestOfTheStations(ArrayList<Station> allStations) {

        int sumOfDistances = 0;
        int countOfStations = allStations.size() + 1;
        int min = 10000;
        int max = 0;

        for (Station s : allStations) {

            int currentDistance = MapUtils.distanceInMeters(location, s.getStationLocation());
            if (currentDistance < min) min = currentDistance;
            if (currentDistance > max) max = currentDistance;

            sumOfDistances += currentDistance;
        }

        int [] result = new int[3];

        double p = (double)sumOfDistances / (double) countOfStations;
        result[0] = (int)Math.floor(p);
        result[1] = min;
        result[2] = max;

        return result;
    }
}