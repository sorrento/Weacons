package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.ListActivity.WeaconListActivity;

import util.parameters;

/**
 * Created by Milenko on 18/03/2016.
 */
public abstract class HelperBase {
    protected final Context mContext;
    protected WeaconParse we;

    protected String mNotifTitle;
    protected String mNotifBottom;
    protected SpannableString mNotifContent;
    protected String mBody;
    private String mNotifCQContent;
    private String mNotifCQTitle;

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

    protected NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent,
                                                                 Context ctx) {

        NotifFeatures f = LogInManagement.notifFeatures;

        mNotifTitle = NotiSingleCompactTitle();
        mNotifBottom = Notifications.bottomMessage(ctx);

        NotificationCompat.Builder notif = baseNotif(ctx, f.sound, f.refreshButton);


        //Bigtext style
        mNotifContent = NotiSingleExpandedContent();
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle()
                .setBigContentTitle(mNotifTitle)
                .bigText(mNotifContent);
        if (LogInManagement.othersActive()) {
            textStyle.setSummaryText(mNotifBottom);
        }

        notif.setStyle(textStyle);


//        myLog.logNotification(title, String.valueOf(msg), summary, false, refreshButton, false);

        notif.setContentIntent(resultPendingIntent);
        mBody = mNotifContent.toString();

        return notif;
    }


    protected NotificationCompat.Builder baseNotif(Context mContext, boolean sound, boolean silenceButton) {
        Intent delete = new Intent(parameters.deleteIntentName);
        PendingIntent pIntentDelete = PendingIntent.getBroadcast(mContext, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotifCQContent = NotiSingleCompactContent();
        mNotifCQTitle = NotiSingleCompactTitle();
        NotificationCompat.Builder notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notif_we)
                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(mNotifCQTitle)
                .setContentText(mNotifCQContent)
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
