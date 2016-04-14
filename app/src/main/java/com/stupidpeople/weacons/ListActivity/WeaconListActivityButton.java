package com.stupidpeople.weacons.ListActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import util.myLog;

//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import util.DividerItemDecoration;
//import util.WeaconAdapter;


public class WeaconListActivityButton extends ActionBarActivity {
    private RecyclerView mRecyclerView;
    private WeaconAdapter adapter;
    private Context mContext;
    private String tag = "WLB";
    private GPSCoordinates mGps;
    private ArrayList<WeaconParse> activeWeacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_weacon_list_button);

            myLog.initialize();
            mContext = this;


            startServiceIfNeeded();

            myLog.add("opening la lista activity", "aut");
            mRecyclerView = new RecyclerView(this);
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            mRecyclerView.hasFixedSize();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

            //Fill the list with launched
            //TODO if there is no data, refresh
            //TODO implement refresh button here (tirando para abajo
            activeWeacons = LogInManagement.getActiveWeacons();
            adapter = new WeaconAdapter(this, activeWeacons);
            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //TODO make work click over item
//                    WeaconParse we = (WeaconParse) v.getTag();
//
//                    Intent resultIntent;
//
//                    resultIntent = null;
////                    resultIntent = we.getIntent();
//                    WeaconListActivity.this.startActivity(resultIntent);
//                    overridePendingTransition(R.transition.trans_left_in, R.transition.trans_left_out);
                }
            });
            mRecyclerView.setAdapter(adapter);

            refreshList();
        } catch (Resources.NotFoundException e) {
            myLog.error(e);
        }
    }

    void refreshList() {
        Toast.makeText(this, "Actualidzamons", Toast.LENGTH_SHORT).show();

        LogInManagement.FetchAllActive(new MultiTaskCompleted() {
            @Override
            public void OneTaskCompleted() {
                activeWeacons = LogInManagement.getActiveWeacons();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void OnError(Exception e) {
                myLog.error(e);
            }
        });
    }

    private void startServiceIfNeeded() {
        boolean isActive = WifiObserverService.serviceIsActive;
        myLog.add("Is service active: " + isActive, tag);
        if (!isActive) mContext.startService(new Intent(mContext, WifiObserverService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weacon_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OnClickImIn2(View view) {
        Toast.makeText(mContext, R.string.looking_for_busstop, Toast.LENGTH_SHORT).show();

        final GetCallback<WeaconParse> oneParadaCallback = new GetCallback<WeaconParse>() {
            @Override
            public void done(WeaconParse we, ParseException e) {
                if (e == null) {
                    int distanceMts = (int) Math.round(we.getGPS().distanceInKilometersTo(mGps.getGeoPoint()) * 1000);
                    we.build(mContext);
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
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
                Collections.sort(sr, new srComparator());

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

