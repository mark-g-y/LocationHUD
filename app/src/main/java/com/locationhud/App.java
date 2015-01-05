package com.locationhud;

import android.app.Application;

import com.locationhud.parseapi.AuthenticationData;
import com.parse.Parse;

/**
 * Created by Mark on 04/01/2015.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, AuthenticationData.APPLICATION_ID, AuthenticationData.CLIENT_KEY);
    }

}
