package com.locationhud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.locationhud.compassdirection.MapPoint;
import com.locationhud.compassdirection.MyLocationFoundCallback;
import com.locationhud.compassdirection.MyLocationManager;
import com.locationhud.map.ConfirmSelectedLocationDialog;
import com.locationhud.map.ConfirmSelectedLocationDialogCallback;
import com.locationhud.storage.SharedPreferencesStorage;
import com.locationhud.ui.AnimationFactory;
import com.locationhud.ui.UiUtility;
import com.locationhud.utility.IntentTransferCodes;

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
    private boolean isLocationServicesOn = false;
    private boolean saveMapZoomState = false;
    private String poiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_edit_map);
        myActivity = this;

        poiList = getIntent().getStringExtra(IntentTransferCodes.CURRENT_POI_LIST);
        if (poiList == null) {
            poiList = "Default";
        }

        boolean instructionsViewed = SharedPreferencesStorage.isInstructionsRead(getApplicationContext());
        if (instructionsViewed) {
            hideInstructions();
        } else {
            final Button confirmInstructionsReadButton = (Button)findViewById(R.id.button_instructions_read);
            confirmInstructionsReadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferencesStorage.setInstructionsRead(getApplicationContext(), true);
                    hideInstructions();
                }
            });
            UiUtility.setOnTouchColourChanges(confirmInstructionsReadButton, android.R.color.transparent, R.color.item_pressed);
        }

        isLocationServicesOn = MyLocationManager.isLocationServicesOn(this);
        locationManager = new MyLocationManager(this, this, -1);
        locationManager.onCreate();

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLong) {
                lastAddedMarker = map.addMarker(new MarkerOptions().position(latLong).draggable(true));
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
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
            @Override
            public void onMarkerDrag(Marker marker) {
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                markerToPoiMap.get(marker).updateLocation(marker.getPosition().latitude, marker.getPosition().longitude);
            }
        });
        saveMapZoomState = SharedPreferencesStorage.loadMapCamera(getApplicationContext(), map);

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
        SharedPreferencesStorage.saveMapCamera(getApplicationContext(), map);
    }

    @Override
    public void onMyLocationFound(Location location) {
        if (!saveMapZoomState) {
            saveMapZoomState = true;
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationManager.getLastLocation().getLatitude(), locationManager.getLastLocation().getLongitude()), 12));
        }
     }

    @Override
    public void onMyLocationUnavailable() {

    }

    @Override
    public void onCancel() {
        if (markerToPoiMap.get(lastAddedMarker) == null) {
            removeLastAddedMarker();
        }
    }

    @Override
    public void onDelete() {
        removeLastAddedMarker();
    }

    @Override
    public void onYes(String data) {
        MapPoint mapPoint = new MapPoint(data, lastLongClickLocation.latitude, lastLongClickLocation.longitude);
        if (markerToPoiMap.get(lastAddedMarker) == null) {
            mapPoint.findAltitudeFromApi();
        }
        markerToPoiMap.put(lastAddedMarker, mapPoint);
    }

    private void removeLastAddedMarker() {
        lastAddedMarker.remove();
        markerToPoiMap.remove(lastAddedMarker);
    }

    private void addLocationsInCurrentListToMap() {
        ArrayList<MapPoint> list = PoiManager.getList(poiList);
        for (int i = 0; i < list.size(); i++) {
            MapPoint mp = list.get(i);
            Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(mp.getLatitude(), mp.getLongitude())).draggable(true));
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
        PoiManager.setList(poiList, list);
    }

    private void hideInstructions() {
        final View instructionsView = findViewById(R.id.edit_poi_map_instructions);
        TranslateAnimation animationHide = AnimationFactory.buildTranslateAnimation(instructionsView, 0, instructionsView.getHeight(), 250);
        instructionsView.startAnimation(animationHide);
        final Handler hideViewAfterAnimationHandler = new Handler();
        hideViewAfterAnimationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        instructionsView.setVisibility(View.GONE);
                    }
                });
            }
        }, 150);
    }
}
