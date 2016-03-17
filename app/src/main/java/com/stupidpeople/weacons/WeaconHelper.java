package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import org.jsoup.Connection;

import java.util.ArrayList;

/**
 * Created by Milenko on 12/03/2016.
 */
public interface WeaconHelper {

    //Remember to add the case WeaconParse.build() for each new type of weacon

    //TODO maybe make an abstact class wiht the common constructor
    String typeString();

    boolean notificationRequiresFetching();

    ArrayList processResponse(Connection.Response response);

    String getFetchingUrl();

    String NotiSingleCompactTitle();

    String NotiSingleCompactContent();

    String NotiSingleExpandedTitle();

    String NotiSingleExpandedContent();

    SpannableString NotiOneLineSummary();

    SpannableString getOneLineSummary();

    Class getActivityClass();

    Intent getResultIntent(Context mContext);

    NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent,
                                                       boolean sound, Context mContext);

}
