package com.locationhud.utility;

import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by Mark on 07/06/2015.
 */
public class ToolbarUtility {

    public static Toolbar initWithBackButton(final ActionBarActivity activity, String title, int toolbarId) {
        Toolbar toolbar = (Toolbar)activity.findViewById(toolbarId);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });

        return toolbar;
    }

}
