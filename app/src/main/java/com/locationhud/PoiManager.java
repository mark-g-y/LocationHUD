package com.locationhud;

import android.content.Context;
import android.content.res.Resources;

import com.locationhud.compassdirection.MapPoint;
import com.locationhud.storage.FileStorage;
import com.locationhud.storage.JsonFactory;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Mark on 23/10/2014.
 */
public class PoiManager {

    private static ArrayList<MapPoint> poiList = new ArrayList<MapPoint>();

    public static ArrayList<MapPoint> getList() {
        return poiList;
    }

    public static void setList(ArrayList<MapPoint> newList) {
        poiList = newList;
    }

    public static void saveLocationsToFile(Context context, ArrayList<MapPoint> list) {
        try {
            FileStorage.writeToFile(context, JsonFactory.generateJsonForPoiList(list).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<MapPoint> readLocationsFromFile(Context context) {
        String jsonPoi = FileStorage.readFromFile(context);
        if (jsonPoi != null) {
            return JsonFactory.decodeJsonForPoiList(jsonPoi);
        }
        return null;
    }

}
