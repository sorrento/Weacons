package com.stupidpeople.weacons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stupidpeople.weacons.ready.WifiObserverService;

import util.myLog;

/**
 * Created by Milenko on 31/03/2016.
 */
public class BatteryStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        myLog.add("____cambio de estado de la batería " + action, "aut");
        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            myLog.add("____Se ha conectado el cargador", "aut");
            if (WifiObserverService.serviceIsActive) {
                myLog.add("****Service is indeed active", "aut");
            } else {
                myLog.add("****Service is indeed NOT active, will start", "aut");
                context.startService(new Intent(context, WifiObserverService.class));
            }
        }
    }
}
