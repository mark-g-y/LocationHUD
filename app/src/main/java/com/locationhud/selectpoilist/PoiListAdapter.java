package com.locationhud.selectpoilist;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class PoiListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> poiLists;
    private ArrayList<String> defaultPoiLists;

    public PoiListAdapter(Context context, ArrayList<String> poiLists, ArrayList<String> defaultPoiLists) {
        this.context = context;
        this.poiLists = poiLists;
        this.defaultPoiLists = defaultPoiLists;
    }

    public void updateData(ArrayList<String> poiLists, ArrayList<String> defaultPoiLists) {
        this.poiLists = poiLists;
        this.defaultPoiLists = defaultPoiLists;
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public int getChildrenCount(int i) {
        return i == 0 ? poiLists.size() : defaultPoiLists.size();
    }

    @Override
    public Object getGroup(int i) {
        return i == 0 ? poiLists : defaultPoiLists;
    }

    @Override
    public Object getChild(int i, int i2) {
        return i == 0 ? poiLists.get(i2) : defaultPoiLists.get(i2);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i2) {
        return i2;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        GroupViewHolder viewHolder;
        String groupName = groupPosition == 0 ? context.getString(R.string.custom_list) : context.getString(R.string.default_list);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_poi_list_group_item, viewGroup, false);

            viewHolder = new GroupViewHolder();
            viewHolder.groupName = (TextView)view.findViewById(R.id.group_name);

            view.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder)view.getTag();
        }

        viewHolder.groupName.setText(groupName);

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int position, boolean isLastChild, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        String listName = groupPosition == 0 ? poiLists.get(position) : defaultPoiLists.get(position);

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

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    static class ViewHolder {
        TextView title;
    }

    static class GroupViewHolder {
        TextView groupName;
    }
}
