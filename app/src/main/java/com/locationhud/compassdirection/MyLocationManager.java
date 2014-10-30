package com.locationhud.compassdirection;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * Created by Mark on 23/10/2014.
 */
public class MyLocationManager implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private Activity activity;
    private MyLocationFoundCallback myLocationFoundCallback = null;
    private LocationClient locationClient;
    private LocationManager locationManager;
    private boolean isLocationConnected = false;

    public MyLocationManager(Activity activity) {
        this.activity = activity;
    }

    public MyLocationManager(Activity activity, MyLocationFoundCallback myLocationFoundCallback) {
        this.activity = activity;
        this.myLocationFoundCallback = myLocationFoundCallback;
    }

    public void onCreate() {
        locationClient = new LocationClient(activity, this, this);
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    public void onStart() {
        locationClient.connect();
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
    }

    public void onStop() {
        locationClient.disconnect();
        locationManager.removeUpdates(this);
    }

    public void onResume() {
    }

    public void onPause() {
        isLocationConnected = false;
    }

    public Location getLastLocation() {
        if (isLocationConnected) {
            return locationClient.getLastLocation();
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        isLocationConnected = true;
        if (myLocationFoundCallback != null) {
            myLocationFoundCallback.onMyLocationFound(locationClient.getLastLocation());
        }
    }

    @Override
    public void onDisconnected() {
        isLocationConnected = false;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
