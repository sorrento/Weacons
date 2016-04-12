package com.stupidpeople.weacons.Temporary;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.stupidpeople.weacons.GPSCoordinates;
import com.stupidpeople.weacons.LocationAsker;
import com.stupidpeople.weacons.LocationCallback;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WifiAsker;
import com.stupidpeople.weacons.askScanResults;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;
import com.stupidpeople.weacons.ready.WifiObserverService;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import util.myLog;

import static com.stupidpeople.weacons.WeaconParse.ListarSR;

public class DebugActivity extends AppCompatActivity {

    private Switch swDetection;
    private String tag = "DBG";
    private Context mContext;
    private GPSCoordinates mGps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        myLog.add("ON create", tag);
        myLog.initialize();

        initializeViews();

        mContext = this;

        //PARSE
//        ParseUserLogIn(); //TODO where to put the login in parse, and the load of weacons?

        startServiceIfNeeded();

    }

    private void startServiceIfNeeded() {
        boolean isActive = WifiObserverService.serviceIsActive;
        myLog.add("Is service active: " + isActive, tag);
        if (!isActive) mContext.startService(new Intent(mContext, WifiObserverService.class));
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

    public void OnClickImIn(View view) {

        Toast.makeText(mContext, R.string.looking_for_busstop, Toast.LENGTH_SHORT).show();

        final GetCallback<WeaconParse> oneParadaCallback = new GetCallback<WeaconParse>() {
            @Override
            public void done(WeaconParse we, ParseException e) {
                if (e == null) {
                    int distanceMts = (int) Math.round(we.getGPS().distanceInKilometersTo(mGps.getGeoPoint()) * 1000);

                    String msg = String.format(getString(R.string.distance_bus_stop), we.getName(), distanceMts);

                    //2. Check if the nearest weacon is inside 15 mts
                    String msg2;
                    if (distanceMts < 15) {
                        //The notification is forced to show the recently acquired
                        HashSet<WeaconParse> myHash = LogInManagement.lastWeaconsDetected;
                        we.setInteresting(true);
                        myHash.add(we);
                        LogInManagement.setNewWeacons(myHash);

                        msg2 = getString(R.string.updating_data);
                        Toast.makeText(mContext, msg + msg2, Toast.LENGTH_LONG).show();
                        SendWifis(we);

                    } else {
                        msg2 = getString(R.string.go_closer);
                        Toast.makeText(mContext, msg + msg2, Toast.LENGTH_LONG).show();
                    }
                    myLog.add(msg + msg2, tag);

                } else {
                    myLog.error(e);
                }

            }
        };

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void LocationReceived(GPSCoordinates gps) {
                myLog.add("tenemos localización   pero sin accuracy" + gps, tag);
            }

            @Override
            public void NotPossibleToReachAccuracy() {

            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {
                Toast.makeText(mContext, "Got Position, Accuracy =" + accuracy, Toast.LENGTH_SHORT).show();
                myLog.add("Ya tenemos la loclizacion con precicion:" + accuracy + gps, tag);
                mGps = gps;
                ParseActions.getClosestBusStop(gps, oneParadaCallback);
            }
        };

        //1. Get accurate position <10m
        new LocationAsker(mContext, locationCallback, 10);
    }

    /**
     * Capture wifis, Keep the five with highest power, Write in web and in local
     * //Marcar como interesting, para que le salte.//TODO
     *
     * @param we
     */
    public void SendWifis(final WeaconParse we) {
        new WifiAsker(mContext, new askScanResults() {
            @Override
            public void OnReceiveWifis(List<ScanResult> sr) {
//                Toast.makeText(mContext, "Recibidos " + sr.size() + "wifis", Toast.LENGTH_SHORT).show();

                Collections.sort(sr, new srComparator());
                myLog.add("****aftersort\n" + ListarSR(sr), tag);

                try {
                    MultiTaskCompleted assignTask = new MultiTaskCompleted() {
                        @Override
                        public void OneTaskCompleted() {
                            //Reload weacons in the area after upload everything
                            ParseActions.getSpotsForBusStops(mGps.getGeoPoint(), new MultiTaskCompleted() {
                                @Override
                                public void OneTaskCompleted() {
                                    //And finally, make the new stop "interesting".
                                    ParseActions.AddToInteresting(we);
                                }

                                @Override
                                public void OnError(Exception e) {

                                }
                            });
                        }

                        @Override
                        public void OnError(Exception e) {
                            myLog.error(e);
                        }
                    };

                    List<ScanResult> srShort = sr.size() > 4 ? sr.subList(0, 5) : sr;

                    ParseActions.assignSpotsToWeacon(we, srShort, mGps, mContext, assignTask);
                } catch (Exception e) {
                    myLog.error(e);
                }
            }

            @Override
            public void noWifiDetected() {
                myLog.add("error recibiendo los sopotsde manera forzasa", "WARN");
            }
        });
    }

    class srComparator implements Comparator<ScanResult> {

        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            return rhs.level - lhs.level;
        }
    }
}
