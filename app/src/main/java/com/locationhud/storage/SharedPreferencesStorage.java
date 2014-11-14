package com.locationhud.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Mark on 03/11/2014.
 */
public class SharedPreferencesStorage {

    public static class GeneralSettingsStorage {
        private static final String KEY = "settings";
        private static final String KEY_INSTRUCTIONS_VIEWED = "instructions_viewed";
    }

    public static class MapCameraStorage {
        private static final String KEY = "map_settings";
        private static final String KEY_LATITUDE = "latitude";
        private static final String KEY_LONGITUDE = "longitude";
        private static final String KEY_ZOOM = "zoom";
    }

    public static class PoiListGroupStorage {
        private static final String KEY = "poi_list_group";
        private static final String KEY_CUSTOM_GROUP_EXPANDED = "custom_group_expanded";
        private static final String KEY_DEFAULT_GROUP_EXPANDED = "default_group_expanded";
    }

    public static void saveExpandedGroups(Context context, boolean[] groups) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PoiListGroupStorage.KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(PoiListGroupStorage.KEY_CUSTOM_GROUP_EXPANDED, groups[0]);
        sharedPreferencesEditor.putBoolean(PoiListGroupStorage.KEY_DEFAULT_GROUP_EXPANDED, groups[1]);
        sharedPreferencesEditor.commit();
    }

    public static boolean[] getExpandedGroups(Context context) {
        boolean[] expanded = new boolean[2];
        SharedPreferences sharedPreferences = context.getSharedPreferences(PoiListGroupStorage.KEY, Context.MODE_PRIVATE);
        expanded[0] = sharedPreferences.getBoolean(PoiListGroupStorage.KEY_CUSTOM_GROUP_EXPANDED, true);
        expanded[1] = sharedPreferences.getBoolean(PoiListGroupStorage.KEY_DEFAULT_GROUP_EXPANDED, true);
        return expanded;
    }

    public static void setInstructionsRead(Context context, boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GeneralSettingsStorage.KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(GeneralSettingsStorage.KEY_INSTRUCTIONS_VIEWED, true);
        sharedPreferencesEditor.commit();
    }

    public static boolean isInstructionsRead(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(GeneralSettingsStorage.KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(GeneralSettingsStorage.KEY_INSTRUCTIONS_VIEWED, false);
    }

    public static boolean loadMapCamera(Context context, GoogleMap map) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MapCameraStorage.KEY, Context.MODE_PRIVATE);
        if (sharedPreferences == null || !sharedPreferences.contains(MapCameraStorage.KEY_LATITUDE) ||
                !sharedPreferences.contains(MapCameraStorage.KEY_LONGITUDE) ||
                !sharedPreferences.contains(MapCameraStorage.KEY_ZOOM)) {
            return false;
        }
        double latitude = sharedPreferences.getFloat(MapCameraStorage.KEY_LATITUDE, -1);
        double longitude = sharedPreferences.getFloat(MapCameraStorage.KEY_LONGITUDE, -1);
        float zoom = sharedPreferences.getFloat(MapCameraStorage.KEY_ZOOM, -1);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
        return true;
    }

    public static void saveMapCamera(Context context, GoogleMap map) {
        CameraPosition mapCamera = map.getCameraPosition();
        double latitude = mapCamera.target.latitude;
        double longitude = mapCamera.target.longitude;
        double zoom = mapCamera.zoom;

        SharedPreferences settings = context.getSharedPreferences(MapCameraStorage.KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(MapCameraStorage.KEY_LATITUDE, (float)latitude);
        editor.putFloat(MapCameraStorage.KEY_LONGITUDE, (float)longitude);
        editor.putFloat(MapCameraStorage.KEY_ZOOM, (float)zoom);
        editor.commit();
    }

}
