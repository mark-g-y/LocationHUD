package com.locationhud.selectpoilist;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.locationhud.PoiManager;
import com.locationhud.R;

/**
 * Created by Mark on 05/11/2014.
 */
public class SelectPoiListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_poi_list);

        PoiListAdapter listAdapter = new PoiListAdapter(getApplicationContext(), PoiManager.getSupportedPoiLists());
        ListView poiListView = (ListView)findViewById(R.id.poi_list_view);
        poiListView.setAdapter(listAdapter);
    }

}
