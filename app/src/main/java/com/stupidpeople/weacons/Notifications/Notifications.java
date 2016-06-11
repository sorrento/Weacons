package com.stupidpeople.weacons.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.ListActivity.WeaconListActivity;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;

import java.util.ArrayList;
import java.util.HashMap;

import util.StringUtils;
import util.myLog;
import util.parameters;

import static com.stupidpeople.weacons.LogInManagement.notifFeatures;
import static com.stupidpeople.weacons.LogInManagement.numberOfActiveNonNotified;
import static com.stupidpeople.weacons.LogInManagement.othersActive;
import static com.stupidpeople.weacons.LogInManagement.weaconsToNotify;

/**
 * Created by Milenko on 17/07/2015.
 */
public class Notifications {

    public static boolean isShowingNotification = false;
//    public static boolean mSilenceButton;

    static String tag = "NOTIF";
    private static NotificationManager mNotificationManager;
    private static Context mContext;
    private static int idNotiOcurrences = 102;
    private static int mIdNoti = 103;
    private static ArrayList<WeaconParse> mWeacons;

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

//    public static void Notify(ArrayList<WeaconParse> weacons, int nOtherActive, boolean sound, boolean autofetching,
//                              boolean buttonRefresh, boolean buttonSilence) {
////        mWeacons = weacons;
////        Collections.reverse(mWeacons); //so the newest appears first
////        mNOtherActive = nOtherActive;
////        mSound = sound;
////        mAutoFetching = autofetching;
////        mRefreshButton = buttonRefresh;
////        mSilenceButton = buttonSilence;
////        mSummary = bottomMessage(mContext);
//
//        //para ver por qué no se borra la notificacion solita
////        myLog.add(".....Weacons a notificar:" + mWeacons.size() + "|butnRefresh=" + buttonRefresh + "|autofet=" + mAutoFetching, tag);
//        Notify();
////        if (buttonRefresh && mAutoFetching) {//if there is the refresh button means at least one is fetchable
//////            logBump.setReasonToNotify(LogBump.ReasonToNotify.FETCHING);
//////            RefreshNotification(logBump);
////            LogInManagement.fetchActiveWeacons(mContext);
////        } else {
////            Notify(logBump);
////        }
//    }

    /**
     * Lanch the notification showing the weacons in LogInManagments and its NotificationFeatures
     */
    public static void Notify() {
//        mWeacons = weaconsToNotify;
//        mNOtherActive = numberOfActiveNonNotified();
//        mNotifFet= notificationFeatures;
//        mSound = sound;
//        mAutoFetching = autofetching;
//        mRefreshButton = buttonRefresh;
//        mSilenceButton = buttonSilence;
//        mSummary = bottomMessage(mContext);
        if (weaconsToNotify == null) {
            myLog.add("Se ha mandado a notificar una lista null de weacons", "WARN");
            return;
        }

        mWeacons = weaconsToNotify;
        int nWe = mWeacons.size();

        if (nWe == 0) {
            if (isShowingNotification) RemoveNotification();
        } else if (nWe == 1) {
            NotifySingle();
        } else {
            NotifyMultiple();
        }
        LogInManagement.notifFeatures.sound = false;
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

    private static void NotifyMultiple() {

        Intent delete = new Intent(parameters.deleteIntentName);
        PendingIntent pIntentDelete = PendingIntent.getBroadcast(mContext, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        NotifFeatures f = notifFeatures;

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

        if (f.sound) addSound(notif);
        if (f.silenceButton) addSilenceButton(notif);
        if (f.refreshButton) addRefreshButton(notif);

        //Inbox
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(cqTitle);
        if (othersActive()) inboxStyle.setSummaryText(bottomMessage(mContext));

        for (WeaconParse we : mWeacons) inboxStyle.addLine(we.inboxSummary());

        notif.setStyle(inboxStyle);

        // On Click
        Intent resultIntent = new Intent(mContext, WeaconListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.setContentIntent(pendingIntent);

        isShowingNotification = true;
        mNotificationManager.notify(mIdNoti, notif.build());

    }

    private static void NotifySingle() {
        WeaconParse we = mWeacons.get(0);

        try {
            PendingIntent pendingIntent = getPendingIntent(we.getActivityClass());
            NotificationCompat.Builder notification = we.buildSingleNotification(pendingIntent, mContext);
            mNotificationManager.notify(mIdNoti, notification.build());
            isShowingNotification = true;

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
        notif.setLights(0xE6D820, 300, 300)
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
        Intent refreshIntent = new Intent(parameters.refreshIntent);
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, mContext.getString(R.string.refresh_button), resultPendingIntentRefresh);

        notif.addAction(actionRefresh);
    }

    public static ArrayList<WeaconParse> getNotifiedWeacons() {
        return mWeacons;
    }

    public static String bottomMessage(Context ctx) {
        String summary = numberOfActiveNonNotified() > 1 ? ctx.getString(R.string.currently_active) :
                ctx.getString(R.string.currently_active_one);
        return String.format(summary, numberOfActiveNonNotified());
    }


//    public static void NotifyRefreshing() {
//        notifFeatures.silenceButton = true;
//        LogInManagement.informWeaconsRefreshing(mContext);
////        markAsRefreshing(true);
////        Notify();
////        markAsRefreshing(false);
//    }

//    public static void NotifyObsololete() {
//        LogInManagement.markAsObsolete(true);
//    }
}
