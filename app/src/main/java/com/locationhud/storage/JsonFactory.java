package com.locationhud.storage;

import android.util.Log;

import com.locationhud.compassdirection.MapPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Mark on 27/10/2014.
 */
public class JsonFactory {

    private static final String JSON = "json";

    public static JSONArray generateJsonForPois (HashMap<String, ArrayList<MapPoint>> poiMap) throws JSONException {
        JSONArray poi = new JSONArray();

        Iterator iterator = poiMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pairs = (Map.Entry)iterator.next();
            ArrayList<MapPoint> poiListValues = (ArrayList<MapPoint>) pairs.getValue();
            JSONObject poiList = new JSONObject();
            poiList.put(MapPoint.LIST_LABEL, (String)pairs.getKey());
            JSONArray poiListArray = new JSONArray();
            for (MapPoint point : poiListValues) {
                JSONObject pointJson = new JSONObject();
                pointJson.put(MapPoint.TITLE_LABEL, point.getTitle());
                pointJson.put(MapPoint.LATITUDE_LABEL, point.getLatitude());
                pointJson.put(MapPoint.LONGITUDE_LABEL, point.getLongitude());
                pointJson.put(MapPoint.ALTITUDE_LABEL, point.getAltitude());
                poiListArray.put(pointJson);
            }
            poiList.put(MapPoint.POI_LIST_LABEL, poiListArray);
            poi.put(poiList);
            //iterator.remove(); // avoids a ConcurrentModificationException
        }
        //Log.d("GENERATEJSON", poi.toString());
        return poi;
    }

    public static HashMap<String, ArrayList<MapPoint>> decodeJsonForPois(String jsonPoi) throws JSONException{
        HashMap<String, ArrayList<MapPoint>> poiMap = new HashMap<String, ArrayList<MapPoint>>();
        //Log.d("FOO", jsonPoi);
        JSONArray poi = new JSONArray(jsonPoi);
        for (int i = 0; i < poi.length(); i++) {
            JSONObject poiList = (JSONObject)poi.get(i);
            String key = poiList.getString(MapPoint.LIST_LABEL);
            ArrayList<MapPoint> poiPoints = new ArrayList<MapPoint>();
            JSONArray poiListArray = poiList.getJSONArray(MapPoint.POI_LIST_LABEL);
            for (int m = 0; m < poiListArray.length(); m++) {
                JSONObject pointJson = (JSONObject)poiListArray.get(m);
                MapPoint point = new MapPoint(pointJson.getString(MapPoint.TITLE_LABEL), pointJson.getDouble(MapPoint.LATITUDE_LABEL), pointJson.getDouble(MapPoint.LONGITUDE_LABEL), pointJson.getDouble(MapPoint.ALTITUDE_LABEL));
                poiPoints.add(point);
            }
            poiMap.put(key, poiPoints);
        }
        return poiMap;
    }
}
