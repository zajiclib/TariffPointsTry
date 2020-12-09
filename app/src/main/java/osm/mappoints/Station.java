package osm.mappoints;

import androidx.annotation.NonNull;

import org.osmdroid.util.GeoPoint;

/**
 * Class representing station in
 */
public class Station {

    private String id;
    private String name;

    private String zone;
    private String zoneId;
    private GeoPoint stationLocation;
    private boolean isCurrentlyDisplayed = false;


    public Station(GeoPoint point) {
        this.stationLocation = point;
    }

    public Station(String id, String name, String zone, GeoPoint stationLocation) {
        this.id = id;
        this.name = name;
        this.zone = zone;
        this.stationLocation = stationLocation;
    }

    public boolean isCurrentlyDisplayed() {
        return isCurrentlyDisplayed;
    }

    public void setCurrentlyDisplayed(boolean currentlyDisplayed) {
        this.isCurrentlyDisplayed = currentlyDisplayed;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public GeoPoint getStationLocation() {
        return stationLocation;
    }

    public void setStationLocation(GeoPoint stationLocation) {
        this.stationLocation = stationLocation;
    }

    @NonNull
    @Override
    public String toString() {
        return "id: "+ id + "; name: " + name + "; zone: " + zone + "\n";
    }
}
