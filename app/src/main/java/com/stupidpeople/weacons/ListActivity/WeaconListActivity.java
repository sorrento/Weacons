package com.stupidpeople.weacons.ListActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.stupidpeople.weacons.GPSCoordinates;
import com.stupidpeople.weacons.LocationAsker;
import com.stupidpeople.weacons.LocationCallback;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WifiAsker;
import com.stupidpeople.weacons.WifiSpot;
import com.stupidpeople.weacons.askScanResults;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;
import com.stupidpeople.weacons.ready.WifiObserverService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import util.myLog;
import util.parameters;
import util.srComparator;

import static com.stupidpeople.weacons.StringUtils.Listar;

public class WeaconListActivity extends ActionBarActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 101;
    private RecyclerView mRecyclerView;
    private WeaconAdapter adapter;
    private Context mContext;
    private String tag = "ADD_STOP";
    private GPSCoordinates mGps;
    private ArrayList<WeaconParse> activeWeacons;
    private SwipeRefreshLayout mRefresh;
    private newDataReceiver newDataReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_weacon_list_button);

            myLog.initialize();
            mContext = this;

            if (isFirstTime()) ShowExplainationDialog();

            Toast.makeText(mContext, "Estamos creadndo la Actividad", Toast.LENGTH_SHORT).show();


            myLog.add("opening la lista activity", "wifi");
            mRecyclerView = new RecyclerView(this);
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            mRecyclerView.hasFixedSize();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(this, getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

            //Fill the list with launched
            //TODO if there is no data, refresh
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

            mRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
            mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshList(true);
                }
            });

            //Refresh & silence & delete notif receiver
            newDataReceiver = new newDataReceiver();
            mContext.registerReceiver(newDataReceiver, new IntentFilter(parameters.updateInfo));

            mRecyclerView.setAdapter(adapter);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);//for Androis 6
            } else {
                startServiceIfNeeded();
            }

//            refreshList(false);
        } catch (Resources.NotFoundException e) {
            myLog.error(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(mContext, "Estamos retomandola Actividad", Toast.LENGTH_SHORT).show();

        someTest();

        //Show NO WEACON message
        if (LogInManagement.getActiveWeacons().size() == 0) {

            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();

        } else {
//            emptyView.setVisibility(View.GONE);
//            mRecyclerView.setVisibility(View.VISIBLE);
            updateList();

            if (LogInManagement.getActiveWeacons().get(0).isObsolete()) {
                refreshList(false);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startServiceIfNeeded();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.attention)
                    .content(getString(R.string.gps_explaination))
                    .positiveText(R.string.got_it)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

    }

    private void ShowExplainationDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.welcome)
                .content(R.string.inital_explaination)
                .positiveText(R.string.got_it)
//                .negativeText(R.string.disagree)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();//TODO llamar al segundo dialog4
                    }
                })
                .show();
    }

    private boolean isFirstTime() {
        SharedPreferences prefs = getSharedPreferences("com.stupidpeople.weacons", MODE_PRIVATE);

        return prefs.getBoolean("firstrunService", true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(newDataReceiver);
    }

    void refreshList(boolean forced) {

        if (forced) mRefresh.setRefreshing(true);

        Intent intent = new Intent(parameters.refreshIntent);
        intent.putExtra("forced", forced);
        sendBroadcast(intent);

    }

    private void startServiceIfNeeded() {
        boolean isActive = WifiObserverService.serviceIsActive;
        myLog.add("Is service active: " + isActive, "wifi");
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
        if (id == R.id.action_add_stop) {
            actionAddBusStop();
        }

        return true;
    }

    public void actionAddBusStop() {
        Toast.makeText(mContext, R.string.looking_for_busstop, Toast.LENGTH_SHORT).show();

//        final GetCallback<WeaconParse> oneParadaCallback = new GetCallback<WeaconParse>() {
//            @Override
//            public void done(WeaconParse we, ParseException e) {
//                if (e == null) {
//                    int distanceMts = (int) Math.round(we.getGPS().distanceInKilometersTo(mGps.getGeoPoint()) * 1000);
//                    we.build(mContext);
//                    String msg = String.format(getString(R.string.distance_bus_stop), we.getName(), distanceMts);
//
//                    //2. Check if the nearest weacon is inside 15 mts
//                    String msg2;
//                    if (distanceMts < 15) {
//
//                        msg2 = getString(R.string.updating_data);
//                        Toast.makeText(mContext, msg + msg2, Toast.LENGTH_LONG).show();
//
//                        uploadBusStop(we);
//
//                    } else {
//
//                        //bring all bustops in 100mts
//                        myLog.add("Vamos a buscar en los 100 mts", "ADD_STOP");
//                        FindCallback<WeaconParse> listener = new FindCallback<WeaconParse>() {
//                            @Override
//                            public void done(List<WeaconParse> list, ParseException e) {
//                                if (e == null) {
//                                    if (list == null || list.size() == 0) {
//                                        myLog.add("enlos 100 mts hay NO HAY paradsas ", "ADD_STOP");
//                                        Toast.makeText(mContext, R.string.no_bus_stop_100, Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        myLog.add("enlos 100 mts hay paradsas " + list.size(), "ADD_STOP");
//                                        showDialogDecision(list);
//                                    }
//                                } else myLog.add("eerror:" + e.getLocalizedMessage(), "ADD_STOP");
//                            }
//                        };
//                        ParseActions.getBusStopsInRadius(mGps, 0.1, listener);
//
//                        msg2 = getString(R.string.go_closer);
//                        Toast.makeText(mContext, msg + msg2, Toast.LENGTH_LONG).show();
//                    }
//                    myLog.add(msg + msg2, tag);
//
//                } else {
//                    myLog.error(e);
//                }
//            }
//        };

        final FindCallback<WeaconParse> nearestBusStopsCallback = new FindCallback<WeaconParse>() {
            @Override
            public void done(List<WeaconParse> busStops, ParseException e) {
                if (e == null) {
                    myLog.add("... estos son las paradas cercanas en 100 mts " +
                            Listar((ArrayList<WeaconParse>) busStops), tag);

                    showDialogDecision(busStops);

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
                //try with less accuracy
                new LocationAsker(mContext, 18, new LocationCallback() {
                    @Override
                    public void LocationReceived(GPSCoordinates gps) {

                    }

                    @Override
                    public void NotPossibleToReachAccuracy() {
                        Toast.makeText(mContext, getString(R.string.gps_no_accuracy), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void LocationReceived(GPSCoordinates gps, double accuracy) {
                        gotAccuracy(gps, accuracy);
                    }
                });
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {
                gotAccuracy(gps, accuracy);
            }

            private void gotAccuracy(GPSCoordinates gps, double accuracy) {
                Toast.makeText(mContext, getString(R.string.gps_accuracy) + accuracy, Toast.LENGTH_SHORT).show();
                myLog.add("Ya tenemos la loclizacion con precision:" + accuracy + gps, "ADD_STOP");
                mGps = gps;
//                ParseActions.getClosestBusStop(gps, oneParadaCallback);

                ParseActions.getBusStopsInRadius(gps, 100, nearestBusStopsCallback);
            }
        };

        //1. Get accurate position <10m
        new LocationAsker(mContext, 10, locationCallback);

    }

    private void showDialogDecision(final List<WeaconParse> busStops) {
        //create the list of strings

        int size = busStops.size();
        myLog.add("Vamos amostrar el dialog: lista de :" + size, "ADD_STOP");

        MaterialDialog.Builder db = new MaterialDialog.Builder(this);

        if (size == 0) {
            db.title(R.string.no_bustop_title)
                    .content(R.string.no_busstop_message_dialog)
                    .positiveText(R.string.ok);//todo boton de reportar parada que no salta

        } else {
            final ArrayList<String> arr = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                WeaconParse we = busStops.get(i);
                int dist = (int) Math.round(we.getGPS().distanceInKilometersTo(mGps.getGeoPoint()) * 1000);
                String s = we.getParadaId() + " " + we.getName() + " " + dist + "m";
                arr.add(s);
                myLog.add("Opcion:" + s, "ADD_STOP");
            }

            if (size == 1) {
                db.title(R.string.isthisone)
                        .content(arr.get(0))
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                WeaconParse we = busStops.get(0);
                                we.build(mContext);
                                uploadBusStopAndNotify(we);
                            }
                        });
            } else {
                arr.add(getString(R.string.non_of_these));
                db.title(R.string.whichone)
                        .items(arr)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                if (i < arr.size() - 1) {
                                    WeaconParse we = busStops.get(i);
                                    we.build(mContext);
                                    uploadBusStopAndNotify(we);
                                }
                                materialDialog.dismiss();
                            }
                        });
            }
        }

        MaterialDialog d = db.build();
        d.show();

    }

    private void uploadBusStopAndNotify(WeaconParse we) {
        //The notification is forced to show the recently acquired
        HashSet<WeaconParse> myHash = LogInManagement.lastWeaconsDetected;
        we.setInteresting(true);

        if (myHash == null) myHash = new HashSet<>();
        myHash.add(we);

        LogInManagement.setNewWeacons(myHash, mContext);

        SendWifis(we);
    }


    public void OnCLickClear(View view) {
        ParseActions.ClearHomes();
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
                ParseActions.SaveIntensities2(sr, mGps);

                Collections.sort(sr, new srComparator());

                try {
                    final MultiTaskCompleted assignTask = new MultiTaskCompleted() {
                        @Override
                        public void OneTaskCompleted() {
                            //Reload weacons in the area after upload everything
                            Toast.makeText(mContext, "The busstop has been uploaded: " + we.getName(), Toast.LENGTH_SHORT).show();

                            ParseActions.increaseNScannings(we);

                            ParseActions.getNearWifiSpots(mGps.getGeoPoint(), new MultiTaskCompleted() {
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

                    final List<ScanResult> srShort = sr.size() > 4 ? sr.subList(0, 5) : sr;

                    // Quitamos los spots que había y ponemos los nuevos
                    ParseActions.removeSpotsOfWeacon(we, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseActions.assignSpotsToWeacon(we, srShort, mGps, mContext, assignTask);
                            } else {
                                myLog.error(e);
                            }
                        }
                    });
                } catch (Exception e) {
                    myLog.error(e);
                }
            }

            @Override
            public void noWifiDetected() {
                String text = "error recibiendo los sopotsde manera forzasa";
                Toast.makeText(mContext, R.string.no_wifis, Toast.LENGTH_SHORT).show();
                myLog.add(text, "WARN");
            }
        });
    }

    private void updateList() {
        mRefresh.setRefreshing(false);
        activeWeacons = LogInManagement.getActiveWeacons();
        adapter.setWeaconItemList(activeWeacons);
        adapter.notifyDataSetChanged();
    }

    private void someTest() {
//        checkWigleonLocal();
    }

    /**
     * vrificia que haya guardado los wigle en local
     */
    private void checkWigleonLocal() {
        ParseQuery<WifiSpot> q = ParseQuery.getQuery(WifiSpot.class);
        q.whereEqualTo("distanceWe", -1)
                .fromPin(parameters.pinWeacons)
                .findInBackground(new FindCallback<WifiSpot>() {
                    @Override
                    public void done(List<WifiSpot> list, ParseException e) {
                        if (e == null) {
                            myLog.add("wifis wigle on local: " + Listar(list), "aut");

                        } else {
                            myLog.error(e);
                        }
                    }
                });
    }


////Testing

    private class newDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(parameters.updateInfo)) {
                    updateList();
                }
            } catch (Exception e) {
                myLog.error(e);
            }
        }
    }
}

