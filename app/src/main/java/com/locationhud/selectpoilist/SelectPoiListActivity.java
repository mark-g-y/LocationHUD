package com.locationhud.selectpoilist;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.locationhud.PoiManager;
import com.locationhud.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Mark on 05/11/2014.
 */
public class SelectPoiListActivity extends Activity {

    private TrieNode head;
    private ArrayList<String> searchResults = PoiManager.getSupportedPoiLists();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_poi_list);

        final PoiListAdapter listAdapter = new PoiListAdapter(getApplicationContext(), PoiManager.getSupportedPoiLists());
        final ListView poiListView = (ListView)findViewById(R.id.poi_list_view);
        poiListView.setAdapter(listAdapter);

        head = TrieNode.createTrie(PoiManager.getSupportedPoiLists());

        final EditText searchBar = (EditText)findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                final TrieNode node = TrieNode.getCurrentPosition(head, searchBar.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (node == null) {
                            searchResults.clear();
                        } else {
                            searchResults = TrieNode.getStringsWithCurrentPrefix(new ArrayList<String>(), node);
                        }
                        listAdapter.updateData((ArrayList<String>) searchResults);
                        listAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

}
