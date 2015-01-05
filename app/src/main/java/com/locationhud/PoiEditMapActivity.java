package com.locationhud;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.locationhud.compassdirection.MapPoint;
import com.locationhud.compassdirection.MyLocationFoundCallback;
import com.locationhud.compassdirection.MyLocationManager;
import com.locationhud.googleapi.placesautocomplete.PlacesAutoCompleteAdapter;
import com.locationhud.googleapi.placesdetail.LatitudeLongitudeFoundCallback;
import com.locationhud.googleapi.placesdetail.LatitudeLongitudeRetrievalTask;
import com.locationhud.map.ConfirmSelectedLocationDialog;
import com.locationhud.map.ConfirmSelectedLocationDialogCallback;
import com.locationhud.parseapi.AuthenticationData;
import com.locationhud.parseapi.ParseObjectConstants;
import com.locationhud.storage.SharedPreferencesStorage;
import com.locationhud.ui.AnimationFactory;
import com.locationhud.ui.UiUtility;
import com.locationhud.utility.IntentTransferCodes;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Mark on 23/10/2014.
 */
public class PoiEditMapActivity extends FragmentActivity implements MyLocationFoundCallback,
        ConfirmSelectedLocationDialogCallback, LatitudeLongitudeFoundCallback {

    private static final int DEFAULT_MAP_ZOOM_LEVEL = 10;

    private Activity myActivity;
    private LatitudeLongitudeFoundCallback callback;
    private MyLocationManager locationManager;
    private GoogleMap map;
    private HashMap<Marker, MapPoint> markerToPoiMap = new HashMap<Marker, MapPoint>();
    HashMap<String, MapPoint> oldMarkers = new HashMap<String, MapPoint>();
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
        callback = this;

        poiList = getIntent().getStringExtra(IntentTransferCodes.CURRENT_POI_LIST);
        if (poiList == null) {
            poiList = "Default";
        }

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.prompt_to_select_poi);
        final PlacesAutoCompleteAdapter placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(this, R.layout.places_autocomplete_list_item);
        autoCompView.setAdapter(placesAutoCompleteAdapter);
        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectAddressToAdd(placesAutoCompleteAdapter, position);
            }
        });
        autoCompView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (placesAutoCompleteAdapter.hasElements()) {
                        selectAddressToAdd(placesAutoCompleteAdapter, 0);
                        return true;
                    }
                }
                return false;
            }
        });

        boolean instructionsViewed = SharedPreferencesStorage.isInstructionsRead(getApplicationContext());
        if (!instructionsViewed) {
            LinearLayout instructionsView = (LinearLayout)findViewById(R.id.edit_poi_map_instructions);
            instructionsView.setVisibility(View.VISIBLE);
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
                addMarker(latLong);
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
        storeLocationsInCurrentListForLaterComparison(markerToPoiMap);

        ImageButton navigationArrowForwardButton = (ImageButton)findViewById(R.id.navigation_arrow_back);
        navigationArrowForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void storeLocationsInCurrentListForLaterComparison(HashMap<Marker, MapPoint> markerToPoiMap) {
        Collection values = markerToPoiMap.values();
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            MapPoint mapPoint = (MapPoint) iterator.next();
            oldMarkers.put(mapPoint.toString(), mapPoint);
        }
    }

    private void addMarker(LatLng latLong) {
        addMarker("", latLong);
    }

    private void addMarker(String name, LatLng latLong) {
        lastAddedMarker = map.addMarker(new MarkerOptions().position(latLong).draggable(true));
        lastLongClickLocation = latLong;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, DEFAULT_MAP_ZOOM_LEVEL));
        displayEditMarkerDialog(lastAddedMarker, name, myActivity.getString(R.string.save), myActivity.getString(R.string.cancel));
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
        uploadListChangesToServer();
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

    private void uploadListChangesToServer() {
        ArrayList<ParseObject> changedLocations = new ArrayList<ParseObject>();
        Collection values = markerToPoiMap.values();
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            MapPoint mapPoint = (MapPoint) iterator.next();
            if (oldMarkers.get(mapPoint.toString()) == null) {
                ParseObject locationParseObject = new ParseObject(ParseObjectConstants.LocationObject.NAME);
                locationParseObject.put(ParseObjectConstants.LocationObject.NAME_FIELD_TITLE, mapPoint.getTitle());
                locationParseObject.put(ParseObjectConstants.LocationObject.LATITUDE_LONGITUDE_FIELD_TITLE, new ParseGeoPoint(mapPoint.getLatitude(), mapPoint.getLongitude()));
                locationParseObject.put(ParseObjectConstants.LocationObject.ALTITUDE_FIELD_TITLE, mapPoint.getAltitude());
                changedLocations.add(locationParseObject);
            }
        }
        ParseObject.saveAllInBackground(changedLocations);
    }

    private void selectAddressToAdd(PlacesAutoCompleteAdapter placesAutoCompleteAdapter, int position) {
        String name = placesAutoCompleteAdapter.getItem(position);
        String placeId = placesAutoCompleteAdapter.getPlaceId(position);
        LatitudeLongitudeRetrievalTask task = new LatitudeLongitudeRetrievalTask(callback, name, placeId);
        task.execute();
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
        }, 100);
    }

    @Override
    public void onLatitudeLongitudeFound(String name, LatLng latLng) {
        if (latLng != null) {
            AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.prompt_to_select_poi);
            autoCompView.setText("");
            name = name.indexOf(',') >= 0 ? name.substring(0, name.indexOf(',')) : name;
            addMarker(name, latLng);
        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.error_get_lat_lng), Toast.LENGTH_SHORT).show();
        }
    }
}
