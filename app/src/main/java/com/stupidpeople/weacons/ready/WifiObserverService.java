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
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.Notifications;
import com.stupidpeople.weacons.WeaconParse;

import java.util.HashSet;
import java.util.List;

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
    private EventsReceiver eventsReceiver;


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
            mContext.registerReceiver(receiverWifi, intentFilter);
            Toast.makeText(mContext, "Detection ON", Toast.LENGTH_LONG).show();

            //Refresh & silence & delete notif receiver
            eventsReceiver = new EventsReceiver();
            IntentFilter filter = new IntentFilter(parameters.refreshIntent);
            filter.addAction(parameters.silenceIntentName);
            filter.addAction(parameters.deleteIntentName);
            filter.addAction(parameters.updateInfo);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            mContext.registerReceiver(eventsReceiver, filter);


            ParseActions.LogInParse();

            //Load weacons if first time
            prefs = getSharedPreferences("com.stupidpeople.weacons", MODE_PRIVATE);
            myLog.add("Is first time rumning" + prefs.getBoolean("firstrunService", true), "aut");
            if (prefs.getBoolean("firstrunService", true)) {
                ParseActions.getNearWifiSpots(this);
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
            mContext.unregisterReceiver(eventsReceiver);
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


    private class EventsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            try {
                action = intent.getAction();

                //Refresh
                if (action.equals(parameters.refreshIntent)) {

                    if (LogInManagement.getActiveWeacons().size() == 0) return;

                    if (!LogInManagement.now.anyInteresting) {
                        ParseActions.AddToInteresting(LogInManagement.weaconsToNotify);
                    }
                    LogInManagement.notifFeatures.silenceButton = true; //se pone desde ya

                    boolean forced = intent.getBooleanExtra("forced", true);
                    myLog.add("RFERESH force=" + forced, "aut");
                    LogInManagement.fetchAllActiveAndInform(mContext, forced);

                    // Silence
                } else if (action.equals(parameters.silenceIntentName)) {
//                    ParseActions.removeInteresting(Notifications.getNotifiedWeacons());
                    LogInManagement.notifFeatures.silenceButton = false;
                    ParseActions.removeInteresting(LogInManagement.weaconsToNotify);
                    Notifications.Notify();

                    // SCREEN on
                } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    myLog.add("Ha encendido la pantalla", "wifi");

                    if (Notifications.isShowingNotification && LogInManagement.now.anyInteresting &&
                            LogInManagement.now.anyFetchable()) {
                        LogInManagement.fetchAllActiveAndInform(mContext, false);
                    }


                    //Delete notification
                } else if (action.equals(parameters.deleteIntentName)) {
                    Notifications.isShowingNotification = false;

                    // Notify
                } else if (action.equals(parameters.updateInfo)) {
                    Notifications.Notify();
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
                    processScanResults(wifiManager.getScanResults());

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

        private void processScanResults(List<ScanResult> sr) {
            iScan++;

            if (iScan % 30 == 0) ParseActions.DownloadWeaconsIfNeeded(mContext);

            CheckSpotMatches(sr, mContext, new CallBackWeacons() {
                @Override
                public void OnReceive(HashSet<WeaconParse> weaconHash) {
                    LogInManagement.setNewWeacons(weaconHash, mContext);
                }
            });
        }
    }
}


