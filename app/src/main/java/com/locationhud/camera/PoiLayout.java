package com.locationhud.camera;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.locationhud.R;
import com.locationhud.compassdirection.CompassDirectionManager;
import com.locationhud.compassdirection.MapPoint;

import org.w3c.dom.Text;

/**
 * Created by Mark on 21/10/2014.
 */
public class PoiLayout extends LinearLayout {

    private MapPoint poi;

    public PoiLayout(Context context, MapPoint poi) {
        super(context);

        LayoutInflater  inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_poi, this, true);

        this.poi = poi;
        TextView titleTextView = (TextView)findViewById(R.id.title_view);
        titleTextView.setText(poi.getTitle());

        TextView locationTextView = (TextView)findViewById(R.id.location_view);
        locationTextView.setText(poi.getLatitude() + ", " + poi.getLongitude() + ", " + poi.getAltitude());
    }

    public void updateDistanceToPoi(double distance) {
        TextView titleTextView = (TextView) findViewById(R.id.title_view);
        titleTextView.setText(poi.getTitle() + " | " + convertDistanceForDisplay(distance));
    }

    private String convertDistanceForDisplay(double distance) {
        double roundedDistance = (double)Math.round(distance / 100);
        if (distance > 1200) {
            return roundedDistance / 10 + "km";
        }
        return ((double)Math.round(distance * 10)) / 10 + "m";
    }
}
