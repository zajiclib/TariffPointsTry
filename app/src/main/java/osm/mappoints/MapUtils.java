package osm.mappoints;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

/**
 * Helper class with only static methods
 */
public class MapUtils {

    static final double _eQuatorialEarthRadius = 6378.1370D;
    static final double _d2r = (Math.PI / 180D);

    public static boolean isGeoPointInBoundingBox(BoundingBox boundingBox, GeoPoint geoPoint) {
        return boundingBox.contains(geoPoint);
    }

    /**
     * North west top point
     * @param boundingBox of passed bounding box
     * @return
     */
    public static GeoPoint getTopLeftGeoPoint(BoundingBox boundingBox) {
        return new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonWest());
    }

    /**
     * Get south east point
     * @param boundingBox of passed bounding box
     * @return bottom right GeoPoint
     */
    public static GeoPoint getBottomRightGeoPoint(BoundingBox boundingBox) {
        return new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonEast());
    }

    /**
     * Distance between north side and southside of bounding box
     * @param bb bounding box to compute it from
     * @return distance in meters
     */
    public static double distanceBetweenTwoPoints(BoundingBox bb) {
        double lat1 = bb.getLatNorth();
        double lon1 = bb.getActualNorth();
        double lat2 = bb.getLatSouth();
        double lon2 = bb.getActualSouth();

        return distanceBetweenTwoPoints(lat1, lon1, lat2, lon2);
    }


    /**
     * Compute distance between two GeoPoints
     * @param geoPoint1 First geo point
     * @param geoPoint2 First geo point
     * @return
     */
    public static double distanceBetweenTwoPoints(GeoPoint geoPoint1, GeoPoint geoPoint2) {
        return distanceBetweenTwoPoints(geoPoint1.getLatitude(), geoPoint1.getLongitude(), geoPoint2.getLatitude(), geoPoint2.getLongitude());
    }

    public static double distanceBetweenTwoPoints(double lat1, double long1, double lat2, double long2) {
        double dlong = (long2 - long1) * _d2r;
        double dlat = (lat2 - lat1) * _d2r;
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * _d2r) * Math.cos(lat2 * _d2r)
                * Math.pow(Math.sin(dlong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = _eQuatorialEarthRadius * c;

        return d;
    }


    /**
     *
     * @param lat1 latitude of first point
     * @param long1 longitude of first point
     * @param lat2 latitude of second point
     * @param long2 longitude of second point
     * @return distance between points
     */
    public static int distanceInMeters(double lat1, double long1, double lat2, double long2) {
        return (int) (1000D * distanceBetweenTwoPoints(lat1, long1, lat2, long2));
    }
}
