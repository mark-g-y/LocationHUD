package com.locationhud.compassdirection;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Mark on 19/10/2014.
 */
public class CompassDirectionManager implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener,
        SensorEventListener {

    private static final int MAX_DIRECTION_HISTORY_SIZE = 7;

    private LocationClient locationClient;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] mGravity = null;
    private float[] mGeomagnetic = null;
    private double azimuth;
    private double tiltAngle = 0;
    private boolean isLocationConnected = false;
    private CompassDirectionFoundCallback compassDirectionFoundCallback;

    private ArrayList<Double> directionHistory = new ArrayList<Double>();
    private ArrayList<Double> tiltAngleHistory = new ArrayList<Double>();
    
    private Activity activity;
    
    public CompassDirectionManager(Activity activity, CompassDirectionFoundCallback compassDirectionFoundCallback) {
        this.activity = activity;
        this.compassDirectionFoundCallback = compassDirectionFoundCallback;
    }

    public Location getLastLocation() {
        if (isLocationConnected) {
            return locationClient.getLastLocation();
        }
        return null;
    }

    public static double getAngleBetweenCoordinates(double lat1, double long1, double lat2, double long2) {

        lat1 = Math.toRadians(lat1);
        long1 = Math.toRadians(long1);
        lat2 = Math.toRadians(lat2);
        long2 = Math.toRadians(long2);

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double bearing = Math.atan2(y, x);

        bearing = Math.toDegrees(bearing);
        bearing = (bearing + 360) % 360;
        //bearing = 360 - bearing;

        return bearing;
    }

    public static double getAngleBetweenAltitudes(Location myLocation, MapPoint poi) {
        double distance = getDistance(myLocation, poi);
        double heightDiff = poi.getAltitude() - myLocation.getAltitude();
        return distance == 0 ? 90 : Math.toDegrees(Math.atan(heightDiff / distance));
    }

    public double getTiltAngle() {
        return tiltAngle;
    }

    public void onCreate() {
        locationClient = new LocationClient(activity, this, this);
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        sensorManager = (SensorManager) activity.getSystemService(activity.getApplicationContext().SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void onStart() {
        locationClient.connect();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
    }

    public void onStop() {
        locationClient.disconnect();
        locationManager.removeUpdates(this);
    }

    public void onResume() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
        isLocationConnected = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());
        //Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show();
        isLocationConnected = true;
    }

    @Override
    public void onDisconnected() {
        isLocationConnected = false;
        Toast.makeText(activity, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(activity, connectionResult.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast.makeText(activity, "Status Changed - " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(activity, "Provider enabled " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(activity, "Provider disabled " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values.clone();
        }
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success && isLocationConnected) {
                float[] orientation = getOrientationValues(R);
                processPitchAngle(orientation.clone());
                processBearingAngle(orientation.clone());
            }
        }
    }

    private float[] getOrientationValues(float[] oldR) {
        float orientation[] = new float[3];
        float[] R = new float[9];
        SensorManager.remapCoordinateSystem(oldR, SensorManager.AXIS_X, SensorManager.AXIS_Z, R);
        SensorManager.getOrientation(R, orientation);
        return orientation;
    }

    private void processPitchAngle(float[] orientation) {
        double pitch = -Math.toDegrees(orientation[1]);
        tiltAngleHistory.add(pitch);
        tiltAngle = getRefinedAverage(tiltAngleHistory);
        tiltAngleHistory.clear();
    }

    private void processBearingAngle(float[] orientation) {
        float azimut = orientation[0];
        azimuth = Math.toDegrees(azimut);
        azimuth = convertMagneticNorthToTrueNorth(azimuth);
        directionHistory.add(azimuth);
        if (directionHistory.size() >= MAX_DIRECTION_HISTORY_SIZE) {
            azimuth = getRefinedAverage(directionHistory);
            directionHistory.clear();
            compassDirectionFoundCallback.onCompassDirectionFound(azimuth);
        }
    }

    private double convertMagneticNorthToTrueNorth(double azimuth) {
        final GeomagneticField geoField = new GeomagneticField(
                (float) locationClient.getLastLocation().getLatitude(),
                (float) locationClient.getLastLocation().getLongitude(),
                (float) locationClient.getLastLocation().getAltitude(),
                System.currentTimeMillis());
        azimuth += geoField.getDeclination(); // converts magnetic north into true north
        return azimuth;
    }

    public static double getDistance(Location myLocation, MapPoint poi) {
        float[] result = new float[3];
        Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(), poi.getLatitude(), poi.getLongitude(), result);
        return result[0];
    }

    private double getRefinedAverage(List<Double> list) {
        Collections.sort(list);
        int start = (int)Math.round(list.size() * 0.2);
        int end = (int) Math.round(list.size() * 0.8);
        return getAverage(list.subList(start, end));
    }

    private double getAverage(List<Double> list) {
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum+= list.get(i);
        }
        return list.size() > 0 ? sum / list.size() : 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
