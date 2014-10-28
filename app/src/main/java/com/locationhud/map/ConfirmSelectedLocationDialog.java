package com.locationhud.map;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import com.locationhud.R;

/**
 * Created by Mark on 23/10/2014.
 */
public class ConfirmSelectedLocationDialog extends DialogFragment {

    private ConfirmSelectedLocationDialogCallback confirmSelectedLocationDialogCallback;
    private DialogFragment thisFragment;
    private LatLng selectedLocation;
    private String name;

    public ConfirmSelectedLocationDialog() {
        thisFragment = this;
    }

    public void initiate(ConfirmSelectedLocationDialogCallback confirmSelectedLocationDialogCallback) {
        this.confirmSelectedLocationDialogCallback = confirmSelectedLocationDialogCallback;
    }

    public void initiate(LatLng location, String name) {
        this.selectedLocation = location;
        this.name = name;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_confirm_selected_location, container, false);

        TextView messageTextView = (TextView)rootView.findViewById(R.id.confirmation_message);
        messageTextView.setText("Edit this point");

        EditText locationNameInput = (EditText)rootView.findViewById(R.id.location_name_input);
        locationNameInput.setText(name);

        Button noButton = (Button)rootView.findViewById(R.id.cancel_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisFragment.dismiss();
                confirmSelectedLocationDialogCallback.onCancel();
            }
        });

        Button yesButton = (Button)rootView.findViewById(R.id.confirm_button);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisFragment.dismiss();
                EditText locationNameInput = (EditText)rootView.findViewById(R.id.location_name_input);
                confirmSelectedLocationDialogCallback.onYes(locationNameInput.getText().toString());
            }
        });

        // Do something else
        return rootView;
    }
}
