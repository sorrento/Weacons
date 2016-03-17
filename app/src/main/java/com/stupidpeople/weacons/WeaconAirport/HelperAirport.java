package com.stupidpeople.weacons.WeaconAirport;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.WeaconHelper;
import com.stupidpeople.weacons.WeaconParse;

import org.jsoup.Connection;

import java.util.ArrayList;

import static com.stupidpeople.weacons.StringUtils.getSpannableString;

/**
 * Created by Milenko on 14/03/2016.
 */
public class HelperAirport implements WeaconHelper {
    private final WeaconParse we;

    //TODO airport meritates its own notification. Implementate that possibility
    public HelperAirport(WeaconParse weaconParse) {
        this.we = weaconParse;
    }

    @Override
    public String typeString() {
        return "AIRPORT";
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
    public String getFetchingUrl() {
        return null;
    }

    @Override
    public String NotiSingleCompactTitle() {
        return we.getName();
    }

    @Override
    public String NotiSingleCompactContent() {
        return we.getTypeString();
    }

    @Override
    public String NotiSingleExpandedTitle() {
        return we.getName();
    }

    @Override
    public String NotiSingleExpandedContent() {
        return we.getDescription();
    }

    //todo is this function redundant?
    @Override
    public SpannableString getOneLineSummary() {
        String name;

        if (we.getName().length() > 10) {
            name = we.getName().substring(0, 10) + ".";
        } else {
            name = we.getName();
        }

        return getSpannableString(name + " " + "Click to get fligths", name.length());

    }

    @Override
    public SpannableString NotiOneLineSummary() {
        return getOneLineSummary();
    }

    @Override
    public Class getActivityClass() {
        return null;
        //TODO put the cardactivity
    }

    @Override
    public Intent getResultIntent(Context mContext) {
        return null;
        //TODO complete this
    }

    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        return null;
        //TODO may be is better that WeraconHelper is a class with default implementations and abstract methods
    }
}