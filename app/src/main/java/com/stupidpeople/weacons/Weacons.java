package com.stupidpeople.weacons;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Milenko on 04/03/2016.
 */
public class Weacons extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
//        ParseObject.registerSubclass(HitSapo.class);
        ParseObject.registerSubclass(WeaconParse.class);
        ParseObject.registerSubclass(WifiSpot.class);
        Parse.initialize(this, "CADa4nX2Lx29QEJlC3LUY1snbjq9zySlF5S3YSVG", "hC9VWCmGEBxb9fSGQPiOjSInaAPnYMZ0t8k3V0UO");
//we are using the credential for other app..problem?
    }
}
