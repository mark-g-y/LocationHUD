package com.locationhud.compassdirection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.locationhud.R;

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
    private boolean isLocationServicesOn = false;
    private int timeBeforeLocationUpdate = -1;

    public MyLocationManager(Activity activity) {
        this.activity = activity;
    }

    public MyLocationManager(Activity activity, MyLocationFoundCallback myLocationFoundCallback, int timeBeforeLocationUpdate) {
        this.activity = activity;
        this.myLocationFoundCallback = myLocationFoundCallback;
        this.timeBeforeLocationUpdate = timeBeforeLocationUpdate;
    }

    public void onCreate() {
        isLocationServicesOn = isLocationServicesOn(activity);
        if (isLocationServicesOn) {
            locationClient = new LocationClient(activity, this, this);
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void onStart() {
        if (isLocationServicesOn) {
            locationClient.connect();
            if(timeBeforeLocationUpdate > 0) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
            }
        }
    }

    public void onStop() {
        if (isLocationServicesOn) {
            locationClient.disconnect();
            locationManager.removeUpdates(this);
        }
    }

    public void onResume() {
    }

    public void onPause() {
        isLocationConnected = false;
    }

    public Location getLastLocation() {
        if (isLocationConnected) {
            Location location = locationClient.getLastLocation();
            //Converter converter = Converter.getInstance();
            //double altitude = converter.getHeightFromLatitudeAndLongitude(location.getLatitude(), location.getLongitude());
            //location.setAltitude(altitude);
            //Log.d("ASDFASDFDSFFOO", "" + altitude);
            return location;
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

    public double convertMagneticNorthToTrueNorth(double azimuth) {
        final GeomagneticField geoField = new GeomagneticField(
                (float) locationClient.getLastLocation().getLatitude(),
                (float) locationClient.getLastLocation().getLongitude(),
                (float) locationClient.getLastLocation().getAltitude(),
                System.currentTimeMillis());
        azimuth += geoField.getDeclination(); // converts magnetic north into true north
        return azimuth;
    }

    public static boolean isLocationServicesOn(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            return false;
        }
        return true;
    }

    public static void promptUserTurnOnLocation(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.gps_not_found_title);  // GPS not found
        builder.setMessage(R.string.gps_want_to_enable_message); // Want to enable?
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                activity.finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.finish();
            }
        });
        builder.create().show();
    }
}
