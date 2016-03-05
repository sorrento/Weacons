package com.stupidpeople.weacons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.util.Log;


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
                    sendOneWeacon(notificables.get(0), sound, anyFetchable);
                } else {
                    sendSeveralWeacons(notificables, sound, anyFetchable);
                }
            } else {
                mNotificationManager.cancel(mIdNoti);
                myLog.add("Borrada la notifcacion porque no estamos en área de ninguno.", "LIM");
            }
        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
    }

    /**
     * Send a notification that shows a single Weacon
     *
     * @param we
     * @param sound
     * @param needsFetching
     */
    public static void sendOneWeacon(WeaconParse we, boolean sound, boolean needsFetching) {
        try {
            myLog.add("estanmos en send one weacon", tag);

            Class<?> cls = CardsOrBrowser(we);
            Intent intent = getResultIntent(we, mContext, cls);

            //TODO verify this stack works properly
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            stackBuilder.addParentStack(cls);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT); //Todo solve the stack for going back from cards

            NotificationCompat.Builder notification = buildSingleNotification(we, pendingIntent, sound, needsFetching);
            mNotificationManager.notify(mIdNoti, notification.build());
        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
    }

    private static Intent getResultIntent(WeaconParse we, Context mContext, Class<?> cls) {
        Intent intent = new Intent(mContext, cls)
                .putExtra("wName", we.getName())
                .putExtra("wWeaconObId", we.getObjectId())
                .putExtra("wLogo", we.getLogoRounded());

        if (we.getType() == parameters.typeOfWeacon.bus_station) {
            intent.putExtra("wFetchingUrl", we.getFetchingUrl());
        }
        //Complete with other specific cases

        return intent;
    }

    private static Class<?> CardsOrBrowser(WeaconParse we) {
        //TODO verify that card o browser depends on the type of weacon and not specific we
        Class<?> cls;
        parameters.typeOfWeacon typeOfWeacon = we.getType();

        if (typeOfWeacon == parameters.typeOfWeacon.bus_station) {
            cls = CardActivity.class;
        } else {
            cls = BrowserActivity.class;
        }
        return cls;
    }

    ///////////////////

    private static NotificationCompat.Builder buildSingleNotification
            (WeaconParse we, PendingIntent resultPendingIntent, boolean sound, boolean needsFetching) {

        NotificationCompat.Builder notif;

        Intent refreshIntent = new Intent("popo"); //TODO poner el reciever de esto, para que refresque la notif
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);
        NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_silence, "Turn Off", resultPendingIntent);//TODO to create the silence intent


        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_stat_name_hn)
                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(we.getName())
                .setContentText(we.getTypeString())
                .setAutoCancel(true)
                .setTicker("Weacon detected\n" + we.getName())
                .addAction(actionSilence);

        if (needsFetching) notif.addAction(actionRefresh);

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }

        //TODO me gustaría meter la creacion de la notificación dentro del objeto weacons
//        notif = we.getNotification();

        if (we.getType() == parameters.typeOfWeacon.bus_station) {
            BusStop busStop = (BusStop) we.getFetchedElements().get(0);
            notif.setContentText("BUS STOP. " + busStop.summarizeAllLines());

            //InboxStyle
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(we.getName());
            inboxStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");

            StringBuilder sb = new StringBuilder();
            for (SpannableString s : busStop.summarizeByOneLine()) {
                inboxStyle.addLine(s);
                sb.append("   " + s + "\n");
            }

            notif.setStyle(inboxStyle);
            notificationMultiple(we.getName(), sb.toString(), "Currently " + LogInManagement.getActiveWeacons().size() + " weacons active", String.valueOf(sound));
        } else {
            //Bigtext style
            NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
            textStyle.setBigContentTitle(we.getName());
            textStyle.bigText(we.getMessage());
            textStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");
            notif.setStyle(textStyle);
        }

        notif.setContentIntent(resultPendingIntent);

        return notif;
    }

    private static void notificationMultiple(String title, String body, String summary, String sound) {
        myLog.add("***********************************SoUND:" + sound + "\n" + title + "\n" + body + summary + "\n", "NOTI");
    }

    private static void sendSeveralWeacons(ArrayList<WeaconParse> notificables, boolean sound, boolean anyFetchable) {

        Intent refreshIntent = new Intent("popo");
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);

        NotificationCompat.Builder notif;
        Collections.reverse(notificables);

        Intent resultIntent;
        TaskStackBuilder stackBuilder;
        PendingIntent resultPendingIntent;

        String msg = Integer.toString(notificables.size()) + " weacons around you";

        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_stat_name_dup)
                .setLargeIcon(notificables.get(0).getLogoRounded())
                .setContentTitle(msg)
                .setContentText(notificables.get(0).getName() + " and others.")
                .setAutoCancel(true)
                .setDeleteIntent(pendingDeleteIntent)
                .setTicker(msg);

        if (anyFetchable) notif.addAction(actionRefresh);

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setVibrate(new long[]{0, 300, 150, 400, 100})
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(msg);
        inboxStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");

        StringBuilder sb = new StringBuilder();
        for (WeaconParse weacon : notificables) {
            inboxStyle.addLine(weacon.getOneLineSummary());
            sb.append("  " + weacon.getOneLineSummary() + "\n");
        }

        notif.setStyle(inboxStyle);
        resultIntent = new Intent(mContext, WeaconListActivity.class);

        stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(WeaconListActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.setContentIntent(resultPendingIntent);

        myLog.notificationMultiple(msg, sb.toString(), "Currently " + LogInManagement.getActiveWeacons().size() + " weacons active", String.valueOf(sound));
        mNotificationManager.notify(mIdNoti, notif.build());
    }


}
