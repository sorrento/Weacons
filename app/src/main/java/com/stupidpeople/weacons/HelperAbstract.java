package com.stupidpeople.weacons;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.WeaconRestaurant.CardsActivity;

import util.myLog;

/**
 * Created by Milenko on 18/03/2016.
 */
public abstract class HelperAbstract {
    protected WeaconParse we;

    protected HelperAbstract(WeaconParse we) {
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
        return CardsActivity.class;
    }

    protected NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        String title = NotiSingleCompactTitle();
        NotificationCompat.Builder notif = baseNotif(mContext, sound, true);


        //Bigtext style
        SpannableString msg = NotiSingleExpandedContent();
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle()
                .setBigContentTitle(title)
                .bigText(msg);

        notif.setStyle(textStyle);

        myLog.notificationMultiple(title, String.valueOf(msg), "Currently " + LogInManagement.getActiveWeacons().size()
                + " weacons active", String.valueOf(false));

        notif.setContentIntent(resultPendingIntent);

        return notif;
    }

    protected NotificationCompat.Builder baseNotif(Context mContext, boolean sound, boolean silenceButton) {

        NotificationCompat.Builder notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notif_we)
                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(NotiSingleCompactTitle())
                .setContentText(NotiSingleCompactContent())
                .setAutoCancel(true)
                .setTicker("Weacon detected\n" + we.getName());
        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }

        if (silenceButton) {
            Intent resultIntent = new Intent(mContext, CardsActivity.class);//TODO  create the intent for Silence
            PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_volume_off_white_24dp, "Turn Off", resultPendingIntent);//TODO to create the silence intent
            notif.addAction(actionSilence);
        }

        return notif;
    }

    protected boolean notificationRequiresFetching() {
        return false;
    }

}
