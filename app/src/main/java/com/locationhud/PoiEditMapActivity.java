package com.locationhud;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.locationhud.autogenpoiapi.SubmitPoiListTask;
import com.locationhud.compassdirection.MapPoint;
import com.locationhud.compassdirection.MyLocationFoundCallback;
import com.locationhud.compassdirection.MyLocationManager;
import com.locationhud.googleapi.placesautocomplete.PlacesAutoCompleteAdapter;
import com.locationhud.googleapi.placesdetail.LatitudeLongitudeFoundCallback;
import com.locationhud.googleapi.placesdetail.LatitudeLongitudeRetrievalTask;
import com.locationhud.map.ConfirmSelectedLocationDialog;
import com.locationhud.map.ConfirmSelectedLocationDialogCallback;
import com.locationhud.storage.SharedPreferencesStorage;
import com.locationhud.ui.AnimationFactory;
import com.locationhud.ui.UiUtility;
import com.locationhud.utility.ToolbarUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Mark on 23/10/2014.
 */
public class PoiEditMapActivity extends ActionBarActivity implements MyLocationFoundCallback,
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
    private AutoCompleteTextView locationSearchAutoCompView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_edit_map);
        myActivity = this;
        callback = this;

        ToolbarUtility.initWithBackButton(this, "", R.id.toolbar);

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

        initMap();

        addLocationsInCurrentListToMap();
        saveCurrentListLocations(markerToPoiMap);

        zoomMapCameraToCentreOfAllMarkers(map, markerToPoiMap);
    }

    private void initMap() {
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
    }

    private void zoomMapCameraToCentreOfAllMarkers(GoogleMap map, HashMap<Marker, MapPoint> markers) {
        if (markers.keySet().size() <= 0) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers.keySet()) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 10; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 500, 500, padding);
        map.animateCamera(cu);
    }

    private void saveCurrentListLocations(HashMap<Marker, MapPoint> markerToPoiMap) {
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
        //zoomMapCameraToCentreOfAllMarkers(map, markerToPoiMap);
        if (!saveMapZoomState) {
            saveMapZoomState = true;
            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationManager.getLastLocation().getLatitude(), locationManager.getLastLocation().getLongitude()), 12));
        }
     }

    @Override
    public void onMyLocationUnavailable() {
        //zoomMapCameraToCentreOfAllMarkers(map, markerToPoiMap);
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
        ArrayList<MapPoint> list = PoiManager.readLocationsFromFile(getApplicationContext());
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
        PoiManager.setList(list);
        PoiManager.saveLocationsToFile(getApplicationContext(), list);
    }

    private void uploadListChangesToServer() {
        ArrayList<MapPoint> mapPoints = new ArrayList<MapPoint>();
        Collection values = markerToPoiMap.values();
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            MapPoint mapPoint = (MapPoint) iterator.next();
            if (oldMarkers.get(mapPoint.toString()) == null) {
                mapPoints.add(mapPoint);
            }
        }
        SubmitPoiListTask task = new SubmitPoiListTask(mapPoints);
        task.execute();
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
            locationSearchAutoCompView.setText("");
            name = name.indexOf(',') >= 0 ? name.substring(0, name.indexOf(',')) : name;
            addMarker(name, latLng);
        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.error_get_lat_lng), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poi_edit_map, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        locationSearchAutoCompView = (AutoCompleteTextView) MenuItemCompat.getActionView(searchItem);
        setupPoiAutoCompleteTextView(locationSearchAutoCompView);
        MenuItemCompat.expandActionView(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    private void setupPoiAutoCompleteTextView(AutoCompleteTextView autoCompView) {
        autoCompView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        autoCompView.setHint(getString(R.string.prompt_to_search_poi));
        autoCompView.setMinWidth(UiUtility.getScreenSize(this).x);
        autoCompView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
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
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();
        super.onBackPressed();
    }
}
