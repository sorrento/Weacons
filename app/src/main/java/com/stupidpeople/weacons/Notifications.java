package com.stupidpeople.weacons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.stupidpeople.weacons.ListActivity.WeaconListActivityButton;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 17/07/2015.
 */
public class Notifications {

    public static boolean isShowingNotification = false;
    public static boolean mSilenceButton;
    static String tag = "NOTI";
    static Timer t = new Timer();
    static boolean obsolete = false;
    private static NotificationManager mNotificationManager;
    private static Context mContext;
    private static int idNotiOcurrnces = 102;
    private static int mIdNoti = 103;
    private static ArrayList<WeaconParse> mWeacons;
    private static int mNOtherActive;
    private static boolean mSound;
    private static boolean mAutoFetching;
    private static boolean mRefreshButton;
    private static String mSummary;

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

        mNotificationManager.notify(idNotiOcurrnces, notif.build());
    }

    public static void Initialize(Context act) {
        mContext = act;
        mNotificationManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void Notify2(ArrayList<WeaconParse> weacons, int nActive, boolean sound, boolean autofetching,
                               boolean buttonRefresh, boolean buttonSilence) {
        mWeacons = weacons;
        Collections.reverse(mWeacons); //so the newest appears first
        mNOtherActive = nActive;
        mSound = sound;
        mAutoFetching = autofetching;
        mRefreshButton = buttonRefresh;
        mSilenceButton = buttonSilence;
        mSummary = bottomMessage(mContext);

        if (buttonRefresh && mAutoFetching) {//if there is the refresh button means at least one is fetchable
            RefreshNotification2();
        } else {
            Notify2();
        }
    }

    private static void Notify2() {
        int nWe = mWeacons.size();
        t.cancel();

        if (mWeacons == null) {
            myLog.add("Se ha mandado a notificar una lista null de weacons", "OJO");
            return;
        }

        if (nWe == 0) {
            if (isShowingNotification) RemoveNotification2();
        } else if (nWe == 1) {
            NotifySingle2();
        } else {
            NotifyMultiple2();
        }
    }

    public static void RefreshNotification2() {
        final int nTotal = mWeacons.size();


        MultiTaskCompleted listener = new MultiTaskCompleted() {
            int iTaksCompleted = 0;

            @Override
            public void OneTaskCompleted() {
                iTaksCompleted++;
                if (iTaksCompleted == nTotal) allFetched2();
            }

            @Override
            public void OnError(Exception e) {
                iTaksCompleted++;
                if (iTaksCompleted == nTotal) allFetched2();
            }
        };

        for (final WeaconParse we : mWeacons) {
            if (we.notificationRequiresFetching()) {
                we.fetchForNotification(listener);
            } else {
                listener.OneTaskCompleted();
            }
        }
    }

    private static void allFetched2() {

        Notify2();
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                myLog.add("han pasado los 30seg", tag);
                NotifyRemovingObsoleteInfo2();
            }
        }, 30000, 550000);
    }

    private static void NotifyRemovingObsoleteInfo2() {
        //Removing last info
        myLog.add("Removing info of paradas (last feching) from everyweacon", tag);
        for (WeaconParse we : mWeacons) {
            we.setObsolete(true);
        }
        Notify2();
    }

    private static void NotifyMultiple2() {
        Intent delete = new Intent(parameters.deleteIntentName);
        PendingIntent pIntentDelete = PendingIntent.getBroadcast(mContext, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notif;

        String title = Integer.toString(mWeacons.size()) + mContext.getString(R.string.weacons_around);

        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_noti_we_double)
                .setLargeIcon(mWeacons.get(0).getLogoRounded())
                .setContentTitle(title)
                .setContentText(mWeacons.get(0).getName() + mContext.getString(R.string.and_others))
                .setAutoCancel(true)
                .setDeleteIntent(pIntentDelete)
                .setTicker(title);

        if (mSound) addSound(notif);
        if (mSilenceButton) addSilenceButton(notif);
        if (mRefreshButton) addRefreshButton(notif);

        //Inbox
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);
        if (mNOtherActive > 0) inboxStyle.setSummaryText(mSummary);

        StringBuilder sb = new StringBuilder();
        for (WeaconParse we : mWeacons) {
            inboxStyle.addLine(we.NotiOneLineSummary());
            sb.append("  " + we.NotiOneLineSummary() + "\n");
        }

        notif.setStyle(inboxStyle);

        // On Click TODO refresh when clicked and compact
//        Intent resultIntent = new Intent(mContext, WeaconListActivity.class);
        Intent resultIntent = new Intent(mContext, WeaconListActivityButton.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.setContentIntent(pendingIntent);

        myLog.logNotification(title, sb.toString(), mSummary, mSound, mSilenceButton, mRefreshButton);

        mNotificationManager.notify(mIdNoti, notif.build());

    }

    public static String bottomMessage(Context mContext) {
        String summary = mNOtherActive > 1 ? mContext.getString(R.string.currently_active) : mContext.getString(R.string.currently_active_one);
        return String.format(summary, mNOtherActive);
    }

    private static void NotifySingle2() {
        WeaconParse we = mWeacons.get(0);
        try {
            PendingIntent pendingIntent = getPendingIntent(we.getActivityClass());
            NotificationCompat.Builder notification =
                    we.buildSingleNotification(pendingIntent, mSound, mContext, mRefreshButton);
            mNotificationManager.notify(mIdNoti, notification.build());
        } catch (Exception e) {
            myLog.error(e);
        }
    }

    private static void RemoveNotification2() {
        isShowingNotification = false;
        mNotificationManager.cancel(mIdNoti);
    }


//    public static void showNotification(ArrayList<WeaconParse> notificables, boolean sound,
//                                        boolean anyFetchable, boolean anyInteresting) {
//        try {
//            t.cancel();
//            if (notificables.size() > 0) {
//
//                if (anyFetchable && anyInteresting && !obsolete) {
//                    t = new Timer();
//                    t.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            myLog.add("han pasado los 30seg", tag);
//                            LogInManagement.NotifyRemovingObsoleteInfo();
//                        }
//                    }, 30000, 550000);
//
//                }
//
//                isShowingNotification = true;
//                if (notificables.size() == 1) {
//                    sendOneWeacon(notificables.get(0), sound);
//                } else {
//                    sendSeveralWeacons(notificables, sound, anyFetchable, anyInteresting);
//                }
//            } else {
//                isShowingNotification = false;
//                mNotificationManager.cancel(mIdNoti);
//                myLog.add("Borrada la notifcacion porque no estamos en área de ninguno.", tag);
//            }
//        } catch (Exception e) {
//            myLog.error(e);
//        }
//    }

//    /**
//     * Send a notification that shows a single Weacon
//     *
//     * @param we
//     * @param sound
//     */
//    public static void sendOneWeacon(WeaconParse we, boolean sound) {
//        try {
//            myLog.add("estanmos en send one weacon", tag);
//
//            PendingIntent pendingIntent = getPendingIntent(we.getActivityClass());
//
//            NotificationCompat.Builder notification = we.buildSingleNotification(pendingIntent, sound, mContext, we.isInteresting());
//            mNotificationManager.notify(mIdNoti, notification.build());
//        } catch (Exception e) {
//            myLog.error(e);
//        }
//    }

    private static PendingIntent getPendingIntent(Class activityClass) {
        Intent resultIntent = new Intent(mContext, activityClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

//    private static void sendSeveralWeacons(ArrayList<WeaconParse> notificables,
//                                           boolean anyInterestingAppearing, boolean refreshButton, boolean silenceButton) {
//        Intent delete = new Intent(parameters.deleteIntentName);
//        PendingIntent pIntentDelete = PendingIntent.getBroadcast(mContext, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder notif;
//
////        if (LogInManagement.newAppearance) Collections.reverse(notificables);
//
//        String msg = Integer.toString(notificables.size()) + mContext.getString(R.string.weacons_around);
//
//        notif = new NotificationCompat.Builder(mContext)
//                .setSmallIcon(R.drawable.ic_noti_we_double)
//                .setLargeIcon(notificables.get(0).getLogoRounded())
//                .setContentTitle(msg)
//                .setContentText(notificables.get(0).getName() + mContext.getString(R.string.and_others))
//                .setAutoCancel(true)
//                .setDeleteIntent(pIntentDelete)
//                .setTicker(msg);
//
//        if (anyInterestingAppearing) addSound(notif);
//        if (silenceButton) addSilenceButton(notif);
//        if (refreshButton) addRefreshButton(notif);
//        String summary = "";// LogInManagement.bottomMessage(mContext);
//
//
//        //Inbox
//        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//        inboxStyle.setBigContentTitle(msg);
//        if (LogInManagement.othersActive()) inboxStyle.setSummaryText(summary);
//
//        StringBuilder sb = new StringBuilder();
//        for (WeaconParse we : notificables) {
//            inboxStyle.addLine(we.NotiOneLineSummary());
//            sb.append("  " + we.NotiOneLineSummary() + "\n");
//        }
//
//        notif.setStyle(inboxStyle);
//
//        // On Click
//        Intent resultIntent = new Intent(mContext, WeaconListActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        notif.setContentIntent(pendingIntent);
//
////        myLog.logNotification(msg, sb.toString(), summary,
////                String.valueOf(anyInterestingAppearing), silenceButton, refreshButton);
//
//        mNotificationManager.notify(mIdNoti, notif.build());
//    }

    public static void addSound(NotificationCompat.Builder notif) {
        notif.setLights(0xE6D820, 300, 100)
                .setVibrate(new long[]{0, 300, 150, 400, 100})
                .setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
    }

    public static void addSilenceButton(NotificationCompat.Builder notif) {
        Intent resultIntent = new Intent(parameters.silenceIntentName);
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_volume_off_white_24dp,
                mContext.getString(R.string.silence), resultPendingIntent);
        notif.addAction(actionSilence);
    }

    public static void addRefreshButton(NotificationCompat.Builder notif) {
        Intent refreshIntent = new Intent(parameters.refreshIntentName);
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, mContext.getString(R.string.refresh_button), resultPendingIntentRefresh);

        notif.addAction(actionRefresh);
    }

//    public static void refreshNotifications() {
//        Intent refreshIntent = new Intent(parameters.refreshIntentName);
//        myLog.add("mandando un inten t parra refrescar las notificaciones", "aut");
//        mContext.sendBroadcast(refreshIntent);
//    }

//    public static boolean areObsolete() {
//        return obsolete;
//    }

    public static ArrayList<WeaconParse> getNotifiedWeacons() {
        return mWeacons;
    }

    public static void RemoveSilenceButton2() {
        mSilenceButton = false;
        Notify2();
//        Notify2(mWeacons, mNOtherActive, false, false, true, false);
    }

}
