package com.locationhud.googleapi.placesdetail;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.locationhud.googleapi.GoogleApiManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Mark on 08/11/2014.
 */
public class LatitudeLongitudeRetrievalTask extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = "PlacesDetailRetrievalTask";

    LatitudeLongitudeFoundCallback callback;
    private String name;
    private String placeId;
    private LatLng latlong;

    public LatitudeLongitudeRetrievalTask(LatitudeLongitudeFoundCallback callback, String name, String placeId) {
        this.name = name;
        this.callback = callback;
        this.placeId = placeId;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        latlong = getLatitudeLongitude(placeId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        callback.onLatitudeLongitudeFound(name, latlong);
    }

    public LatLng getLatitudeLongitude(String placeId) {

        LatLng latlng = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(GoogleApiManager.PLACES_API_BASE + GoogleApiManager.TYPE_DETAILS + GoogleApiManager.OUT_JSON);
            sb.append("?key=" + GoogleApiManager.GOOGLE_API_KEY);
            sb.append("&placeid=" + URLEncoder.encode(placeId, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return latlng;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return latlng;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonResults.toString());
            JSONObject locationDict = jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
            latlng = new LatLng(locationDict.getDouble("lat"), locationDict.getDouble("lng"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return latlng;
    }

}
