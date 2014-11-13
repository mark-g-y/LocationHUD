package com.locationhud.selectpoilist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.locationhud.R;

/**
 * Created by Mark on 12/11/2014.
 */
public class EditPoiListOptionsDialogAdapter extends BaseAdapter {

    private Context context;
    private String[] options;

    public EditPoiListOptionsDialogAdapter(Context context, String[] options) {
        this.context = context;
        this.options = options;
    }

    @Override
    public int getCount() {
        return options.length;
    }

    @Override
    public Object getItem(int i) {
        return options[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_edit_poi_list_item, viewGroup, false);

            holder = new ViewHolder();
            holder.text = (TextView)view.findViewById(R.id.text);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        holder.text.setText(options[i]);

        return view;
    }

    static class ViewHolder {
        TextView text;
    }
}
