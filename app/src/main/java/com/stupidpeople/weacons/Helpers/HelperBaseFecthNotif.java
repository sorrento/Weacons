package com.stupidpeople.weacons.Helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.TextUtils;

import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.Notifications.NotifFeatures;
import com.stupidpeople.weacons.Notifications.Notifications;
import com.stupidpeople.weacons.R;

import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Milenko on 18/03/2016.
 */
public abstract class HelperBaseFecthNotif extends HelperBase {
    protected int maxElementsToShowInList = -1;
    Date lastUpdateTime;
    private String sInbox;

    protected HelperBaseFecthNotif(WeaconParse we, Context ctx) {
        super(we, ctx);
        we.setObsolete(true); //así muestra el mensaje de "press refresh" al aparecer por primera vez
    }

    protected abstract long fetchedDataIsValidDuringMinutes();

    /**
     * set if is relevant at som specific hour, like lunch
     *
     * @return
     */
    protected boolean doFetchingAEstaHora() {
        return true;
    }

    protected abstract ArrayList<fetchableElement> processResponse(Connection.Response response);

    private NotificationCompat.InboxStyle getInboxStyle() {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(NotiSingleTitleExpanded());

        if (LogInManagement.othersActive())
            inboxStyle.setSummaryText(Notifications.bottomMessage(mContext));

        StringBuilder sb = new StringBuilder();
        for (fetchableElement fe : we.fetchedElements) {
            inboxStyle.addLine(fe.oneLineSummary());
            sb.append("   ").append(fe.oneLineSummary()).append("\n");
        }

        sInbox = sb.toString();
        return inboxStyle;

    }

    protected abstract String getFetchingFinalUrl();

    @Override
    protected final boolean notificationRequiresFetching() {
        return true;
    }


    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, Context mContext) {
        //if no message, put the standard for the place
        if (we.emptyAnswerOrErrorFetching())
            return super.buildSingleNotification(resultPendingIntent, mContext);

        NotifFeatures f = LogInManagement.notifFeatures;
        NotificationCompat.Builder notif = baseNotif(mContext, f.sound, f.silenceButton);
        Notifications.addRefreshButton(notif);

        mNotifTitle = NotiSingleTitle();

        if (we.isObsolete() || we.refreshing || we.fetchedElements.size() == 0) {
            SpannableString msg = SpannableString.valueOf(oneLineSummary());

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

        return notif;
    }


    private String summarizeAllfetchedElementsInOneRow() {
        StringBuilder sb = new StringBuilder();

        for (fetchableElement fe : we.fetchedElements) sb.append(fe.veryShortSummary()).append("|");

        String s = sb.toString();
        return s.substring(0, s.length() - 1);
    }


    //  Notifications' content

    @Override
    protected final String oneLineSummary() {
        String s;

        if (we.refreshing) {
            s = msgRefreshing();
        } else if (we.isObsolete()) {
            s = msgPressRefresh();
        } else if (we.fetchedElements.size() != 0) {
            s = summarizeAllfetchedElementsInOneRow();
        } else if (we.emptyAnswerOrErrorFetching()) {
            s = msgNoFetched();
        } else {
            s = super.oneLineSummary();
        }
        return s;

    }

    @Override
    public final SpannableString textForListActivity() {
        SpannableString s;

        if (we.refreshing) {
            s = SpannableString.valueOf(msgRefreshing());
        } else if (we.isObsolete()) {
            s = msgPullToRefresh();
        } else if (we.fetchedElements.size() != 0) {
            s = allfetchedElementsForListActivity();
        } else if (we.emptyAnswerOrErrorFetching()) {
            s = SpannableString.valueOf(msgNoFetched());
        } else {
            s = super.textForListActivity();
        }
        return s;
    }

    private SpannableString allfetchedElementsForListActivity() {
        SpannableString buf = null;

        // In case there are too many, show a few
        List<fetchableElement> elements = maxElementsToShowInList > 0 && we.fetchedElements.size() >= maxElementsToShowInList ?
                we.fetchedElements.subList(0, maxElementsToShowInList) : we.fetchedElements;

        //Concatenate spannables
        for (fetchableElement fe : elements) {
            buf = buf == null ? fe.getLongSpan() :
                    SpannableString.valueOf(TextUtils.concat(buf, "\n", fe.getLongSpan()));
        }

        return buf;
    }

    protected abstract SpannableString msgPressRefreshLong();

    protected abstract SpannableString msgPullToRefresh();


    /**
     * Message to be shown when the fefhicng disappeared by obsolete
     *
     * @return
     */
    protected String msgPressRefresh() {
        return mContext.getString(R.string.notif_press_refresh);
    }

    /**
     * Message shown when refreshing
     *
     * @return
     */
    protected String msgRefreshing() {
        return mContext.getString(R.string.refreshing);
    }

    /**
     * Message shown when there is no fetched elements
     *
     * @return
     */
    protected String msgNoFetched() {
        return we.getDescription();
    }

}
