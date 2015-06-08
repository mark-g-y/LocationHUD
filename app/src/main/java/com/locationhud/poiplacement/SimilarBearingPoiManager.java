package com.locationhud.poiplacement;

import com.locationhud.compassdirection.MapPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mark on 07/06/2015.
 */
public class SimilarBearingPoiManager {

    private ArrayList<Bearing> bearings;
    private static final double SIMILAR_THRESHOLD = 15;
    private HashMap<MapPoint, MapPoint> similar;
    private boolean doneGenerating = false;

    public SimilarBearingPoiManager() {
        bearings = new ArrayList<Bearing>();
    }

    public void add(MapPoint p, Double bearing) {
        bearings.add(new Bearing(p, bearing));
    }

    public ArrayList<ArrayList<MapPoint>> generate() {
        Collections.sort(bearings);
        similar = new HashMap<MapPoint, MapPoint>();
        double sum = 0;
        int num = 0;
        ArrayList<ArrayList<MapPoint>> similarBearingsList = new ArrayList<ArrayList<MapPoint>>();
        ArrayList<MapPoint> similarBearings = new ArrayList<MapPoint>();
        for (int i = 0; i < bearings.size(); i++) {
            sum += bearings.get(i).bearing;
            num += 1;
            if (Math.abs(bearings.get(i).bearing - sum / num) < SIMILAR_THRESHOLD) {
                similarBearings.add(bearings.get(i).mp);
            } else {
                for (int m = 0; m < similarBearings.size(); m++) {
                    similar.put(similarBearings.get(m), m == similarBearings.size() - 1 ? similarBearings.get(0) : similarBearings.get(m + 1));
                }
                similarBearingsList.add(similarBearings);
                sum = 0;
                num = 0;
                i -= 1;
                similarBearings = new ArrayList<MapPoint>();
            }
        }
        if (sum != 0) {
            for (int m = 0; m < similarBearings.size(); m++) {
                similar.put(similarBearings.get(m), m == similarBearings.size() - 1 ? similarBearings.get(0) : similarBearings.get(m + 1));
            }
            similarBearingsList.add(similarBearings);
        }
        doneGenerating = true;
        return similarBearingsList;
    }

    public MapPoint cyclePoiNext(MapPoint current) {
        return similar.get(current);
    }

    public boolean isDoneGenerating() {
        return doneGenerating;
    }

    private class Bearing implements Comparable{
        final MapPoint mp;
        final double bearing;
        public Bearing(MapPoint mp, double bearing) {
            this.mp = mp;
            this.bearing = bearing;
        }
        @Override
        public int compareTo(Object anotherObj) {
            Bearing another = (Bearing)anotherObj;
            return (int)Math.floor(bearing - another.bearing);
        }
    }

}
