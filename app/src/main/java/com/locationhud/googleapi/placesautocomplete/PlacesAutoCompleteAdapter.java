package com.locationhud.googleapi.placesautocomplete;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by Mark on 28/10/2014.
 */
public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    public ArrayList<String> resultListId;
    private ArrayList<String> resultList;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    ArrayList<ArrayList<String>> results = PlacesAutocomplete.autocomplete(constraint.toString());
                    resultList = results.get(0);
                    resultListId = results.get(1);

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    public String getPlaceId(int position) {
        return resultListId.get(position);
    }

    public boolean hasElements() {
        if (resultListId == null || resultListId.size() == 0) {
            return false;
        }
        return true;
    }
}
