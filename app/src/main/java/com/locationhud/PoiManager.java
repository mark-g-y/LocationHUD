package com.locationhud;

import android.content.Context;

import com.locationhud.compassdirection.MapPoint;
import com.locationhud.storage.FileStorage;
import com.locationhud.storage.JsonFactory;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Mark on 23/10/2014.
 */
public class PoiManager {

    private static ArrayList<ArrayList<MapPoint>> poiList = new ArrayList<ArrayList<MapPoint>>();
    private static HashMap<String, ArrayList<MapPoint>> poiMap = new HashMap<String, ArrayList<MapPoint>>();
    private static HashMap<String, ArrayList<MapPoint>> defaultPoiMap = new HashMap<String, ArrayList<MapPoint>>();
    private static String currentList = "Default";

    static {
        try {
            poiMap = JsonFactory.decodeJsonForPois(FileStorage.readFromRawResFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<MapPoint> def = new ArrayList<MapPoint>();
        def.add(new MapPoint("Mission Peak", 37.512643, -121.880493, 767));
        def.add(new MapPoint("San Francisco", 37.808305, -122.409104));
        def.add(new MapPoint("Home", 37.420980, -121.900235));
        poiMap.put("Default", def);
        defaultPoiMap = (HashMap<String, ArrayList<MapPoint>>) poiMap.clone();
    }

    public static void addList() {
        poiList.add(new ArrayList<MapPoint>());
    }

    public static void addList(String name) {
        poiMap.put(name, new ArrayList<MapPoint>());
    }

    public static ArrayList<MapPoint> getList(String name) {
        return poiMap.get(name);
    }

    public static void setList(String name, ArrayList<MapPoint> list) {
        poiMap.put(name, list);
    }

    public static String getCurrentList() {
        return currentList;
    }

    public static ArrayList<String> getCustomPoiLists() {
        ArrayList<String> list = new ArrayList<String>();
        Iterator iterator = poiMap.keySet().iterator();
        while(iterator.hasNext()) {
            String listName = (String)iterator.next();
            if (defaultPoiMap.get(listName) == null) {
                list.add(listName);
            }
        }
        return list;
    }

    public static ArrayList<String> getDefaultPoiLists() {
        ArrayList<String> list = new ArrayList<String>();
        Iterator iterator = poiMap.keySet().iterator();
        while(iterator.hasNext()) {
            String listName = (String)iterator.next();
            if (defaultPoiMap.get(listName) != null) {
                list.add(listName);
            }
        }
        return list;
    }

    public static void saveLocationsToFile(Context context) {
        try {
            FileStorage.writeToFile(context, JsonFactory.generateJsonForPois(poiMap).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void readLocationsFromFile(Context context) {
        try {
            String jsonPoi = FileStorage.readFromFile(context);
            if (jsonPoi != null) {
                poiMap = JsonFactory.decodeJsonForPois(jsonPoi);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setCurrentList(String list) {
        currentList = list;
    }

    public static void removeList(String listName) {
        poiMap.remove(listName);
        currentList = "";
    }
}
