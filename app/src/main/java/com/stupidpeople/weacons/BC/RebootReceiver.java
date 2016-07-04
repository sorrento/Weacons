package com.stupidpeople.weacons.BC;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stupidpeople.weacons.ready.WifiObserverService;

/**
 * Created by Milenko on 23/03/2016.
 */
public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WifiObserverService.class));
        //TODO load weacons from sant cugat


//        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent2 = new Intent(context, WifiObserverService.class);
////        Intent intent2 = new Intent(context, AlarmReceiver.class);
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//
////        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
////                SystemClock.elapsedRealtime() +
////                        60 * 1000, alarmIntent);
//
//        // Hopefully your alarm will have a lower frequency than this!
////        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
//                AlarmManager.INTERVAL_HALF_HOUR,
//                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
    }
}
