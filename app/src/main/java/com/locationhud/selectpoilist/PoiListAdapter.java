package com.locationhud.selectpoilist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.locationhud.R;
import com.locationhud.compassdirection.MapPoint;

import java.util.ArrayList;

/**
 * Created by Mark on 05/11/2014.
 */
public class PoiListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> poiLists;

    public PoiListAdapter(Context context, ArrayList<String> poiLists) {
        this.context = context;
        this.poiLists = poiLists;
    }

    public void updateData(ArrayList<String> poiLists) {
        this.poiLists = poiLists;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_poi_list_item, viewGroup, false);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)view.findViewById(R.id.title);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.title.setText(poiLists.get(i));

        return view;
    }

    static class ViewHolder {
        TextView title;
    }
}
