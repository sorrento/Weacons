package com.stupidpeople.weacons.Helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.ListActivity.WeaconListActivity;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.Notifications.NotifFeatures;
import com.stupidpeople.weacons.Notifications.Notifications;
import com.stupidpeople.weacons.R;

import util.StringUtils;
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

    protected Class getActivityClass() {
        return WeaconListActivity.class; //TODO poner cards
    }

    protected boolean notificationRequiresFetching() {
        return false;
    }

    protected int getRepeatedOffRemoveFromNotification() {
        return parameters.repeatedOffRemoveFromNotification;
    }


    // Notifications' content

    protected String oneLineSummary() {
        return typeString();
    }


    ////////////// ORDER:

    protected NotificationCompat.Builder baseNotif(Context mContext, boolean sound, boolean silenceButton) {
        Intent delete = new Intent(parameters.deleteIntentName);
        PendingIntent pIntentDelete = PendingIntent.getBroadcast(mContext, 1, delete, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotifCQTitle = NotiSingleTitle();
        mNotifCQContent = NotifSingleSummary();

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

    protected NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent,
                                                                 Context ctx) {

        NotifFeatures f = LogInManagement.notifFeatures;
        NotificationCompat.Builder notif = baseNotif(ctx, f.sound, f.refreshButton);

        mNotifTitle = NotiSingleTitle();
        mNotifBottom = Notifications.bottomMessage(ctx);

        //Bigtext style
        mNotifContent = NotiSingleContentExpanded();
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

    // Single compact
    protected String NotiSingleTitle() {
        return we.getName();
    }

    protected String NotifSingleSummary() {
        return oneLineSummary();
    }

    //Expanded
    protected String NotiSingleTitleExpanded() {
        return we.getName();
    }

    protected SpannableString NotiSingleContentExpanded() {
        return SpannableString.valueOf(we.getDescription());
    }

    /**
     * Includes the name: it is used whe you find several weacons [this in inbox). It's not necesary to overwrite
     *
     * @return
     */
    public final SpannableString inboxSummaryText() {
        int len = 16;
        String name;
        String greyPart = oneLineSummary();

        if (we.getName().length() > len) {
            name = we.getName().substring(0, len) + ".";
        } else {
            name = we.getName();
        }

        return StringUtils.getSpannableString(name + " " + greyPart, name.length());

    }

    public SpannableString textForListActivity() {
        return SpannableString.valueOf(we.getDescription());
    }
}
