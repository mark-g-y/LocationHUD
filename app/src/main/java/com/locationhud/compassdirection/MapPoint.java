package com.locationhud.compassdirection;

/**
 * Created by Mark on 19/10/2014.
 */
public class MapPoint {

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
}
