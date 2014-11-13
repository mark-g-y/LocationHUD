package com.locationhud.selectpoilist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.locationhud.PoiEditMapActivity;
import com.locationhud.PoiManager;
import com.locationhud.R;
import com.locationhud.utility.ActivityResultCodes;
import com.locationhud.utility.IntentTransferCodes;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Mark on 05/11/2014.
 */
public class SelectPoiListActivity extends FragmentActivity implements EditPoiListOptionsDialogCallback {

    private FragmentActivity myActivity;
    private EditPoiListOptionsDialogCallback callback;
    private TrieNode customListHead;
    private TrieNode defaultListHead;
    private ArrayList<String> customListSearchResults = PoiManager.getCustomPoiLists();
    private ArrayList<String> defaultListSearchResults = PoiManager.getDefaultPoiLists();
    private EditText searchBar;
    private PoiListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myActivity = this;
        callback = this;
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
                        customListSearchResults.add(value);
                        updateListAdapter();
                        TrieNode.insertString(customListHead, value);
                        dialog.dismiss();
                    }
                });
            }
        });

        listAdapter = new PoiListAdapter(getApplicationContext(), PoiManager.getCustomPoiLists(), PoiManager.getDefaultPoiLists());
        updateListAdapter();
        final ExpandableListView poiListView = (ExpandableListView)findViewById(R.id.poi_expandable_list_view);
        poiListView.setAdapter(listAdapter);
        poiListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int position, long l) {
                if (groupPosition == 0) {
                    PoiManager.setCurrentList(customListSearchResults.get(position));
                } else {
                    PoiManager.setCurrentList(defaultListSearchResults.get(position));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                    }
                });
                onBackPressed();
                return false;
            }
        });

        poiListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                int itemType = ExpandableListView.getPackedPositionType(id);

                if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    final int childPosition = ExpandableListView.getPackedPositionChild(id);
                    final int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    String[] allItems = {getApplicationContext().getString(R.string.edit), getApplicationContext().getString(R.string.delete)};
                    String[] editOnlyItems = {getApplicationContext().getString(R.string.edit)};

                    final String[] items = groupPosition == 0 ? allItems : editOnlyItems;
                    EditPoiListOptionsDialog poiListOptionsDialog = new EditPoiListOptionsDialog();
                    String listName = groupPosition == 0 ? customListSearchResults.get(childPosition) : defaultListSearchResults.get(childPosition);
                    poiListOptionsDialog.initiate(callback, listName, childPosition, items);
                    poiListOptionsDialog.show(myActivity.getSupportFragmentManager(), "EDIT");
//                    AlertDialog.Builder builder = new AlertDialog.Builder(SelectPoiListActivity.this);
//                    builder.setTitle(getString(R.string.edit_poi_options));
//                    builder.setItems(items, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int item) {
//                            String listName = groupPosition == 0 ? customListSearchResults.get(childPosition) : defaultListSearchResults.get(childPosition);
//                            if (item == 0) {
//                                Intent intent = new Intent(getApplication(), PoiEditMapActivity.class);
//                                intent.putExtra(IntentTransferCodes.CURRENT_POI_LIST, listName);
//                                myActivity.startActivity(intent);
//                            } else {
//                                // cannot delete default list - must be a custom list before we can enable delete
//                                if (groupPosition == 0) {
//                                    customListSearchResults.remove(childPosition);
//                                    PoiManager.removeList(listName);
//                                    TrieNode.deleteString(customListHead, listName);
//                                    updateListAdapter();
//                                }
//                            }
//                        }
//                    });
//                    AlertDialog alert = builder.create();
//                    alert.show();

                    return true;

                } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    //do your per-group callback here
                    return false;
                }
                return false;
            }
        });
        poiListView.expandGroup(0);
        poiListView.expandGroup(1);

        customListHead = TrieNode.createTrie(PoiManager.getCustomPoiLists());
        defaultListHead = TrieNode.createTrie(PoiManager.getDefaultPoiLists());

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
                final TrieNode defaultListNode = TrieNode.getCurrentPosition(defaultListHead, searchBar.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (node == null) {
                            customListSearchResults.clear();
                        } else {
                            customListSearchResults = TrieNode.getStringsWithCurrentPrefix(new ArrayList<String>(), node);
                        }
                        if (defaultListNode == null) {
                            defaultListSearchResults.clear();
                        } else {
                            defaultListSearchResults = TrieNode.getStringsWithCurrentPrefix(new ArrayList<String>(), defaultListNode);
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
        Collections.sort(customListSearchResults);
        Collections.sort(defaultListSearchResults);
        listAdapter.updateData(customListSearchResults, defaultListSearchResults);
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

    @Override
    public void onEdit(String listName) {
        Intent intent = new Intent(getApplication(), PoiEditMapActivity.class);
        intent.putExtra(IntentTransferCodes.CURRENT_POI_LIST, listName);
        myActivity.startActivity(intent);
    }

    @Override
    public void onDelete(String listName, int childPosition) {
        customListSearchResults.remove(childPosition);
        PoiManager.removeList(listName);
        TrieNode.deleteString(customListHead, listName);
        updateListAdapter();
    }
}
