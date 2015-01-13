package com.locationhud.autogenpoiapi;

import android.os.AsyncTask;

import com.locationhud.compassdirection.MapPoint;
import com.locationhud.storage.JsonFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Mark on 13/01/2015.
 */
public class SubmitPoiListTask extends AsyncTask<Void, Void, Void> {

    private static final String URL = "https://locationhud.herokuapp.com/poi_api/";
    private ArrayList<MapPoint> mapPoints;

    public SubmitPoiListTask(ArrayList<MapPoint> mapPoints) {
        this.mapPoints = mapPoints;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        submitPoisToServer(mapPoints);
        return null;
    }

    private void submitPoisToServer(ArrayList<MapPoint> mapPoints) {
        JSONArray jsonArray;
        try {
            jsonArray = JsonFactory.generateJsonForPoiList(mapPoints);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        if (jsonArray == null) {
            return;
        }

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
        HttpResponse response;

        try {
            HttpPost post = new HttpPost(URL);
            StringEntity se = new StringEntity(jsonArray.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(se);
            response = client.execute(post);

            if(response!=null){
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
