package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import org.jsoup.Connection;

import java.util.ArrayList;

/**
 * Created by Milenko on 18/03/2016.
 */
public abstract class HelperBaseFecthNotif extends HelperBase {
    protected String sInbox;


    protected HelperBaseFecthNotif(WeaconParse we, Context ctx) {
        super(we, ctx);
    }


    protected abstract ArrayList processResponse(Connection.Response response);

    protected abstract NotificationCompat.InboxStyle getInboxStyle();

    protected abstract String getFetchingFinalUrl();

    @Override
    protected boolean notificationRequiresFetching() {
        return true;
    }

    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext, boolean refreshButton, LogBump logBump) {
        NotificationCompat.Builder notif = baseNotif(mContext, sound, refreshButton);
        Notifications.addRefreshButton(notif);

        mNotifTitle = NotiSingleCompactTitle();

        if (we.obsolete) {
            //Bigtext style
            SpannableString msg = NotiSingleExpandedContent();
            NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle()
                    .setBigContentTitle(mNotifTitle)
                    .bigText(msg);
            if (LogInManagement.othersActive())
                textStyle.setSummaryText(Notifications.bottomMessage(mContext));
            notif.setStyle(textStyle);

            mBody = String.valueOf(msg);
        } else {
            //InboxStyle
            notif.setStyle(getInboxStyle());
            mBody = sInbox;
        }
        notif.setContentIntent(resultPendingIntent);

//        myLog.logNotification(title, body, Notifications.bottomMessage(mContext), sound, refreshButton, true);
        logNotification(logBump);
        return notif;

    }

}
