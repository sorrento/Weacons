package com.stupidpeople.weacons.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stupidpeople.weacons.ready.WifiObserverService;

import util.myLog;

/**
 * Created by Milenko on 31/03/2016.
 */
public class ServiceRevival extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isActive = WifiObserverService.serviceIsActive;

        myLog.add(intent.getAction() + ". Was active? " + isActive, "REVIVAL");

        if (!isActive) context.startService(new Intent(context, WifiObserverService.class));
    }
}
