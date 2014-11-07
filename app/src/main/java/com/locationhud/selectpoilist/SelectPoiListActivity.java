package com.locationhud.selectpoilist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.locationhud.PoiEditMapActivity;
import com.locationhud.PoiManager;
import com.locationhud.R;
import com.locationhud.utility.IntentTransferCodes;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Mark on 05/11/2014.
 */
public class SelectPoiListActivity extends Activity {

    private Activity myActivity;
    private TrieNode head;
    private ArrayList<String> searchResults = PoiManager.getSupportedPoiLists();
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_poi_list);

        ImageButton backButton = (ImageButton)findViewById(R.id.navigation_arrow_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ImageButton newListButton = (ImageButton)findViewById(R.id.add_new_list);
        newListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);
                alert.setTitle(getResources().getString(R.string.new_poi_list_title_prompt));
                final EditText input = new EditText(myActivity);
                alert.setView(input);

                alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        PoiManager.addList(value);
                        Intent intent = new Intent(getApplication(), PoiEditMapActivity.class);
                        intent.putExtra(IntentTransferCodes.CURRENT_POI_LIST, value);
                        myActivity.startActivity(intent);
                        searchResults.add(value);
                        TrieNode.insertString(head, value);
                    }
                });

                alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.show();
            }
        });

        final PoiListAdapter listAdapter = new PoiListAdapter(getApplicationContext(), PoiManager.getSupportedPoiLists());
        final ListView poiListView = (ListView)findViewById(R.id.poi_list_view);
        poiListView.setAdapter(listAdapter);
        poiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PoiManager.setCurrentList(searchResults.get(i));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        poiListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final CharSequence[] items = {
                        "Edit", "Delete"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectPoiListActivity.this);
                builder.setTitle("Make your selection");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            Intent intent = new Intent(getApplication(), PoiEditMapActivity.class);
                            intent.putExtra(IntentTransferCodes.CURRENT_POI_LIST, searchResults.get(position));
                            myActivity.startActivity(intent);
                        } else {
                            // delete list
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return false;
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
                    setKeyboardShowing(searchBar, true);
                } else {
                    searchBar.setVisibility(View.GONE);
                    searchTitle.setVisibility(View.VISIBLE);
                    searchActionButton.setVisibility(View.VISIBLE);
                    setKeyboardShowing(searchBar, false);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PoiManager.saveLocationsToFile(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        if (searchBar.hasFocus()) {
            searchBar.clearFocus();
        } else {
            super.onBackPressed();
        }
    }

    private void setKeyboardShowing(EditText editText, boolean showing) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (showing) {
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } else {
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

}
