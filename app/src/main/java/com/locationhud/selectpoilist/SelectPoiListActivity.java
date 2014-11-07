package com.locationhud.selectpoilist;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.locationhud.PoiManager;
import com.locationhud.R;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Mark on 05/11/2014.
 */
public class SelectPoiListActivity extends Activity {

    private TrieNode head;
    private ArrayList<String> searchResults = PoiManager.getSupportedPoiLists();
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_poi_list);

        final PoiListAdapter listAdapter = new PoiListAdapter(getApplicationContext(), PoiManager.getSupportedPoiLists());
        final ListView poiListView = (ListView)findViewById(R.id.poi_list_view);
        poiListView.setAdapter(listAdapter);
        poiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PoiManager.setCurrentList(searchResults.get(i));
            }
        });

        head = TrieNode.createTrie(PoiManager.getSupportedPoiLists());

        final TextView searchTitle = (TextView)findViewById(R.id.search_title);

        final ImageButton searchActionButton = (ImageButton)findViewById(R.id.search);
        searchActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBar.setVisibility(View.VISIBLE);
                searchBar.requestFocus();
                // when search bar gets focus, the focus listener will do the rest of the work hiding views
            }
        });

        searchBar = (EditText)findViewById(R.id.search_bar);
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
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    searchBar.setVisibility(View.VISIBLE);
                    searchTitle.setVisibility(View.GONE);
                    searchActionButton.setVisibility(View.GONE);
                } else {
                    searchBar.setVisibility(View.GONE);
                    searchTitle.setVisibility(View.VISIBLE);
                    searchActionButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (searchBar.hasFocus()) {
            searchBar.clearFocus();
        } else {
            super.onBackPressed();
        }
    }

}
