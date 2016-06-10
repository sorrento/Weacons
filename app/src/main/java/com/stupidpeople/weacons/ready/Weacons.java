package com.stupidpeople.weacons.ready;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WifiSpot;
import com.stupidpeople.weacons.secured;

/**
 * Created by Milenko on 04/03/2016.
 */
public class Weacons extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(WeaconParse.class);
        ParseObject.registerSubclass(WifiSpot.class);
        Parse.initialize(this, secured.APPLICATION_ID, secured.CLIENT_KEY);
    }
}
