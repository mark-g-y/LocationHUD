package com.locationhud.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * Created by Mark on 17/11/2014.
 */
public class LocationScanningService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private LocationClient locationClient;
    private LocationManager locationManager;
    private boolean initialLocationRetrieved = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("FOO", "onCreate");
        locationClient = new LocationClient(getApplicationContext(), this, this);
        locationClient.connect();
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("FOO", "ON DESTORY");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("FOO", "onBind");
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("FOO", "location changed " + location.toString());
        if (!initialLocationRetrieved) {
            locationManager.removeUpdates(this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 360000, 0, this);
            initialLocationRetrieved = true;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("FOO", "status changed "  + s);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("FOO", "provided enabled" + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("FOO", "provider disabled" + s);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
