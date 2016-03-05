package com.stupidpeople.weacons;

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
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import util.myLog;

import static com.stupidpeople.weacons.ParseActions.CheckSpotMatches;

/**
 * Created by Milenko on 04/10/2015.
 */
public class WifiObserverService extends Service {

    String tag = "wfs";

    private Context mContext;
    private WifiManager wifiManager;

    private WifiReceiver receiverWifi;
    private int iScan = 0;
    private NotificationManager mNotificationManager;

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
//TODO
//            showRecordingNotification();
            Notifications.Initialize(this);


            //Start listening to Wifi events: (dis)connection and scan results available
            receiverWifi = new WifiReceiver();
            IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mContext.registerReceiver(receiverWifi, intentFilter);
            Toast.makeText(mContext, "Detection ON", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(mContext, "Not posstible to activate detection ", Toast.LENGTH_LONG).show();
            myLog.add("error starign " + e.getLocalizedMessage(), tag);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * From the list of ScanResults, it looks if SSIDS are present in parse list,
     * and launch the notifications
     *
     * @param sr scanResults
     * @return number of matches
     */
    public void CheckScanResults(final List<ScanResult> sr) {
        iScan++;

        ArrayList<String> bssids = new ArrayList<>();
        ArrayList<String> ssids = new ArrayList<>();
        StringBuilder sb = new StringBuilder(iScan + "+++++++ Scan results:+" + "\n");
        StringBuilder sb2 = new StringBuilder();

        for (ScanResult r : sr) {
            bssids.add(r.BSSID);
            ssids.add(r.SSID);

            sb.append("  '" + r.SSID + "' | " + r.BSSID + " | l= " + r.level + "\n");
            sb2.append(r.SSID + " | ");
        }
        sb.append("+++++++++");
        myLog.add(sb.toString(), tag);

        //TODO remove?
//        updateRecordingNotification("new scann", sb2.toString());

        CheckSpotMatches(bssids, ssids);
    }

    //TODO implement notifications of working service
    private void showRecordingNotification() {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notif;

        notif = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
//                .setLargeIcon(we.getLogoRounded())
                .setContentTitle("Service is active")
//                .setContentText(we.getType())
//                .setAutoCancel(true)
                .setOngoing(true)
//                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
//                .setLights(0xE6D820, 300, 100)
                .setTicker("WIFI watching");
//                .setDeleteIntent(pendingDeleteIntent)
//                .addAction(actionSilence);
//
        mNotificationManager.notify(101, notif.build());


//        Notification not = new Notification(R.drawable.icon, "Application started", System.currentTimeMillis());
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, main.class), Notification.FLAG_ONGOING_EVENT);
//        not.flags = Notification.FLAG_ONGOING_EVENT;
//        not.setLatestEventInfo(this, "Application Name", "Application Description", contentIntent);
//        mNotificationManager.notify(1, not);
    }

    private void updateRecordingNotification(String title, String content) {
//        mNotificationManager.cancel(101);

        NotificationCompat.Builder notif;

        notif = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
//                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(title)
//                .setContentText(we.getType())
//                .setAutoCancel(true)
                .setOngoing(true)
//                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
//                .setLights(0xE6D820, 300, 100)
                .setTicker("WIFI update");
//                .setDeleteIntent(pendingDeleteIntent)
//                .addAction(actionSilence);
        //Bigtext style
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        textStyle.setBigContentTitle("wifis around");
        textStyle.bigText(content);
//        textStyle.bigText(LogInManagement.getContabilidadString());
        notif.setStyle(textStyle);

        mNotificationManager.notify(101, notif.build());


//        Notification not = new Notification(R.drawable.icon, "Application started", System.currentTimeMillis());
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, main.class), Notification.FLAG_ONGOING_EVENT);
//        not.flags = Notification.FLAG_ONGOING_EVENT;
//        not.setLatestEventInfo(this, "Application Name", "Application Description", contentIntent);
//        mNotificationManager.notify(1, not);
    }

    @Override
    public void onDestroy() {
        myLog.add("Destroying ", tag);
        try {
//            mNotificationManager.cancel(101);
            Toast.makeText(mContext, "Detection Service OFF", Toast.LENGTH_LONG).show();
            mContext.unregisterReceiver(receiverWifi);
            super.onDestroy();
        } catch (Exception e) {
            Toast.makeText(mContext, "Not possible to turn off detection", Toast.LENGTH_LONG).show();
            myLog.add("error destroying: " + e.getLocalizedMessage(), tag);
        }
    }

    class WifiReceiver extends BroadcastReceiver {

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
                    CheckScanResults(sr);

                } else {
                    myLog.add("Entering in a different state of network: " + action, tag);
                }
            } catch (Exception e) {
                myLog.add("EEE onReceive" + e.getLocalizedMessage(), "aut");
            }
        }
    }
}


