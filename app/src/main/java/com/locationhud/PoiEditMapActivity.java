package com.locationhud;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.locationhud.compassdirection.MapPoint;
import com.locationhud.compassdirection.MyLocationFoundCallback;
import com.locationhud.compassdirection.MyLocationManager;
import com.locationhud.map.ConfirmSelectedLocationDialog;
import com.locationhud.map.ConfirmSelectedLocationDialogCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Mark on 23/10/2014.
 */
public class PoiEditMapActivity extends FragmentActivity implements MyLocationFoundCallback, ConfirmSelectedLocationDialogCallback {

    private Activity myActivity;
    private MyLocationManager locationManager;
    private GoogleMap map;
    private HashMap<Marker, MapPoint> markerToPoiMap = new HashMap<Marker, MapPoint>();
    private Marker lastAddedMarker;
    private LatLng lastLongClickLocation;
    private boolean saveMapZoomState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_edit_map);
        myActivity = this;

        locationManager = new MyLocationManager(this, this);
        locationManager.onCreate();

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();

        final ConfirmSelectedLocationDialogCallback callback = this;

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLong) {
                lastAddedMarker = map.addMarker(new MarkerOptions().position(latLong));
                lastLongClickLocation = latLong;
                displayEditMarkerDialog(lastAddedMarker, "", myActivity.getString(R.string.save), myActivity.getString(R.string.cancel));
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (markerToPoiMap.get(marker) != null) {
                    String name = markerToPoiMap.get(marker).getTitle();
                    lastAddedMarker = marker;
                    lastLongClickLocation = marker.getPosition();
                    displayEditMarkerDialog(marker, name, myActivity.getString(R.string.save), myActivity.getString(R.string.delete));
                }
                return false;
            }
        });

        PoiManager.readLocationsFromFile(getApplicationContext());
        addLocationsInCurrentListToMap();

        ImageButton navigationArrowForwardButton = (ImageButton)findViewById(R.id.navigation_arrow_forward);
        navigationArrowForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(myActivity, HudActivity.class);
                startActivity(intent);
            }
        });
    }

    private void displayEditMarkerDialog(Marker marker, String name, String yesButtonText, String noButtonText) {
        ConfirmSelectedLocationDialog dialog = new ConfirmSelectedLocationDialog();
        dialog.initiate(this, marker.getPosition(), name, yesButtonText, noButtonText);
        dialog.show(getSupportFragmentManager(), marker.getPosition().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        locationManager.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.onPause();
        updatePoiManager();
    }

    @Override
    public void onStop() {
        super.onStop();
        locationManager.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PoiManager.saveLocationsToFile(getApplicationContext());
    }

    @Override
    public void onMyLocationFound(Location location) {
        if (!saveMapZoomState) {
            saveMapZoomState = true;
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationManager.getLastLocation().getLatitude(), locationManager.getLastLocation().getLongitude()), 12));
        }
     }

    @Override
    public void onCancel() {
        if (markerToPoiMap.get(lastAddedMarker) == null) {
            lastAddedMarker.remove();
            markerToPoiMap.remove(lastAddedMarker);
        }
    }

    @Override
    public void onYes(String data) {
        MapPoint mapPoint = new MapPoint(data, lastLongClickLocation.latitude, lastLongClickLocation.longitude);
        markerToPoiMap.put(lastAddedMarker, mapPoint);
    }

    private void addLocationsInCurrentListToMap() {
        ArrayList<MapPoint> list = PoiManager.getList(PoiManager.getCurrentList());
        for (int i = 0; i < list.size(); i++) {
            MapPoint mp = list.get(i);
            Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(mp.getLatitude(), mp.getLongitude())));
            markerToPoiMap.put(marker, mp);
        }
    }

    private void updatePoiManager() {
        ArrayList<MapPoint> list = new ArrayList<MapPoint>();
        Collection values = markerToPoiMap.values();
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            list.add((MapPoint)iterator.next());
        }
        PoiManager.setList(PoiManager.getCurrentList(), list);
    }
}
