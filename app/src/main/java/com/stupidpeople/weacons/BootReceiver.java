package com.stupidpeople.weacons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stupidpeople.weacons.ready.WifiObserverService;

/**
 * Created by Milenko on 23/03/2016.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WifiObserverService.class));
        //TODO load weacons from sant cugat
    }
}
