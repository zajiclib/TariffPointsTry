
package osm.mappoints;


import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BVGClientGetTariffPoints implements Callback {

    private static final String TAG = "BvgClientGetStations";
    private static final String LOG_TAG = "bvgclientgetstations";

    private static final String REQUEST_URL = "https://bvg-app-hosting.systemtechnik-online.de:8030/Search/FindTariffPoints";
    private static final int HTTP_REQUEST_TAG = 215;  //tag (id) to access the http request even after the call is made

    private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");

    // Request parameters
    public static final String KEY_PARAMETER_LOCATION = "location";
    public static final String KEY_PARAMETER_LONGITUDE = "longitude";
    public static final String KEY_PARAMETER_LATITUDE = "latitude";
    public static final String KEY_PARAMETER_RADIUS = "radius";


    // Response parameters
    public static final String KEY_PARAMETER_TARIFF_POINT_ID = "id";
    public static final String KEY_PARAMETER_TARIFF_POINT_NAME = "name";
    public static final String KEY_PARAMETER_ZONE_DESCRIPTION = "zoneShortDescription";


    private OkHttpClient okHttpClient;
    private ChooseStartingStationActivity activity;


    public BVGClientGetTariffPoints(ChooseStartingStationActivity activity) {
        this.activity = activity;
    }

    /**
     * Call appropriate bvg service provider
     */
    public void postToBVGToGetStationsNearby(String jsonString) {
        cancelOkHttpClientRequests();

        //Create new HTTP REST client
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

//        String jsonString = ViewerJSONActivity.buildJsonString();
        RequestBody postBody = RequestBody.create(jsonString, MEDIA_TYPE_JSON);

        //Create Http POST request
        Request httpRequest = new Request.Builder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                // .header("Authorization", "Base ZmU0MjQzOWEtN2EzNS00MmFlLWIxMTQtZjYyMjI0Zjk0ODkxQHRlbXAubG9jYWw=")
                .header("Authorization", "Base MGJiZmZiNTUtODM0Ny00ZDEwLTg0ZDYtZWYwYTVkYmI2ZjYxQHRlbXAubG9jYWw=") //keyword "Base " + base64 encoded account (0bbffb55-8347-4d10-84d6-ef0a5dbb6f61@temp.local)
                .header("Accept-Encoding", "gzip;q=1.0, compress;q=0.5")
                .header("x-sts-api-key", "Dh9uNjSk7vHTNIO33NTYNB3dfugsLsJw")
                // .header("Cache-Control", "no-cache")
                // .header("Connection", "close")
                .url(REQUEST_URL)
                .tag(HTTP_REQUEST_TAG)
                .post(postBody)
                .build();

        //Make asynchronous call
        okHttpClient.newCall(httpRequest).enqueue(this);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

        String msg; //received successful or error message

        //Get response string
        try {
            msg = Objects.requireNonNull(response.body().string());
            Log.i(TAG, "Response received: " + msg);

        } catch (IOException | NullPointerException e) {
            //ERROR
            e.printStackTrace();
            msg = e.getMessage();
            if (msg == null || msg.isEmpty())
                msg = "Unknown error occured."; // msg = activity.getString(R.string.id_bvg_rest_client_unknown_error_output_key_error_missing);
            handleErrorFeedback(msg);
            return;
        }

        if (!response.isSuccessful()) {
            //ERROR
            handleErrorFeedback("Operation not successful." + msg); // activity.getString(R.string.operation_not_successful) + msg
            return;
        }

        handleSuccessFeedback(getReceivedNearbyStations(msg));
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        handleErrorFeedback(e.toString());
    }

    private void handleSuccessFeedback(final ArrayList<Station> receivedStations) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Response received!", Toast.LENGTH_SHORT).show();
                activity.onClientPostResult(receivedStations);
            }
        });
    }

    private void handleErrorFeedback(final String errorMsg) {
        Log.e(TAG, errorMsg);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cancel all http client requests currently being called for no leak occurrence
     */
    public void cancelOkHttpClientRequests() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().cancelAll(); //Cancel all http client requests currently being called
            okHttpClient = null;
        }
    }

    private ArrayList<Station> getReceivedNearbyStations(String msg) {
        ArrayList<Station> stations = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(msg);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject station = jsonArray.getJSONObject(i);
                String id, name, zone;

                id = station.getString(KEY_PARAMETER_TARIFF_POINT_ID);
                name = station.getString(KEY_PARAMETER_TARIFF_POINT_NAME);
                zone = station.getString(KEY_PARAMETER_ZONE_DESCRIPTION);

                JSONObject location = station.getJSONObject(KEY_PARAMETER_LOCATION);

                double longitude = location.getDouble(KEY_PARAMETER_LONGITUDE);
                double latitude = location.getDouble(KEY_PARAMETER_LATITUDE);

                GeoPoint gp = new GeoPoint(latitude, longitude);

                stations.add(new Station(id, name, zone, gp));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stations;
    }
}
