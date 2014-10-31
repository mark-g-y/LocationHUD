package com.locationhud.compassdirection;

import android.location.Location;

/**
 * Created by Mark on 23/10/2014.
 */
public interface MyLocationFoundCallback {
    public void onMyLocationFound(Location location);
    public void onMyLocationUnavailable();
}
