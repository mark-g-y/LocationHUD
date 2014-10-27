package com.locationhud.leftdrawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.locationhud.R;

import java.util.ArrayList;

/**
 * Created by Mark on 24/10/2014.
 */
public class MenuDrawerListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> options = new ArrayList<String>();

    public MenuDrawerListAdapter(Context context) {
        this.context = context;
        options.add("HUD");
        options.add("Change List");
        options.add("Edit Locations");
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int i) {
        return options.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_menu_drawer_list_adapter, viewGroup, false);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView)view.findViewById(R.id.icon);
            viewHolder.label = (TextView)view.findViewById(R.id.label);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.label.setText(options.get(i));

        return view;
    }

    static class ViewHolder {
        ImageView icon;
        TextView label;
    }
}
