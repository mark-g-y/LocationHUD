package com.locationhud.map;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
    private String yesButtonText;
    private String noButtonText;
    private boolean yesSelected = false;

    public ConfirmSelectedLocationDialog() {
        thisFragment = this;
        setRetainInstance(true);
    }

    public void initiate(ConfirmSelectedLocationDialogCallback confirmSelectedLocationDialogCallback, LatLng location, String name, String yesButtonText, String noButtonText) {
        this.confirmSelectedLocationDialogCallback = confirmSelectedLocationDialogCallback;
        this.selectedLocation = location;
        this.name = name;
        this.yesButtonText = yesButtonText;
        this.noButtonText = noButtonText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.dialog_confirm_selected_location, container, false);

        getDialog().setTitle(getString(R.string.dialog_confirm_selected_location_title));

        EditText locationNameInput = (EditText)rootView.findViewById(R.id.location_name_input);
        locationNameInput.setText(name);
        locationNameInput.selectAll();

        Button noButton = (Button)rootView.findViewById(R.id.cancel_button);
        noButton.setText(noButtonText);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisFragment.dismiss();
                if (noButtonText.equals(getString(R.string.cancel))) {
                    confirmSelectedLocationDialogCallback.onCancel();
                } else if (noButtonText.equals(getString(R.string.delete))) {
                    confirmSelectedLocationDialogCallback.onDelete();
                }
            }
        });

        Button yesButton = (Button)rootView.findViewById(R.id.confirm_button);
        yesButton.setText(yesButtonText);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesSelected = true;
                thisFragment.dismiss();
                EditText locationNameInput = (EditText)rootView.findViewById(R.id.location_name_input);
                confirmSelectedLocationDialogCallback.onYes(locationNameInput.getText().toString());
            }
        });

        // Do something else
        return rootView;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (!yesSelected) {
            confirmSelectedLocationDialogCallback.onCancel();
        }
        try {
            this.dismiss();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
