package com.locationhud.camera;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.locationhud.R;
import com.locationhud.compassdirection.MapPoint;

/**
 * Created by Mark on 21/10/2014.
 */
public class PoiLayout extends LinearLayout {

    private MapPoint poi;
    private LinearLayout layoutPoi;
    private boolean isNormalBackground = true;

    public PoiLayout(Context context, MapPoint poi) {
        super(context);

        LayoutInflater  inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_poi, this, true);

        layoutPoi = (LinearLayout)findViewById(R.id.layout_poi);

        this.poi = poi;
        TextView titleTextView = (TextView)findViewById(R.id.title_view);
        titleTextView.setText(poi.getTitle());

        TextView locationTextView = (TextView)findViewById(R.id.location_view);
        //locationTextView.setText(convertDoubleForDisplay(poi.getLatitude()) + ", " + convertDoubleForDisplay(poi.getLongitude()) + ", " + convertDoubleForDisplay(poi.getAltitude()));
        locationTextView.setText(convertDoubleForDisplay(poi.getAltitude()) + "m");
    }

    public void updateDistanceToPoi(double distance) {
        //TextView titleTextView = (TextView) findViewById(R.id.title_view);
        //titleTextView.setText(poi.getTitle() + " | " + convertDistanceForDisplay(distance));
        TextView locationTextView = (TextView)findViewById(R.id.location_view);
        locationTextView.setText("Dist. " + convertDistanceForDisplay(distance) + " | Alt. " + convertDoubleForDisplay(poi.getAltitude()) + "m");
    }

    private String convertDistanceForDisplay(double distance) {
        double roundedDistance = (double)Math.round(distance / 100);
        if (distance > 1200) {
            return roundedDistance / 10 + "km";
        }
        return ((double)Math.round(distance * 10)) / 10 + "m";
    }

    private String convertDoubleForDisplay(double d) {
        return convertDoubleForDisplay(d, 3);
    }

    private String convertDoubleForDisplay(double d, int numDecimals) {
        return Double.toString(Math.round(Math.pow(10, numDecimals) * d) / Math.pow(10, numDecimals));
    }

    public void toggleTranslucentLevel() {
        if (isNormalBackground) {
            layoutPoi.setBackgroundColor(getResources().getColor(R.color.white_more_translucent));
            isNormalBackground = false;
        } else {
            layoutPoi.setBackgroundColor(getResources().getColor(R.color.white_translucent));
            isNormalBackground = true;
        }
    }
}
