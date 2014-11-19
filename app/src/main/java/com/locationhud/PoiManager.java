package com.locationhud;

import android.content.Context;
import android.content.res.Resources;

import com.locationhud.compassdirection.MapPoint;
import com.locationhud.selectpoilist.SelectPoiListActivity;
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

    private static ArrayList<ArrayList<MapPoint>> poiList = new ArrayList<ArrayList<MapPoint>>();
    private static HashMap<String, ArrayList<MapPoint>> poiMap = new HashMap<String, ArrayList<MapPoint>>();
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
            list.add(listName);
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
                //poiMap.putAll(JsonFactory.decodeJsonForPois(jsonPoi));
                poiMap = mergeHashMaps(poiMap, JsonFactory.decodeJsonForPois(jsonPoi));
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

    private static HashMap<String, ArrayList<MapPoint>> mergeHashMaps(HashMap<String, ArrayList<MapPoint>> m1, HashMap<String, ArrayList<MapPoint>> m2) {
        Iterator<Map.Entry<String, ArrayList<MapPoint>>> iterator = m1.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ArrayList<MapPoint>> entry = iterator.next();
            String key = entry.getKey();
            if (m2.get(key) != null) {
                m1.put(key, mergeArray(m1.get(key), m2.get(key)));
                m2.remove(key);
            }
        }
        m1.putAll(m2);
        return m1;
    }

    private static ArrayList<MapPoint> mergeArray(ArrayList<MapPoint> l1, ArrayList<MapPoint> l2) {
        HashMap<String, MapPoint> m1 = new HashMap<String, MapPoint>();
        for (MapPoint mp : l1) {
            m1.put(mp.toString(), mp);
        }
        for (MapPoint mp : l2) {
            if (m1.get(mp.toString()) == null) {
                l1.add(mp);
            }
        }
        return l1;
    }
}
