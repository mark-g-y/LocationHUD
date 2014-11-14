package com.locationhud.compassdirection;

import com.locationhud.googleapi.retrievealtitude.AltitudeFoundCallback;
import com.locationhud.googleapi.retrievealtitude.RetrieveAltitudeTask;

/**
 * Created by Mark on 19/10/2014.
 */
public class MapPoint implements AltitudeFoundCallback {

    public static final String LIST_LABEL = "list";
    public static final String POI_LIST_LABEL = "poi_list";
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

    public void findAltitudeFromApi() {
        RetrieveAltitudeTask task = new RetrieveAltitudeTask(this, latitude, longitude);
        task.execute();
    }

    @Override
    public void onAltitudeFound(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public String toString() {
        return title + "|" + latitude + "|" + longitude + "|" + altitude;
    }
}
