package com.locationhud.selectpoilist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.locationhud.R;
import com.locationhud.map.ConfirmSelectedLocationDialogCallback;
import com.locationhud.ui.UiUtility;

/**
 * Created by Mark on 12/11/2014.
 */
public class EditPoiListOptionsDialog extends DialogFragment {

    private EditPoiListOptionsDialogCallback callback;
    private String listName;
    private int childPosition;
    private Fragment thisFragment;
    private String[] options;

    public EditPoiListOptionsDialog() {
        thisFragment = this;
        setRetainInstance(true);
    }

    public void initiate(EditPoiListOptionsDialogCallback callback, String listName, int childPosition, String[] options) {
        this.callback = callback;
        this.listName = listName;
        this.childPosition = childPosition;
        this.options = options;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_edit_poi_list_options, container, false);

        getDialog().setTitle(getString(R.string.edit_poi_options));

        ListView lv = (ListView)rootView.findViewById(R.id.edit_poi_options_list);
        lv.setAdapter(new EditPoiListOptionsDialogAdapter(getActivity().getApplicationContext(), options));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    callback.onEdit(listName);
                } else if (position == 1) {
                    callback.onDelete(listName, childPosition);
                }
                dismiss();
            }
        });

        // Do something else
        return rootView;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
