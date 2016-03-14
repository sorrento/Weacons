package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import java.util.ArrayList;

/**
 * Created by Milenko on 12/03/2016.
 */
public interface WeaconHelper {

    String typeString();

    boolean notificationRequiresFetching();

    ArrayList processResponse(String response);

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
