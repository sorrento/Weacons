package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.ListActivity.WeaconListActivity;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 18/03/2016.
 */
public abstract class HelperBase {
    protected final Context mContext;
    protected WeaconParse we;

    protected HelperBase(WeaconParse we, Context ctx) {
        mContext = ctx;
        this.we = we;
    }


    protected abstract String typeString();

    protected abstract SpannableString NotiOneLineSummary();

    protected String NotiSingleCompactTitle() {
        return we.getName();
    }

    protected String NotiSingleCompactContent() {
        return we.getTypeString();
    }

    protected String NotiSingleExpandedTitle() {
        return we.getName();
    }

    protected SpannableString NotiSingleExpandedContent() {
        return SpannableString.valueOf(we.getDescription());
    }

    protected Class getActivityClass() {
        return WeaconListActivity.class; //TODO poner cards
    }

    protected NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound,
                                                                 Context mContext, boolean refreshButton) {
        String title = NotiSingleCompactTitle();
        String summary = LogInManagement.bottomMessage(mContext);
        NotificationCompat.Builder notif = baseNotif(mContext, sound, refreshButton);


        //Bigtext style
        SpannableString msg = NotiSingleExpandedContent();
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle()
                .setBigContentTitle(title)
                .bigText(msg);
        if (LogInManagement.othersActive()) textStyle.setSummaryText(summary);

        notif.setStyle(textStyle);


        myLog.logNotification(title, String.valueOf(msg), summary, String.valueOf(false), refreshButton, false);

        notif.setContentIntent(resultPendingIntent);

        return notif;
    }

    protected NotificationCompat.Builder baseNotif(Context mContext, boolean sound, boolean silenceButton) {
        Intent delete = new Intent(parameters.deleteIntentName);
        PendingIntent pIntentDelete = PendingIntent.getBroadcast(mContext, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notif_we)
                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(NotiSingleCompactTitle())
                .setContentText(NotiSingleCompactContent())
                .setAutoCancel(true)
                .setDeleteIntent(pIntentDelete)
                .setTicker("Weacon detected\n" + we.getName());

        if (sound) Notifications.addSound(notif);
        if (silenceButton) Notifications.addSilenceButton(notif);

        return notif;
    }

    protected boolean notificationRequiresFetching() {
        return false;
    }

    protected int getRepeatedOffRemoveFromNotification() {
        return parameters.repeatedOffRemoveFromNotification;
    }
}
