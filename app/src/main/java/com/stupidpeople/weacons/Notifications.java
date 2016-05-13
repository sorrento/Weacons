package com.stupidpeople.weacons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.stupidpeople.weacons.ListActivity.WeaconListActivity;

import java.util.ArrayList;
import java.util.HashMap;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 17/07/2015.
 */
public class Notifications {

    public static boolean isShowingNotification = false;
    public static boolean mSilenceButton;

    static String tag = "NOTIF";
    //    static Timer t = new Timer();
    private static NotificationManager mNotificationManager;
    private static Context mContext;
    private static int idNotiOcurrences = 102;
    private static int mIdNoti = 103;
    private static ArrayList<WeaconParse> mWeacons;
    private static int mNOtherActive;
    private static boolean mSound;
    private static boolean mAutoFetching;
    private static boolean mRefreshButton;
    private static String mSummary;


    public static void Initialize(Context act) {
        mContext = act;
        mNotificationManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
    }

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
        textStyle.bigText(StringUtils.Listar(occurrences));
        notif.setStyle(textStyle);

        mNotificationManager.notify(idNotiOcurrences, notif.build());
    }

    //Notify

    public static void Notify(ArrayList<WeaconParse> weacons, int nOtherActive, boolean sound, boolean autofetching,
                              boolean buttonRefresh, boolean buttonSilence, LogBump logBump) {
        mWeacons = weacons;
//        Collections.reverse(mWeacons); //so the newest appears first
        mNOtherActive = nOtherActive;
        mSound = sound;
        mAutoFetching = autofetching;
        mRefreshButton = buttonRefresh;
        mSilenceButton = buttonSilence;
        mSummary = bottomMessage(mContext);

        //para ver por qué no se borra la notificacion solita
        myLog.add(".....Weacons a notificar:" + mWeacons.size() + "|butnRefresh=" + buttonRefresh + "|autofet=" + mAutoFetching, tag);
        Notify(logBump);

//        if (buttonRefresh && mAutoFetching) {//if there is the refresh button means at least one is fetchable
////            logBump.setReasonToNotify(LogBump.ReasonToNotify.FETCHING);
////            RefreshNotification(logBump);
//            LogInManagement.fetchActiveWeacons(mContext);
//        } else {
//            Notify(logBump);
//        }
    }

    public static void Notify(LogBump logBump) {
        int nWe = mWeacons.size();
//        t.cancel();

        if (mWeacons == null) {
            myLog.add("Se ha mandado a notificar una lista null de weacons", "OJO");
            return;
        }

        if (nWe == 0) {
            if (isShowingNotification) RemoveNotification();
        } else if (nWe == 1) {
            NotifySingle(logBump);
        } else {
            NotifyMultiple(logBump);
        }
    }

//    public static void RefreshNotification(final LogBump logBump) {//ELIMINAR
//        final int nTotal = mWeacons.size();
//
//        //just show "refreshing"
//        markAsRefreshing(true);
//        logBump.setReasonToNotify(LogBump.ReasonToNotify.PUT_REFRESHING);
//        Notify(logBump);
//        mSound = false;
//
//        MultiTaskCompleted listener = new MultiTaskCompleted() {
//            int iTaksCompleted = 0;
//
//            @Override
//            public void OneTaskCompleted() {
//                iTaksCompleted++;
//                if (iTaksCompleted == nTotal) allHaveBeenFetched(logBump);
//            }
//
//            @Override
//            public void OnError(Exception e) {
//                iTaksCompleted++;
//                if (iTaksCompleted == nTotal) allHaveBeenFetched(logBump);
//            }
//        };
//
//        for (final WeaconParse we : mWeacons) {
//            if (we.notificationRequiresFetching()) {
//                we.fetchForNotification(listener);
//            } else {
//                listener.OneTaskCompleted();
//            }
//        }
//    }

//    private static void allHaveBeenFetched(final LogBump logBump) {
//
//        markAsRefreshing(false);
//        Notify(logBump);
//
//        t = new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                myLog.add("------han pasado los 30seg", LogBump.tag);
//                NotifyRemovingObsoleteInfo(logBump);
//            }
//        }, 30000, 550000);
//    }

//    private static void NotifyRemovingObsoleteInfo(LogBump logBump) {
//        //Removing last info
//        myLog.add("Removing info of paradas (last feching) from everyweacon", LogBump.tag);
//        for (WeaconParse we : mWeacons) {
//            we.setObsolete(true);
//        }
//        logBump.setReasonToNotify(LogBump.ReasonToNotify.REMOVING_OBSOLETE_DATA);
//        Notify(logBump);
//    }

    private static void NotifyMultiple(LogBump logBump) {
        Intent delete = new Intent(parameters.deleteIntentName);
        PendingIntent pIntentDelete = PendingIntent.getBroadcast(mContext, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notif;

        String cqTitle = Integer.toString(mWeacons.size()) + mContext.getString(R.string.weacons_around);
        String cqContent = mWeacons.get(0).getName() + mContext.getString(R.string.and_others);

        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_noti_we_double)
                .setLargeIcon(mWeacons.get(0).getLogoRounded())
                .setContentTitle(cqTitle)
                .setContentText(cqContent)
                .setAutoCancel(true)
                .setDeleteIntent(pIntentDelete)
                .setTicker(cqTitle);

        if (mSound) addSound(notif);
        if (mSilenceButton) addSilenceButton(notif);
        if (mRefreshButton) addRefreshButton(notif);

        //Inbox
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(cqTitle);
        if (mNOtherActive > 0) inboxStyle.setSummaryText(mSummary);

        StringBuilder sb = new StringBuilder();
        for (WeaconParse we : mWeacons) {
            inboxStyle.addLine(we.NotiOneLineSummary());
            sb.append("     " + we.NotiOneLineSummary() + "\n");
        }

        notif.setStyle(inboxStyle);

        // On Click TODO refresh when clicked and compact
//        Intent resultIntent = new Intent(mContext, WeaconListActivity.class);
        Intent resultIntent = new Intent(mContext, WeaconListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.setContentIntent(pendingIntent);

        logBump.setNotificationText(StringUtils.Notif2String(cqTitle, cqContent, cqTitle, sb.toString(), mSummary));
        logBump.build();

        isShowingNotification = true;
        mNotificationManager.notify(mIdNoti, notif.build());

    }

    private static void NotifySingle(LogBump logBump) {
        WeaconParse we = mWeacons.get(0);
        try {
            PendingIntent pendingIntent = getPendingIntent(we.getActivityClass());
            NotificationCompat.Builder notification =
                    we.buildSingleNotification(pendingIntent, mSound, mContext, mRefreshButton, logBump);
            isShowingNotification = true;
            mNotificationManager.notify(mIdNoti, notification.build());

        } catch (Exception e) {
            myLog.error(e);
        }
    }

    private static void RemoveNotification() {
        isShowingNotification = false;
        mNotificationManager.cancel(mIdNoti);
    }

    private static PendingIntent getPendingIntent(Class activityClass) {
        Intent resultIntent = new Intent(mContext, activityClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

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

    public static ArrayList<WeaconParse> getNotifiedWeacons() {
        return mWeacons;
    }

    public static String bottomMessage(Context mContext) {
        String summary = mNOtherActive > 1 ? mContext.getString(R.string.currently_active) :
                mContext.getString(R.string.currently_active_one);
        return String.format(summary, mNOtherActive);
    }

    public static void RemoveSilenceButton() {
        mSilenceButton = false;
        LogBump logBump = new LogBump(LogBump.LogType.BTN_SILENCE);
        logBump.setReasonToNotify(LogBump.ReasonToNotify.REMOVE_SILENCE_BUTTON);
        Notify(logBump);
//        Notify2(mWeacons, mNOtherActive, false, false, true, false);
    }

}
