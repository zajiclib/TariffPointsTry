package osm.mappoints;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;

import static osm.mappoints.BVGClientGetTariffPoints.KEY_PARAMETER_LATITUDE;
import static osm.mappoints.BVGClientGetTariffPoints.KEY_PARAMETER_LOCATION;
import static osm.mappoints.BVGClientGetTariffPoints.KEY_PARAMETER_LONGITUDE;
import static osm.mappoints.BVGClientGetTariffPoints.KEY_PARAMETER_RADIUS;


public class ChooseStartingStationActivity extends AppCompatActivity {

    private static final String TAG = "ChooseStartingStationActivity";
    private static final int LOCATION_INTENT = 100;

    private static final int DISTANCE_AWAY_OF_LAST_REQUEST_LOCATION = 900;
    private final int REQUEST_PERMISSION_LOCATION = 1;
    private final int REQUEST_PERMISSION_LOCATION_C = 2;
    private final double MAX_ZOOM_LEVEL = 18.7;
    private final double MIN_ZOOM_LEVEL = 11.5;
    private final double DEFAULT_ZOOM_LEVEL = 13.0;
    private final double DEFAULT_DETAIL_ZOOM_LEVEL = 17.0;
    private static final double RADIUS = 1000;

    private Marker currentLocatioMarker;

    private MapView mMapView;
    private MapController mMapController;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private GeoPoint mLocation;
    private GeoPoint checkingTestingPoint;

    private ArrayList<GeoPoint> geoPoints;
    private boolean firstDraw = true;
    private ArrayList<Station> stationsNearMe;
    private BoundingBox currentScreen;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stationsNearMe = new ArrayList<>();

        fab = findViewById(R.id.fab);

        mMapView = findViewById(R.id.map);
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
//        mMapView.setBuiltInZoomControls(true); // deprecated
        mMapView.setMultiTouchControls(true);

        currentLocatioMarker = new Marker(mMapView);
        currentLocatioMarker.setIcon(ContextCompat.getDrawable(ChooseStartingStationActivity.this, R.drawable.my_location));
        currentLocatioMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // TODO generating places
        // testing purposes
//        geoPoints = new ArrayList<>();
//        geoPoints = generateTestingGeoPoints();


        // maps do not load when commented - important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mMapController = (MapController) mMapView.getController();

        mMapView.setMaxZoomLevel(MAX_ZOOM_LEVEL);
        mMapView.setMinZoomLevel(MIN_ZOOM_LEVEL);
        mMapController.setZoom(DEFAULT_ZOOM_LEVEL);

        // center of berlin 52.5170625, 13.3886883
        mLocation = new GeoPoint(52.5170625, 13.3886883);
        checkingTestingPoint = mLocation;
        mMapController.setCenter(mLocation);


        mMapView.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {

                currentScreen = mMapView.getBoundingBox();

//                if (mLocation == null || currentScreen == null) return false;

                GeoPoint pointToShowNearbyStations = new GeoPoint(currentScreen.getCenterLatitude(), currentScreen.getCenterLongitude());

                if (MapUtils.distanceInMeters(pointToShowNearbyStations, checkingTestingPoint) > DISTANCE_AWAY_OF_LAST_REQUEST_LOCATION) {

                    new BVGClientGetTariffPoints(ChooseStartingStationActivity.this).postToBVGToGetStationsNearby(buildJsonString(pointToShowNearbyStations));
                    checkingTestingPoint = pointToShowNearbyStations;
                }

                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        }, 200));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TODO uncomment when all is done
                checkPermissionsAndGetLocationsUpdates();
//                checkingTestingPoint = new GeoPoint(52.5233039, 13.4297736);
//
//                currentLocatioMarker.setPosition(checkingTestingPoint);
//                mMapView.getOverlays().add(currentLocatioMarker);
//
//                mMapController.setCenter(checkingTestingPoint);
//                mMapController.setZoom(DEFAULT_DETAIL_ZOOM_LEVEL);
//
//                new BVGClientGetTariffPoints(ChooseStartingStationActivity.this).postToBVGToGetStationsNearby(buildJsonString(checkingTestingPoint));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for osm 6+
        mMapView.onResume();
    }

    private void checkPermissionsAndGetLocationsUpdates() {
        //Check permission
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // ask for location permission
            showPhoneStatePermission();
        }

        if (!isLocationManagerEnabled(locationManager)) {
//            Toast.makeText(activity, R.string.gps_please, Toast.LENGTH_LONG).show();
            enableLocationSettings();

        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates("network", 1000, 0, locationListener); //FUSED_PROVIDER, NETWORK_PROVIDER

        setLocationListener();
    }

    private void setLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//
                mLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                mMapController.setCenter(mLocation);
                // TODO later uncomment for getting real location
//                // in the first occurence
//                if (mLocation == null)  {
//                    mLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
//                    return;
//                }
//
//                GeoPoint newProvidedLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
//
//                if (MapUtils.distanceInMeters(newProvidedLocation, mLocation) > 500) {
//                    mLocation = newProvidedLocation;
//                }
//
//                mMapController.setCenter(mLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    public void drawStations() {

        for (int i = 0; i < stationsNearMe.size(); i++) {
            Station currentStation = stationsNearMe.get(i);
            Marker startMarker = new Marker(mMapView);
            startMarker.setIcon(ContextCompat.getDrawable(ChooseStartingStationActivity.this, R.drawable.station));
//                        startMarker.setImage(); icon in image
            startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    Toast.makeText(ChooseStartingStationActivity.this, "Clicked on station - " + currentStation.toString(), Toast.LENGTH_LONG).show();
                    return false;
                }
            });
            startMarker.setId(currentStation.getId());
            startMarker.setPosition(currentStation.getStationLocation());
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            currentStation.setCurrentlyDisplayed(true);
            mMapView.getOverlays().add(startMarker);
        }


    }

    public boolean isLocationManagerEnabled(LocationManager lManager) {
        boolean gps_enabled;
        //boolean network_enabled = false; //finding location via network is not working usually

        try {
            gps_enabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            return gps_enabled;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;

//        try {
//            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        } catch(Exception ex) {}

    }

    /**
     * This method handles the request for permission to use GPS for finding the location.
     */
    private void showPhoneStatePermission() {
        int permissionCheck;
        permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionCheck = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                    || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
                showExplanation("", "permission_location_explanation");
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_LOCATION);
                requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_LOCATION_C);
            }
        } else {
            Toast.makeText(this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method shows the explanation during the request for permission.
     *
     * @param title
     * @param message
     */
//   @param permission
//   @param permissionRequestCode
    private void showExplanation(String title,
                                 String message) { //final String permission,final int permissionRequestCode
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_LOCATION);
                        requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_LOCATION_C);
                        //requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method asks for the permission.
     *
     * @param permissionName        said permission
     * @param permissionRequestCode code
     */
    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    /**
     * uses fused location provider just to check whether the location is enabled
     * if location is not enabled - show native prompt to enable it with one click
     * if locatio is enabled -> nothing happens
     */
    public void enableLocationSettings() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new
                LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);


        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        ChooseStartingStationActivity.this,
                                        LOCATION_INTENT);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }

    /**
     * @return geopoints around Liberec location
     */
    private ArrayList<GeoPoint> generateTestingGeoPoints() {
        final double defaultPointLat = 50.77128781004221;
        final double defaulLongitude = 15.067679278266475;

        double latitudeDifference;
        double longitudeDifference;

        // making sure the points are unique
        HashMap<GeoPoint, String> geoPointStringHashMap = new HashMap<>();

        geoPointStringHashMap.put(new GeoPoint(defaultPointLat, defaulLongitude), "default");

        for (int i = 0; i < 1000; i++) {
            latitudeDifference = Math.random() / 80;
            longitudeDifference = Math.random() / 80;
            GeoPoint th;

            int rest = i % 4;

            if (rest == 0) {
                th = new GeoPoint(defaultPointLat + latitudeDifference, defaulLongitude + longitudeDifference);
            } else if (rest == 1) {
                th = new GeoPoint(defaultPointLat - latitudeDifference, defaulLongitude + longitudeDifference);
            } else if (rest == 2) {
                th = new GeoPoint(defaultPointLat + latitudeDifference, defaulLongitude - longitudeDifference);
            } else {
                th = new GeoPoint(defaultPointLat - latitudeDifference, defaulLongitude - longitudeDifference);
            }

            geoPointStringHashMap.put(th, i + "");
        }

        ArrayList<GeoPoint> generatedPoints = new ArrayList<>(geoPointStringHashMap.keySet());

        Log.d(TAG, "generateTestingGeoPoints: generated points count: " + generatedPoints.size());

        return generatedPoints;
    }

    /**
     * Called according to result of response to get stations
     *
     * @param stations stations gotten from bvg server response
     */
    public void onClientPostResult(ArrayList<Station> stations) {
        if (stations == null) return;

        stationsNearMe.addAll(stations);
        drawStations();
        Toast.makeText(this, "New stations added", Toast.LENGTH_SHORT).show();
    }

    /**
     * Method for building testing json string
     *
     * @return testing json string
     */
    public static String buildJsonString(GeoPoint currentWantedPosition) {
        String jsonString = "";

        try {
            jsonString = new JSONObject()
                    .put(KEY_PARAMETER_LOCATION, new JSONObject().put(KEY_PARAMETER_LONGITUDE, currentWantedPosition.getLongitude())
                            .put(KEY_PARAMETER_LATITUDE, currentWantedPosition.getLatitude())
                            .put(KEY_PARAMETER_RADIUS, RADIUS))
                    .toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonString;
    }
}