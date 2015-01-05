package com.locationhud.googleapi.placesautocomplete;

import android.util.Log;

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
 * Created by Mark on 28/10/2014.
 */
public class PlacesAutocomplete {

    private static final String LOG_TAG = "LocationHUD";

    public static ArrayList<ArrayList<String>> autocomplete(String input) {
        ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(GoogleApiManager.PLACES_API_BASE + GoogleApiManager.TYPE_AUTOCOMPLETE + GoogleApiManager.OUT_JSON);
            sb.append("?key=" + GoogleApiManager.GOOGLE_API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

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
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            ArrayList<String> resultName = new ArrayList<String>(predsJsonArray.length());
            ArrayList<String> resultId = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultName.add(predsJsonArray.getJSONObject(i).getString("description"));
                resultId.add(predsJsonArray.getJSONObject(i).getString("place_id"));
            }
            resultList.add(resultName);
            resultList.add(resultId);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}
