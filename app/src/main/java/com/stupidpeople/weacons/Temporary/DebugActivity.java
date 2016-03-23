package com.stupidpeople.weacons.Temporary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.stupidpeople.weacons.GPSCoordinates;
import com.stupidpeople.weacons.LocationAsker;
import com.stupidpeople.weacons.LocationCallback;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.ready.ParseActions;
import com.stupidpeople.weacons.ready.WifiObserverService;

import util.myLog;
import util.parameters;

public class DebugActivity extends AppCompatActivity {

    private Switch swDetection;
    private String tag = "DBG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        myLog.add("ON create", tag);
        myLog.initialize();

        retrieveSpotsAround(false, parameters.radioSpotsQuery);

        initializeViews();

        //PARSE
        ParseUserLogIn(); //TODO where to put the login in parse, and the load of weacons?

    }

    @Override
    protected void onResume() {
        super.onResume();
        myLog.add("Onresume", tag);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        myLog.add("On postresume", tag);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myLog.add("ON destroyr", tag);
    }

    @Override
    protected void onStop() {
        super.onStop();
        myLog.add("Se ha detenido la actividad princial (debug", tag);
    }

    private void initializeViews() {
        swDetection = (Switch) findViewById(R.id.sw_detection);
        swDetection.setChecked(false);
        swDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context mContext = getApplicationContext();
                if (isChecked) {
                    mContext.startService(new Intent(mContext, WifiObserverService.class));
                } else {
                    mContext.stopService(new Intent(mContext, WifiObserverService.class));
                }
            }
        });


    }

    /**
     * Load the spots from parse that are around from current postion
     *
     * @param bLocal If they are stored in local database
     * @param radio
     */
    private void retrieveSpotsAround(final boolean bLocal, final double radio) {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void LocationReceived(GPSCoordinates gps) {
                if (gps == null) {
                    myLog.add("Location is null", tag);//TODO loaction shlould never be null
                    gps = new GPSCoordinates(parameters.stCugat);
                }
                ParseActions.getSpots(bLocal, radio, gps, getApplicationContext());
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {
                myLog.add("recibido comprorecision, aunque no requrido", tag);

            }
        };
        new LocationAsker(this, locationCallback);
    }

    private void ParseUserLogIn() {

        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            myLog.add("sin user, vamos a loggear", tag);

            //TODO eencript parse user & pass
            ParseUser.logInInBackground("sorrento2", "spidey", new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        myLog.add("Logged in", tag);
                    } else {
                        myLog.add("Not Logged in", tag);
                    }
                }
            });

        } else {
            myLog.add("Ya tenia user,", tag);
        }
    }
}
