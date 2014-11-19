package com.locationhud.selectpoilist;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.locationhud.PoiManager;
import com.locationhud.R;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Mark on 05/11/2014.
 */
public class PoiListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> poiLists;

    public PoiListAdapter(Context context, ArrayList<String> poiLists) {
        this.context = context;
        this.poiLists = poiLists;
        //this.poiLists.add(0, context.getString(R.string.autogenerate_poi_prompt));
    }

    public void updateData(ArrayList<String> poiLists) {
        this.poiLists = poiLists;
        //this.poiLists.add(0, context.getString(R.string.autogenerate_poi_prompt));
    }

    @Override
    public int getCount() {
        return poiLists.size();
    }

    @Override
    public Object getItem(int i) {
        return poiLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        String listName = poiLists.get(position);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_poi_list_item, viewGroup, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)view.findViewById(R.id.title);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.title.setText(listName);

        return view;
    }

    static class ViewHolder {
        TextView title;
    }

}
