package com.stupidpeople.weacons.Wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

import util.myLog;

/**
 * Created by halatm on 08/12/2015.
 */
public class WifiAsker {
    private final askScanResults mListener;
    private final WifiReceiver receiver;
    private final Context mContext;
    private WifiManager wifi;

    public WifiAsker(Context context, askScanResults listener) {
        mListener = listener;
        mContext = context;

        //la busqueda y la escucha está en el mismo contexto
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        receiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(receiver, intentFilter);
        myLog.add("lanzo la peticion wifis", "aut");
        wifi.startScan();
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                List<ScanResult> sr = wifi.getScanResults();
                if (sr != null) {
                    mListener.OnReceiveWifis(sr);
                } else {
                    mListener.noWifiDetected();
                }
//                for (ScanResult r : sr) {
//                    if (!lista.contains(r.SSID) && !r.SSID.equals("")) {  //we omit repeated ssids or =""
//                        lista.add(r.SSID);
//                        myLog.add("..." + r.SSID);
//                    }
//                }
//                myLog.add("that's all");

            } else {
                myLog.add("Entering in a different state of network: " + action, "CON");
            }
            mContext.unregisterReceiver(receiver);
        }
    }
}
