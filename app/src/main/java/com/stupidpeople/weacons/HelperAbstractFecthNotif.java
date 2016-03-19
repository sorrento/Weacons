package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import org.jsoup.Connection;

import java.util.ArrayList;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 18/03/2016.
 */
public abstract class HelperAbstractFecthNotif extends HelperAbstract {
    protected String sInbox;

    public HelperAbstractFecthNotif(WeaconParse we) {
        super(we);
    }

    protected abstract ArrayList processResponse(Connection.Response response);

    protected abstract NotificationCompat.InboxStyle getInboxStyle();

    protected abstract String getFetchingFinalUrl();

    @Override
    protected boolean notificationRequiresFetching() {
        return true;
    }

    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        NotificationCompat.Builder notif = baseNotif(mContext, sound, true);
        String title = NotiSingleCompactTitle();

        //Refresh Button
        Intent refreshIntent = new Intent(parameters.refreshIntentName);
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);

        notif.addAction(actionRefresh);


        if (we.obsolete) {
            //Bigtext style
            SpannableString msg = NotiSingleExpandedContent();
            NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(msg);

            notif.setStyle(textStyle);

            myLog.notificationMultiple(title, String.valueOf(msg), "Currently " + LogInManagement.getActiveWeacons().size()
                    + " weacons active", String.valueOf(false));
        } else {
            //InboxStyle
            notif.setStyle(getInboxStyle());

            myLog.notificationMultiple(title, sInbox, "Currently " + LogInManagement.getActiveWeacons().size()
                    + " weacons active", String.valueOf(sound));
        }

        notif.setContentIntent(resultPendingIntent);

        return notif;

    }


}
