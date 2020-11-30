package osm.mappoints;

import org.osmdroid.util.GeoPoint;

/**
 * Class representing station in
 */
public class Station {

    private String id;
    private String name;
    private String description;
    private String zone;
    private GeoPoint stationLocation;


    public Station(GeoPoint point) {
        this.stationLocation = point;
    }

    public Station(String id, String name, String description, String zone, GeoPoint stationLocation) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.zone = zone;
        this.stationLocation = stationLocation;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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


}
