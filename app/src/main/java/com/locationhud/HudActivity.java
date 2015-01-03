package com.locationhud;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.locationhud.camera.CameraPreview;
import com.locationhud.camera.PoiLayout;
import com.locationhud.compassdirection.CompassDirectionFoundCallback;
import com.locationhud.compassdirection.CompassDirectionManager;
import com.locationhud.compassdirection.MapPoint;
import com.locationhud.compassdirection.MyLocationManager;
import com.locationhud.googleapi.retrievealtitude.AltitudeFoundCallback;
import com.locationhud.googleapi.retrievealtitude.RetrieveAltitudeTask;
import com.locationhud.storage.JsonFactory;
import com.locationhud.ui.UiUtility;
import com.locationhud.ui.buttons.PressedColourChangeViewTouchListener;
import com.locationhud.utility.IntentTransferCodes;
import com.locationhud.utility.ParseApiData;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mark on 19/10/2014.
 */
public class HudActivity extends Activity implements CompassDirectionFoundCallback,
        AltitudeFoundCallback
{

    private static final int MAX_VIEW_DISTANCE = 300 * 1000;
    private static final double NO_ALTITUDE = -999;

    private Activity myActivity;
    private Context context;
    private Camera camera;
    private CompassDirectionManager compassDirectionManager;
    private String currentList = "Default";
    private ArrayList<MapPoint> poi = PoiManager.getList(currentList);
    private HashMap<MapPoint, PoiLayout> poiLayouts = new HashMap<MapPoint, PoiLayout>();

    private ProgressDialog loadingDialog;
    private boolean firstTime = true;
    private boolean isAutomatedPoiRetrieval = false;
    private boolean isLocationPromptShowing = false;

    private double verticalViewAngle;
    private double horizontalViewAngle;

    private double myAltitude = NO_ALTITUDE;

    private boolean receivedAltitudeRetrievalCallback = false;
    private boolean receivedGetAutogenPoiCallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myActivity = this;
        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, ParseApiData.APPLICATION_ID, ParseApiData.CLIENT_KEY);
        isAutomatedPoiRetrieval = getIntent().getBooleanExtra(IntentTransferCodes.IS_AUTOMATED_POI_RETRIEVAL, false);

        loadPoiLayoutsFromStorage();

        compassDirectionManager = new CompassDirectionManager(this, this);
        compassDirectionManager.onCreate();

        LinearLayout menuButton = (LinearLayout)findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        menuButton.setOnTouchListener(new PressedColourChangeViewTouchListener(menuButton, android.R.color.transparent, R.color.item_pressed_translucent));
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassDirectionManager.onResume();
        camera = getCamera();
        firstTime = true;
        isLocationPromptShowing = false;
        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
        initiateCameraViewport(camera);

        CameraPreview preview = new CameraPreview (this, camera);
        FrameLayout previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
        previewLayout.removeAllViews();
        previewLayout.addView(preview);

        TextView poiListName = (TextView)findViewById(R.id.poi_list_name);
        poiListName.setText(isAutomatedPoiRetrieval ? context.getString(R.string.autogenerate_poi_prompt) : PoiManager.getCurrentList());

        if (!MyLocationManager.isLocationServicesOn(this)) {
            MyLocationManager.promptUserTurnOnLocation(this);
        } else {
            loadingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loading), true, true);
            loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    onBackPressed();
                }
            });
        }

        loadPoiLayoutsFromStorage();
    }

    @Override
    protected void onStart() {
        super.onStart();
        compassDirectionManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        compassDirectionManager.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.release();
        compassDirectionManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void loadPoiLayoutsFromStorage() {
        removePoiLayoutsInView();
        currentList = PoiManager.getCurrentList();
        poi = PoiManager.getList(currentList);
        if (poi == null) {
            poi = new ArrayList<MapPoint>();
            return;
        }
        loadPoiLayoutsIntoView();
    }

    private void removePoiLayoutsInView() {
        RelativeLayout layoutHudActivity = (RelativeLayout) findViewById(R.id.activity_hud_layout);
        for (int i = 0; i < poiLayouts.size(); i++) {
            layoutHudActivity.removeView(poiLayouts.get(poi.get(i)));
        }
        poiLayouts.clear();
    }

    private void loadPoiLayoutsIntoView(){
        RelativeLayout layoutHudActivity = (RelativeLayout) findViewById(R.id.activity_hud_layout);
        for (int i = 0; i < poi.size(); i++) {
            final int index = i;
            final PoiLayout poiLayout = new PoiLayout(getApplicationContext(), poi.get(i));
            poiLayout.setVisibility(View.GONE);
            poiLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    poiLayout.toggleTranslucentLevel();
                    for (int m = 0; m < poi.size(); m++) {
                        if (m != index) {
                            poiLayouts.get(poi.get(m)).bringToFront();
                        }
                    }
                }
            });
            poiLayouts.put(poi.get(i), poiLayout);
            layoutHudActivity.addView(poiLayout);
        }
    }

    private Camera getCamera() {
        try {
            camera = Camera.open();
            return camera;
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        return null;
    }

    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = getOrientation(rotation);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void initiateCameraViewport(Camera camera) {
        Camera.Parameters p = camera.getParameters();
        int zoom = p.getZoomRatios().get(p.getZoom()).intValue();
        Camera.Size sz = p.getPreviewSize();
        double aspect = (double) sz.width / (double) sz.height;
        double thetaV = Math.toRadians(p.getVerticalViewAngle());
        double thetaH = 2d * Math.atan(aspect * Math.tan(thetaV / 2));
        verticalViewAngle = Math.toDegrees(2d * Math.atan(100d * Math.tan(thetaV / 2d) / zoom));
        horizontalViewAngle = 0.8 * Math.toDegrees(2d * Math.atan(100d * Math.tan(thetaH / 2d) / zoom));
        // must swap the variables because Android thinks they're reversed
        horizontalViewAngle = verticalViewAngle + horizontalViewAngle;
        verticalViewAngle = horizontalViewAngle - verticalViewAngle;
        horizontalViewAngle = horizontalViewAngle - verticalViewAngle;
    }

    private int getOrientation (int rotation) {
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        return degrees;
    }

    @Override
    public void onCompassDirectionFound(final double azimuth) {
        if (!MyLocationManager.isLocationServicesOn(this) && !isLocationPromptShowing) {
            MyLocationManager.promptUserTurnOnLocation(this);
            isLocationPromptShowing = true;
        }
        Location location = compassDirectionManager.getLastLocation();
        if (firstTime) {
            getAltitudeFromServer(location);
            if (isAutomatedPoiRetrieval) {
                getPoisFromServer(location);
            }
            firstTime = false;
        }
        if (location != null) {
            for (int i = 0; i < poi.size(); i++) {
                double bearing = CompassDirectionManager.getAngleBetweenCoordinates(location.getLatitude(), location.getLongitude(), poi.get(i).getLatitude(), poi.get(i).getLongitude());
                if (isPoiInView(azimuth, bearing, poi.get(i))) {
                    positionPoi(poi.get(i), azimuth, bearing, compassDirectionManager.getTiltAngle(), CompassDirectionManager.getAngleBetweenAltitudes(location, poi.get(i)));
                } else {
                    poiLayouts.get(poi.get(i)).setVisibility(View.GONE);
                }
            }
        }
    }

    private void getPoisFromServer(Location location) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("latitude", location.getLatitude());
        params.put("longitude", location.getLongitude());
        ParseCloud.callFunctionInBackground("nearby_locations", params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    receivedGetAutogenPoiCallback = true;
                    if (shouldDismissLoadingDialog() && loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    removePoiLayoutsInView();
                    poi = JsonFactory.decodeJsonForPoiList(result.toString());
                    Log.d("TEST", result.toString());
                    loadPoiLayoutsIntoView();
                }
            }
        });
    }

    private void getAltitudeFromServer(Location location) {
        RetrieveAltitudeTask task = new RetrieveAltitudeTask(this, location.getLatitude(), location.getLongitude());
        task.execute();
    }

    private void positionPoi(final MapPoint poi, double azimuth, double bearing, double tiltAngle, double altitudeAngle) {
        Display display = getWindowManager().getDefaultDisplay();
        final int width = display.getWidth();
        final int height = display.getHeight();
        double horizontalDiff = (bearing - azimuth) % 180;
        double verticalDiff = (altitudeAngle - tiltAngle);
        double horizontalLength = (width / 2) / Math.tan(Math.toRadians(horizontalViewAngle / 2));
        double verticalLength = (height / 2) / Math.tan(Math.toRadians(verticalViewAngle / 2));
        final double horizontalWidth = Math.tan(Math.toRadians(horizontalDiff)) * horizontalLength;
        final double verticalWidth = Math.tan(Math.toRadians(verticalDiff)) * verticalLength;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PoiLayout poiLayout = poiLayouts.get(poi);
                if (poiLayout == null) {
                    return;
                }
                setPoiViewSizeByDistance(poiLayout, CompassDirectionManager.getDistance(getLastLocation(), poi));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins((int)(horizontalWidth + width / 2 - poiLayout.getWidth() / 2), (int)(-verticalWidth + height / 2 - poiLayout.getHeight() / 2), 0, 0);
                poiLayout.setLayoutParams(params);
                poiLayout.updateDistanceToPoi(CompassDirectionManager.getDistance(getLastLocation(), poi));
                poiLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setPoiViewSizeByDistance(PoiLayout poiLayout, double distance) {
        // scale so that scale = (MAX_VIEW_DISTANCE - distance) / MAX_VIEW_DISTANCE, if MAX_VIEW_DISTANCE > 100 scale = 0
        double scale = (MAX_VIEW_DISTANCE - distance) / MAX_VIEW_DISTANCE;
        scale = scale < 0 ? 0 : scale;
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                (int)UiUtility.convertDpToPixel((float) (300 * scale), this),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout layout = (LinearLayout)poiLayout.findViewById(R.id.layout_poi);
        layout.setLayoutParams(linearLayoutParams);
        for(int i = 0; i < layout.getChildCount(); i++) {
            try {
                TextView tv = (TextView)layout.getChildAt(i);
                tv.setTextSize((float)(scale * 15));
            } catch (ClassCastException e) {
            }
        }
    }

    private boolean isPoiInVerticalView(Location myLocation, MapPoint poi) {
        double diff = CompassDirectionManager.getAngleBetweenAltitudes(myLocation, poi);
        double myAngle = compassDirectionManager.getTiltAngle();
        double topViewLimit = myAngle + verticalViewAngle / 2;
        double bottomViewLimit = myAngle - verticalViewAngle / 2;
        //Log.d("FOO", topViewLimit + ", " + myAngle + ", " + diff + ", " + bottomViewLimit);
        if (diff < topViewLimit && diff > bottomViewLimit) {
            return true;
        }
        return false;
    }

    private boolean isPoiInHorizontalView(double myDirection, double bearing) {
        bearing = bearingToNegUnits(Math.abs(bearing));
        double rightViewLimit = myDirection + horizontalViewAngle / 2 > 180 ? (myDirection + horizontalViewAngle / 2) - 360 : myDirection + horizontalViewAngle / 2;
        double leftViewLimit = myDirection - horizontalViewAngle / 2 < -180 ? 360 + (myDirection - horizontalViewAngle / 2) : myDirection - horizontalViewAngle / 2;
        //Log.d("FOO", leftViewLimit + ", " + bearing + ", " + rightViewLimit);
        if (bearing < rightViewLimit && bearing > leftViewLimit) {
            return true;
        }
        // edge case where left view is less than 180 and right view is greater than (denoted by neg)
        if (leftViewLimit > 0 && rightViewLimit < 0) {
            bearing = bearing > 0 ? bearing : bearing + 360;
            rightViewLimit = rightViewLimit + 360;
            return bearing < rightViewLimit && bearing > leftViewLimit;
        }
        return false;
    }

    private double bearingToNegUnits(double bearing) {
        if (bearing > 180) {
            bearing = bearing - 360;
        }
        return bearing;
    }

    private boolean isPoiInView(double myDirection, double bearing, MapPoint poi) {
        return isPoiInHorizontalView(myDirection, bearing) && isPoiInVerticalView(getLastLocation(), poi);
    }

    private Location getLastLocation() {
        Location location = compassDirectionManager.getLastLocation();
        if (location != null) {
            if (myAltitude != NO_ALTITUDE) {
                location.setAltitude(myAltitude);
            }
        }
        return location;
    }

    @Override
    public void onAltitudeFound(double altitude) {
        myAltitude = altitude;
        receivedAltitudeRetrievalCallback = true;
        if (shouldDismissLoadingDialog() && loadingDialog != null || !isAutomatedPoiRetrieval) {
            loadingDialog.dismiss();
        }
    }

    private boolean shouldDismissLoadingDialog() {
        if (receivedAltitudeRetrievalCallback && receivedGetAutogenPoiCallback) {
            return true;
        }
        return false;
    }
}
