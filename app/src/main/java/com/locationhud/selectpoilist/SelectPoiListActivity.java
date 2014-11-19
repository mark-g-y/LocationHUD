package com.locationhud.selectpoilist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.locationhud.HudActivity;
import com.locationhud.PoiEditMapActivity;
import com.locationhud.PoiManager;
import com.locationhud.R;
import com.locationhud.storage.SharedPreferencesStorage;
import com.locationhud.ui.buttons.CustomButton;
import com.locationhud.utility.IntentTransferCodes;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Mark on 05/11/2014.
 */
public class SelectPoiListActivity extends FragmentActivity {

    private FragmentActivity myActivity;
    private TrieNode customListHead;
    private ArrayList<String> listSearchResults = PoiManager.getCustomPoiLists();
    private EditText searchBar;
    private PoiListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_poi_list);
        PoiManager.readLocationsFromFile(getApplicationContext());

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
                    }
                });

                alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                final AlertDialog dialog = alert.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String value = input.getText().toString();
                        if (PoiManager.getList(value) != null) {
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.poi_name_exists_error), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        PoiManager.addList(value);
                        Intent intent = new Intent(getApplication(), PoiEditMapActivity.class);
                        intent.putExtra(IntentTransferCodes.CURRENT_POI_LIST, value);
                        myActivity.startActivity(intent);
                        listSearchResults.add(value);
                        updateListAdapter();
                        TrieNode.insertString(customListHead, value);
                        dialog.dismiss();
                    }
                });
            }
        });

        listAdapter = new PoiListAdapter(getApplicationContext(), listSearchResults);
        updateListAdapter();
        final ListView poiListView = (ListView)findViewById(R.id.poi_list_view);
        poiListView.setAdapter(listAdapter);
        poiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PoiManager.setCurrentList(listSearchResults.get(position));
                Intent intent = new Intent(myActivity, HudActivity.class);
                startActivity(intent);
            }
        });

        poiListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                CharSequence[] allItems = {getApplicationContext().getString(R.string.edit), getApplicationContext().getString(R.string.delete)};
                final String listName = listSearchResults.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
                builder.setTitle(getString(R.string.edit_poi_options)).setItems(allItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int clickPosition) {
                        if (clickPosition == 0) {
                            Intent intent = new Intent(getApplication(), PoiEditMapActivity.class);
                            intent.putExtra(IntentTransferCodes.CURRENT_POI_LIST, listName);
                            myActivity.startActivity(intent);
                        } else if (clickPosition == 1) {
                            listSearchResults.remove(position);
                            PoiManager.removeList(listName);
                            TrieNode.deleteString(customListHead, listName);
                            updateListAdapter();
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });

        customListHead = TrieNode.createTrie(PoiManager.getCustomPoiLists());

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
                final TrieNode node = TrieNode.getCurrentPosition(customListHead, searchBar.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (node == null) {
                            listSearchResults.clear();
                        } else {
                            listSearchResults = TrieNode.getStringsWithCurrentPrefix(new ArrayList<String>(), node);
                        }
                        updateListAdapter();
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

        CustomButton automatedPoiGenerateButton = (CustomButton)findViewById(R.id.automatic_generate_poi_button);
        automatedPoiGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(myActivity, HudActivity.class);
                intent.putExtra(IntentTransferCodes.IS_AUTOMATED_POI_RETRIEVAL, true);
                startActivity(intent);
            }
        });
        automatedPoiGenerateButton.setOnTouchColourChanges(R.color.item_pressed_translucent, android.R.color.transparent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PoiManager.saveLocationsToFile(getApplicationContext());
        finish();
    }

    @Override
    public void onBackPressed() {
        if (searchBar.hasFocus()) {
            searchBar.clearFocus();
        } else {
            super.onBackPressed();
        }
    }

    private void updateListAdapter() {
        Collections.sort(listSearchResults);
        listAdapter.updateData(listSearchResults);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setKeyboardShowing(EditText editText, boolean showing) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (showing) {
            mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
}
