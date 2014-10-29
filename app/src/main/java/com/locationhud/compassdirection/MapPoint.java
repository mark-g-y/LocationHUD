package com.locationhud.compassdirection;

/**
 * Created by Mark on 19/10/2014.
 */
public class MapPoint {

    public static final String LIST_LABEL = "list";
    public static final String TITLE_LABEL = "title";
    public static final String LATITUDE_LABEL = "latitude";
    public static final String LONGITUDE_LABEL = "longitude";
    public static final String ALTITUDE_LABEL = "altitude";

    private String title;
    private double longitude;
    private double latitude;
    private double altitude;

    public MapPoint(String title, double latitude, double longitude) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MapPoint(String title, double latitude, double longitude, double altitude) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public String getTitle() {
        return title;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
