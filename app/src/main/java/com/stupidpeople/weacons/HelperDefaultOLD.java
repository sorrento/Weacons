package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.WeaconRestaurant.CardsActivity;

import org.jsoup.Connection;

import java.util.ArrayList;

/**
 * Created by Milenko on 18/03/2016.
 */
public class HelperDefaultOLD implements WeaconHelper {
    private final WeaconParse we;

    public HelperDefaultOLD(WeaconParse weaconParse) {
        this.we = weaconParse;
    }

    @Override
    public String typeString() {
        return "Default";
    }

    @Override
    public boolean notificationRequiresFetching() {
        return false;
    }

    @Override
    public ArrayList processResponse(Connection.Response response) {
        return null;
    }

    @Override
    public String getFetchingFinalUrl() {
        return null;
    }

    @Override
    public String NotiSingleCompactTitle() {
        return we.getName();
    }

    @Override
    public String NotiSingleCompactContent() {
//        return we.getTypeString();//TODO cambiar poninenbdo el getTypeString
        return we.getDescription();
    }

    @Override
    public String NotiSingleExpandedTitle() {
        return we.getName();
    }

    @Override
    public SpannableString NotiSingleExpandedContent() {
        return SpannableString.valueOf(we.getDescription());
    }

    @Override
    public SpannableString NotiOneLineSummary() {
        return getOneLineSummary();
    }

    @Override
    public SpannableString getOneLineSummary() {
//        StringUtils.getSpannableString(we.getName()+". "+ we.getTypeString),we.getName().length());//TODO replace
        StringUtils.getSpannableString(we.getName() + ". " + we.getDescription(), we.getName().length());
        return null;
    }

    @Override
    public Class getActivityClass() {
        return CardsActivity.class;
    }

    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        return null;//TODO completar
    }
}
