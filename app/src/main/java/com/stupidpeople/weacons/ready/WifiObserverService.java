package com.stupidpeople.weacons.ready;

/**
 * Created by Milenko on 03/03/2016.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.LogBump;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.Notifications;
import com.stupidpeople.weacons.WeaconParse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.myLog;
import util.parameters;

import static com.stupidpeople.weacons.ready.ParseActions.CheckSpotMatches;
import static com.stupidpeople.weacons.ready.ParseActions.DownloadWeaconsIfNeeded;

/**
 * Created by Milenko on 04/10/2015.
 */
public class WifiObserverService extends Service implements ResultCallback<Status> {

    public static boolean serviceIsActive;
    public static SharedPreferences prefs = null;
    String tag = "wifi";
    int iScan = 0;
    private Context mContext;
    private WifiManager wifiManager;
    private WifiReceiver receiverWifi;
    private RefreshReceiver refreshReceiver;

    private void addTestWeacons(HashSet<WeaconParse> weaconsDetected) {
        ParseQuery<WeaconParse> q = ParseQuery.getQuery(WeaconParse.class);
        List<String> arr = Arrays.asList(parameters.weaconsTest);
        List<WeaconParse> res = null;
        try {
            res = q.whereContainedIn("objectId", arr).find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (WeaconParse we : res) {
            we.build(mContext);
            weaconsDetected.add(we);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myLog.add("Starting service ", tag);

        if (serviceIsActive) {
            myLog.add("No empezaermos el servicio", tag);
            stopSelf();
        }

        serviceIsActive = true;
        try {
            myLog.initialize();

            mContext = getApplicationContext();
            wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

            Notifications.Initialize(this);


            //Start listening to Wifi events: (dis)connection and scan results available
            receiverWifi = new WifiReceiver();
            IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            mContext.registerReceiver(receiverWifi, intentFilter);
            Toast.makeText(mContext, "Detection ON", Toast.LENGTH_LONG).show();

            //Refresh & silence & delete notif receiver
            refreshReceiver = new RefreshReceiver();
            IntentFilter filter = new IntentFilter(parameters.refreshIntentName);
            filter.addAction(parameters.silenceIntentName);
            filter.addAction(parameters.deleteIntentName);
            mContext.registerReceiver(refreshReceiver, filter);


            ParseActions.LogInParse();

            //Load weacons if first time
            prefs = getSharedPreferences("com.stupidpeople.weacons", MODE_PRIVATE);
            myLog.add("Is first time rumning" + prefs.getBoolean("firstrunService", true), "aut");
            if (prefs.getBoolean("firstrunService", true)) {
                ParseActions.getNearWeacons(this);
                prefs.edit().putBoolean("firstrunService", false).commit();
            } else {
                DownloadWeaconsIfNeeded(this);
            }

            //Add geofences
//            mGeofenceList = new ArrayList();
//            mGeofenceList.add(new Geofence.Builder()
//                    .setRequestId(id)
//                    .setCircularRegion(lat, lon, radInMts)
//                            //.setExpirationDuration(SyncStateContract.Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
//                    .setExpirationDuration(milSecs)
//                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
//                    .build());
//
//            LocationServices.GeofencingApi.addGeofences(
//                    mGoogleApiClient,
//                    getGeofencingRequest(),
//                    getGeofencePendingIntent()
//            ).setResultCallback(this);

            //TO STOP it:
//            LocationServices.GeofencingApi.removeGeofences(
//                    mGoogleApiClient,
//                    // This is the same pending intent that was used in addGeofences().
//                    getGeofencePendingIntent()
//            ).setResultCallback(this); // Result processed in onResult().
//

        } catch (Exception e) {
            Toast.makeText(mContext, "Not posstible to activate detection ", Toast.LENGTH_LONG).show();
            myLog.add("error starign " + e.getLocalizedMessage(), tag);
        }

//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myLog.add("Destroying ", tag);

        try {
//            mNotificationManager.cancel(101);
            Toast.makeText(mContext, "Detection Service OFF", Toast.LENGTH_LONG).show();
            mContext.unregisterReceiver(receiverWifi);
            mContext.unregisterReceiver(refreshReceiver);
            serviceIsActive = false;
        } catch (Exception e) {
            Toast.makeText(mContext, "Not possible to turn off detection", Toast.LENGTH_LONG).show();
            myLog.add("error destroying: " + e.getLocalizedMessage(), tag);
        }
    }


    @Override
    public void onResult(Status status) {

    }

    private String AggregateMessages(List<ParseObject> list) {
        StringBuilder sb = new StringBuilder();

        for (ParseObject po : list) {
            sb.append(po.getString("msg"));
        }
        return sb.toString();
    }

    private HashSet<WeaconParse> createHashSet(Set<WeaconParse> weaconParses) {
        HashSet<WeaconParse> res = new HashSet<>();

        for (WeaconParse we : weaconParses) res.add(we);

        return res;
    }

    private void transferLogsToParse() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("log");
        int res = query.fromPin(parameters.pinParseLog)
                .count();
        if (res > 30) {
            ParseQuery<ParseObject> q = ParseQuery.getQuery("log");
            q.fromPin(parameters.pinParseLog)
                    .setLimit(300)
                    .findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(final List<ParseObject> list, ParseException e) {
                            String aggregate = AggregateMessages(list);

                            ParseObject po = new ParseObject("log");
                            po.put("msg", aggregate);
                            po.put("type", "Notif");
                            po.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    try {
                                        ParseObject.unpinAll(parameters.pinParseLog, list);
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

        }
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            try {
                action = intent.getAction();

                if (action.equals(parameters.refreshIntentName)) {
                    if (LogInManagement.getActiveWeacons().size() == 0) return;
                    if (!LogInManagement.now.anyInteresting)
                        ParseActions.AddToInteresting(Notifications.getNotifiedWeacons());
                    Notifications.mSilenceButton = true;

//                    LogBump logBump = new LogBump(LogBump.LogType.BTN_REFRESH);
//                    logBump.setReasonToNotify(LogBump.ReasonToNotify.FETCHING);
//                    Notifications.RefreshNotification(logBump);

                    LogInManagement.fetchAllActiveAndInform(mContext, new LogBump(LogBump.LogType.FORCED_REFRESH));

                } else if (action.equals(parameters.silenceIntentName)) {
                    ParseActions.removeInteresting(Notifications.getNotifiedWeacons());

                    //Delete notification
                } else if (action.equals(parameters.deleteIntentName)) {
                    Notifications.isShowingNotification = false;

                }
            } catch (Exception e) {
                myLog.error(e);
            }
        }
    }

    public class WifiReceiver extends BroadcastReceiver {

        public WifiReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            final String action = intent.getAction();

            try {
                if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if ((netInfo.getDetailedState() == (NetworkInfo.DetailedState.CONNECTED))) {
                        myLog.add("*** We just connected to wifi: " + netInfo.getExtraInfo(), tag);

                        transferLogsToParse();
                    }

                } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    List<ScanResult> sr = wifiManager.getScanResults();

                    final LogBump logBump = new LogBump(LogBump.LogType.READ);
                    logBump.setSpots(sr);

                    iScan++;
                    if (iScan % 30 == 0) {
                        ParseActions.DownloadWeaconsIfNeeded(mContext);
                    }

                    CheckSpotMatches(sr, logBump, new CallBackWeacons() {
                        @Override
                        public void OnReceive(HashMap<WeaconParse, ArrayList<String>> weaconHash) {
                            logBump.setWeaconsHash(weaconHash);

                            HashSet<WeaconParse> weaconHashSet = createHashSet(weaconHash.keySet());

                            //TEST insertion of weacons
                            if (parameters.testWeacons) addTestWeacons(weaconHashSet);

                            LogInManagement.setNewWeacons(weaconHashSet, mContext, logBump);
                        }
                    }, mContext);

//                } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
//                    myLog.add("HHHHHHHHHHHHHHHHHHHHHHHHH  DETECTADO UN BOOOOT", "aut");

                } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    myLog.add("Ha encendido la pantalla", "wifi");

                    if (Notifications.isShowingNotification && LogInManagement.now.anyInteresting &&
                            LogInManagement.now.anyFetchable()) {
//                        LogBump logBump = new LogBump(LogBump.LogType.FORCED_REFRESH);
//                        logBump.setReasonToNotify(LogBump.ReasonToNotify.FETCHING);
//                        Notifications.RefreshNotification(logBump);
                        LogInManagement.fetchAllActiveAndInform(mContext, new LogBump(LogBump.LogType.FORCED_REFRESH_ACTIVE_SCREEN));
                    }
                } else {
                    myLog.add("Entering in a different state of network: " + action, tag);
                }
//                How to test BOOT_COMPLETED without restart emulator or real device? It's easy. Try this:
                //                adb -s device-or-emulator-id shell am broadcast -a android.intent.action.BOOT_COMPLETED
//                How to get device id? Get list of connected devices with id's:
                //                adb devices
//                adb in ADT by default you can find in:
                //                    adt-installation-dir/sdk/platform-tools
            } catch (Exception e) {
                myLog.error(e);
            }
        }
    }
}


