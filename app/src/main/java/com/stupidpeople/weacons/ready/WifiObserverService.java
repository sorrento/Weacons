package com.stupidpeople.weacons.ready;

/**
 * Created by Milenko on 03/03/2016.
 */

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.Notifications;
import com.stupidpeople.weacons.WeaconParse;

import java.util.HashSet;
import java.util.List;

import util.myLog;
import util.parameters;

import static com.stupidpeople.weacons.WeaconParse.Listar;
import static com.stupidpeople.weacons.ready.ParseActions.CheckSpotMatches;

/**
 * Created by Milenko on 04/10/2015.
 */
public class WifiObserverService extends Service {

    String tag = "wos";
    int iScan = 0;

    private Context mContext;
    private WifiManager wifiManager;

    private WifiReceiver receiverWifi;
    private NotificationManager mNotificationManager;
    private RefreshReceiver refreshReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myLog.add("Starting service ", tag);

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


        } catch (Exception e) {
            Toast.makeText(mContext, "Not posstible to activate detection ", Toast.LENGTH_LONG).show();
            myLog.add("error starign " + e.getLocalizedMessage(), tag);
        }

//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        myLog.add("Destroying ", tag);
        try {
//            mNotificationManager.cancel(101);
            Toast.makeText(mContext, "Detection Service OFF", Toast.LENGTH_LONG).show();
            mContext.unregisterReceiver(receiverWifi);
            mContext.unregisterReceiver(refreshReceiver);
            super.onDestroy();
        } catch (Exception e) {
            Toast.makeText(mContext, "Not possible to turn off detection", Toast.LENGTH_LONG).show();
            myLog.add("error destroying: " + e.getLocalizedMessage(), tag);
        }
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            try {
                action = intent.getAction();

                myLog.add("+++Pressed:" + action, "aut");

                if (action.equals(parameters.refreshIntentName)) {
                    ParseActions.AddToInteresting(LogInManagement.getNotifiedWeacons());
                    LogInManagement.refresh(context);

                } else if (action.equals(parameters.silenceIntentName)) {
                    ParseActions.removeInteresting(LogInManagement.getNotifiedWeacons());
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
                    }

                } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    List<ScanResult> sr = wifiManager.getScanResults();

                    iScan++;
                    myLog.add(Integer.toString(iScan) + "--------------------------------------------------------------", "MHP");
                    myLog.add("\tSSIDS:" + sr.size() + "\n" + WeaconParse.ListarSR(sr), tag);

                    CheckSpotMatches(sr, new CallBackWeacons() {
                        @Override
                        public void OnReceive(HashSet<WeaconParse> weaconHashSet) {
                            myLog.add(" " + Listar(weaconHashSet), "MHP");
                            LogInManagement.setNewWeacons(weaconHashSet);
                        }
                    }, mContext);

//                } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
//                    myLog.add("HHHHHHHHHHHHHHHHHHHHHHHHH  DETECTADO UN BOOOOT", "aut");

                } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    myLog.add("Ha encendido la pantalla", "aut");

                    if (Notifications.isShowingNotification && LogInManagement.now.anyInteresting &&
                            LogInManagement.now.anyFetchable() && Notifications.areObsolete()) {
                        Notifications.refreshNotifications();
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
                myLog.add(Log.getStackTraceString(e), "err");
            }
        }
    }
}


