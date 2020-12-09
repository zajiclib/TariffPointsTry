package osm.mappoints;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
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


public class ChooseStartingStationActivity extends AppCompatActivity {

    private static final String TAG = "ChooseStartingStationActivity";
    private MapView mMapView;
    private MapController mMapController;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private GeoPoint mLocation;

    private ArrayList<GeoPoint> geoPoints;
    private boolean firstDraw = true;
    private ArrayList<Station> stationsNearMe;
    private BoundingBox currentScreen;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);

        mMapView = findViewById(R.id.map);
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
//        mMapView.setBuiltInZoomControls(true); // deprecated
        mMapView.setMultiTouchControls(true);

        // TODO generating places
        // testing purposes
//        geoPoints = new ArrayList<>();
//        geoPoints = generateTestingGeoPoints();


        parseStationsNearMeIntoArrayList(true);

        Log.d(TAG, "onCreate: StationsArrayList size - " + stationsNearMe.size());

        // maps do not load when commented - important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(19);
        mLocation = new GeoPoint(52.51918121785197, 13.384318801552391);
        mMapController.setCenter(mLocation);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("network", 1000, 0, locationListener); //FUSED_PROVIDER, NETWORK_PROVIDER

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

//        Log.d(TAG, "onCreate: " + mMapView.getZoomLevelDouble());
//
//        Log.d(TAG, "onCreate: stations near me count: " + geoPoints.size());


        mMapView.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {

                currentScreen = mMapView.getBoundingBox();

                if (mLocation == null || currentScreen == null) return false;


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

                Intent intent = new Intent(ChooseStartingStationActivity.this, ViewerJSONActivity.class);

                startActivity(intent);

//                if (currentScreen == null) return;
//
//                Toast.makeText(ChooseStartingStationActivity.this, "distance - " + MapUtils.distanceBetweenTwoPoints(currentScreen), Toast.LENGTH_SHORT).show();
//                Marker startMarker = new Marker(mMapView);
//                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//
//                GeoPoint point = MapUtils.getTopLeftGeoPoint(currentScreen);
//                startMarker.setPosition(point);
//                startMarker.setIcon(ContextCompat.getDrawable(ChooseStartingStationActivity.this, R.drawable.map_point));
//                mMapView.getOverlays().add(startMarker);
//
//
//                Marker startMarker2 = new Marker(mMapView);
//                startMarker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                GeoPoint newGp = MapUtils.getBottomRightGeoPoint(currentScreen);
//                startMarker2.setPosition(newGp);
//                startMarker2.setIcon(ContextCompat.getDrawable(ChooseStartingStationActivity.this, R.drawable.map_point));
//
//                mMapView.getOverlays().add(startMarker2);
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

    public void drawStations() {
        if (firstDraw) {
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

            firstDraw = false;
        }
    }

    // 50.77128781004221, 15.058581226261822
    // 50.771396371584174, 15.067679278266475

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
     * @param json testing json string
     */
    public void onClientPostResult(String json) {
        if (json == null) return;

        Log.d(TAG, "onClientPostResult: " + json);
    }


    /**
     * Parsing json into arraylist of {@link Station}
     * from response data
     */
    public void parseStationsNearMeIntoArrayList(boolean parseForStations) {

        stationsNearMe = new ArrayList<>();

        String strArray = getJsonArrayString();

        try {
            JSONArray jsonArray = new JSONArray(strArray);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject station = jsonArray.getJSONObject(i);
                String id, name, description, zone;

                id = station.getString("id");
                name = station.getString("name");
                zone = station.getString("zoneShortDescription");

                JSONObject location = station.getJSONObject("location");

                double longitude = location.getDouble("longitude");
                double latitude = location.getDouble("latitude");

                GeoPoint gp = new GeoPoint(latitude, longitude);

                stationsNearMe.add(new Station(id, name, zone, gp));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returning json string
     *
     * @return String with json data taken from response
     */
    private String getJsonArrayString() {
        String p = "[\n" +
                "  {\n" +
                "    \"id\": \"9004104\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Reichstag/Bundestag (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.37648,\n" +
                "      \"latitude\": 52.517716,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100025\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Brandenburger Tor (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"CityTrain\",\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.381937,\n" +
                "      \"latitude\": 52.516511,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9003254\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Bundestag (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.372952,\n" +
                "      \"latitude\": 52.52011,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100543\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Behrenstraße/Wilhelmstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.38183,\n" +
                "      \"latitude\": 52.514508,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100520\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Marschallbrücke (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.379861,\n" +
                "      \"latitude\": 52.521246,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100037\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Brandenburger Tor/Glinkastraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.384094,\n" +
                "      \"latitude\": 52.516702,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9004105\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Platz der Republik (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.370442,\n" +
                "      \"latitude\": 52.517786,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100001\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Friedrichstraße Bhf (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"CityTrain\",\n" +
                "      \"Ice\",\n" +
                "      \"InterRegional\",\n" +
                "      \"Metro\",\n" +
                "      \"RegionalExpress\",\n" +
                "      \"RegionalTrain\",\n" +
                "      \"Tram\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.387153,\n" +
                "      \"latitude\": 52.52027,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100509\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Deutsches Theater (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.383265,\n" +
                "      \"latitude\": 52.523128,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100033\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Schumannstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.379389,\n" +
                "      \"latitude\": 52.524054,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100513\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Unter den Linden/Friedrichstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.388876,\n" +
                "      \"latitude\": 52.516996,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100010\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Mohrenstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.383798,\n" +
                "      \"latitude\": 52.511519,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100022\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S Potsdamer Platz Bhf/Voßstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.376656,\n" +
                "      \"latitude\": 52.510147,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100027\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Französische Straße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.389208,\n" +
                "      \"latitude\": 52.51477,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9003205\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Washingtonplatz/Hauptbahnhof (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Ice\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.370708,\n" +
                "      \"latitude\": 52.523966,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100721\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Potsdamer Platz (Bln) [Bus Leipziger Straße]\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.378288,\n" +
                "      \"latitude\": 52.509607,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100547\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Mohrenstraße/Glinkastraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.38661,\n" +
                "      \"latitude\": 52.511936,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100028\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Am Weidendamm (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.388457,\n" +
                "      \"latitude\": 52.521917,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100020\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Potsdamer Platz Bhf (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"CityTrain\",\n" +
                "      \"RegionalExpress\",\n" +
                "      \"RegionalTrain\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.376454,\n" +
                "      \"latitude\": 52.50934,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9005208\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Varian-Fry-Straße/Potsdamer Platz (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.374302,\n" +
                "      \"latitude\": 52.509293,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100720\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Potsdamer Platz (Bln) [U2]\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.377977,\n" +
                "      \"latitude\": 52.509071,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100542\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Charité - Campus Mitte (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.379089,\n" +
                "      \"latitude\": 52.525756,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100535\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Leipziger Straße/Wilhelmstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.384207,\n" +
                "      \"latitude\": 52.510005,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100047\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Friedrichstraße/Reinhardtstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.386834,\n" +
                "      \"latitude\": 52.523707,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9003253\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Haus der Kulturen der Welt (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.363364,\n" +
                "      \"latitude\": 52.517598,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100722\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Potsdamer Platz (Bln) [Bus Stresemannstraße]\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"Ice\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.37723,\n" +
                "      \"latitude\": 52.50872,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100540\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Universitätsstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Tram\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.391841,\n" +
                "      \"latitude\": 52.518974,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9003200\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Berlin Hauptbahnhof (tief)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Ice\",\n" +
                "      \"RegionalExpress\",\n" +
                "      \"RegionalTrain\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.369386,\n" +
                "      \"latitude\": 52.525042,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100701\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Stadtmitte U2 (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.389711,\n" +
                "      \"latitude\": 52.512169,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100719\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Oranienburger Tor (Berlin) [Tram/Bus]\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"Tram\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.387644,\n" +
                "      \"latitude\": 52.524641,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100011\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Stadtmitte (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.389719,\n" +
                "      \"latitude\": 52.511495,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9003201\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"S+U Berlin Hauptbahnhof\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\",\n" +
                "      \"CityTrain\",\n" +
                "      \"Ice\",\n" +
                "      \"InterRegional\",\n" +
                "      \"Metro\",\n" +
                "      \"RegionalExpress\",\n" +
                "      \"RegionalTrain\",\n" +
                "      \"Tram\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.369075,\n" +
                "      \"latitude\": 52.525605,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9005207\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Philharmonie Süd (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.370024,\n" +
                "      \"latitude\": 52.508761,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100700\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Stadtmitte U6 (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.389827,\n" +
                "      \"latitude\": 52.511179,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100702\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Stadtmitte (Ersatzverkehr) (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.389827,\n" +
                "      \"latitude\": 52.511179,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100019\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Oranienburger Tor (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Metro\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.387587,\n" +
                "      \"latitude\": 52.525163,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9005206\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Philharmonie (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.368444,\n" +
                "      \"latitude\": 52.508954,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9003255\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Clara-Jaschke-Straße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Tram\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.366052,\n" +
                "      \"latitude\": 52.524928,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100528\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Stadtmitte [3-4] (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.389918,\n" +
                "      \"latitude\": 52.510324,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100819\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Oranienburger Tor (Berlin) [Linienstraße]\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Tram\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.38825,\n" +
                "      \"latitude\": 52.526099,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9001204\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"Lehrter Straße/Invalidenstraße (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.365223,\n" +
                "      \"latitude\": 52.525764,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"9100018\",\n" +
                "    \"ifopt\": \"\",\n" +
                "    \"name\": \"U Stadtmitte [5] (Berlin)\",\n" +
                "    \"type\": \"Station\",\n" +
                "    \"modesOfTransport\": [\n" +
                "      \"Bus\"\n" +
                "    ],\n" +
                "    \"location\": {\n" +
                "      \"longitude\": 13.390807,\n" +
                "      \"latitude\": 52.50955,\n" +
                "      \"radius\": 0\n" +
                "    },\n" +
                "    \"zoneId\": \"5555\",\n" +
                "    \"zoneDescription\": \"Berlin AB\",\n" +
                "    \"zoneShortDescription\": \"Berlin AB\"\n" +
                "  }\n" +
                "]";

        return p;
    }
}