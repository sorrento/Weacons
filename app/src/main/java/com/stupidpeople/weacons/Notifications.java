package com.stupidpeople.weacons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.stupidpeople.weacons.ListActivity.WeaconListActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 17/07/2015.
 */
public abstract class Notifications {

    static String tag = "NOTI";
    private static NotificationManager mNotificationManager;
    private static Context mContext;
    private static int mIdNoti = 103;

    private static int currentId = 1;

    /**
     * Just for testing, it creates a notification with the latests occurrences
     *
     * @param occurrences
     */
    public static void notifyOccurrences(HashMap<WeaconParse, Integer> occurrences) {
        NotificationCompat.Builder notif;

        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .setContentTitle("weacons table")
                .setAutoCancel(true)
                .setOngoing(false)
                .setTicker("Weacons situation");
        //Bigtext style
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        textStyle.setBigContentTitle("Weacon contabilidad");
        textStyle.bigText(WeaconParse.Listar(occurrences));
        notif.setStyle(textStyle);

        mNotificationManager.notify(102, notif.build()); //todo verified this id
    }

    public static void Initialize(Context act) {
        mContext = act;
        mNotificationManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void showNotification(ArrayList<WeaconParse> notificables, boolean sound, boolean anyFetchable) {
        try {
            if (notificables.size() > 0) {
                if (notificables.size() == 1) {
                    sendOneWeacon(notificables.get(0), sound);
                } else {
                    sendSeveralWeacons(notificables, sound, anyFetchable);
                }
            } else {
                mNotificationManager.cancel(mIdNoti);
                myLog.add("Borrada la notifcacion porque no estamos en área de ninguno.", tag);
            }
        } catch (Exception e) {
            myLog.error(e);
        }
    }

    /**
     * Send a notification that shows a single Weacon
     *
     * @param we
     * @param sound
     */
    public static void sendOneWeacon(WeaconParse we, boolean sound) {
        try {
            myLog.add("estanmos en send one weacon", tag);

            PendingIntent pendingIntent = getPendingIntent(we.getActivityClass());

            NotificationCompat.Builder notification = we.buildSingleNotification(pendingIntent, sound, mContext);
            mNotificationManager.notify(mIdNoti, notification.build());
        } catch (Exception e) {
            myLog.error(e);
            ;
        }
    }

    private static PendingIntent getPendingIntent(Class activityClass) {
        Intent resultIntent = new Intent(mContext, activityClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private static void sendSeveralWeacons(ArrayList<WeaconParse> notificables, boolean sound, boolean anyFetchable) {

        NotificationCompat.Builder notif;
        if (LogInManagement.anyChange) Collections.reverse(notificables);


        String msg = Integer.toString(notificables.size()) + " weacons around you";

        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_noti_we_double)
                .setLargeIcon(notificables.get(0).getLogoRounded())
                .setContentTitle(msg)
                .setContentText(notificables.get(0).getName() + " and others.")
                .setAutoCancel(true)
                .setTicker(msg);
//                .setDeleteIntent(pendingDeleteIntent) TODO what should hapen when notification are removed??

        //Refresh Button
        if (anyFetchable) {
            Intent refreshIntent = new Intent(parameters.refreshIntentName);
            PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);

            notif.addAction(actionRefresh);
        }

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setVibrate(new long[]{0, 300, 150, 400, 100})
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(msg);
        inboxStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");

        StringBuilder sb = new StringBuilder();
        for (WeaconParse we : notificables) {
            inboxStyle.addLine(we.NotiOneLineSummary());
            sb.append("  " + we.NotiOneLineSummary() + "\n");
        }

        notif.setStyle(inboxStyle);


        Intent resultIntent = new Intent(mContext, WeaconListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        notif.setContentIntent(pendingIntent);

        myLog.notificationMultiple(msg, sb.toString(), "Currently " + LogInManagement.getActiveWeacons().size() + " weacons active", String.valueOf(sound));
        mNotificationManager.notify(mIdNoti, notif.build());
    }

}
