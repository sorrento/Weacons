package com.stupidpeople.weacons.WeaconBus;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.TextUtils;

import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.StringUtils;
import com.stupidpeople.weacons.WeaconBus.SantCugat.BusLineStCugat;
import com.stupidpeople.weacons.WeaconBus.SantCugat.BusStCugat;
import com.stupidpeople.weacons.WeaconBus.Santiago.BusLineSantiago;
import com.stupidpeople.weacons.WeaconHelper;
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WeaconRestaurant.CardsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.HashMap;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 14/03/2016.
 */
public class HelperBusOLD implements WeaconHelper {
    private final WeaconParse we;
    //TODO sort methods
    String updateTime;
    String stopCode;
    private String description;
    private String sInbox;

    public HelperBusOLD(WeaconParse weaconParse) {
        we = weaconParse;
    }


    @Override
    public String typeString() {
        return "BUS STOP";//TODO translate
    }

    @Override
    public boolean notificationRequiresFetching() {
        return true;
    }

    @Override
    public ArrayList processResponse(Connection.Response response) {

        ArrayList arr = new ArrayList();

        if (we.near(parameters.stCugat, 20)) {
            arr = processStCugat(response.body());
        } else if (we.near(parameters.santiago, 20)) {
            arr = processSantiago(response.body());
        }
        return arr;

    }

    @Override
    public String getFetchingFinalUrl() {
        return we.getFetchingPartialUrl() + getBusStopId();
    }

    @Override
    public String NotiSingleCompactTitle() {
        return we.getName();
    }

    @Override
    public String NotiSingleCompactContent() {
        String s;
        if (we.obsolete) {
            s = "Press REFRESH Button";
        } else {
            s = "BUS STOP. " + summarizeAllLines();
        }
        return s;
    }

    @Override
    public String NotiSingleExpandedTitle() {
        return we.getName();
    }

    @Override
    public SpannableString NotiSingleExpandedContent() {
        SpannableString msg = new SpannableString("");
        try {

            if (we.obsolete) {
                msg = SpannableString.valueOf("Please press REFRESH to have updated information about the estimated " +
                        "arrival times of buses at this stop.");
            } else {
                for (SpannableString s : summarizeByOneLine()) {
                    msg = SpannableString.valueOf(TextUtils.concat(msg, "\n", s));
                }
            }
        } catch (Exception e) {
            myLog.error(e);
        }
        return msg;
    }

    @Override
    public SpannableString NotiOneLineSummary() {
        return getOneLineSummary();
    }

    @Override
    public SpannableString getOneLineSummary() {
        String name;
        String greyPart;

        if (we.getName().length() > 10) {
            name = we.getName().substring(0, 10) + ".";
        } else {
            name = we.getName();
        }

        if (we.obsolete) {
            greyPart = we.getTypeString() + ". Press Refresh.";
        } else {
            greyPart = summarizeAllLines();
        }
        return StringUtils.getSpannableString(name + " " + greyPart, name.length());
    }

    @Override
    public Class getActivityClass() {
        return CardsActivity.class;
    }


    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        NotificationCompat.Builder notif;
        String title = NotiSingleCompactTitle();

        Intent refreshIntent = new Intent(parameters.refreshIntentName);
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);
        NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_volume_off_white_24dp, "Turn Off", resultPendingIntent);//TODO to create the silence intent
//TODO change icon to bell
        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notif_we)
                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(title)
                .setContentText(NotiSingleCompactContent())
                .setAutoCancel(true)
                .addAction(actionSilence)
                .addAction(actionRefresh);


        if (we.obsolete) {
            //Bigtext style

            SpannableString msg = NotiSingleExpandedContent();
            NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
            textStyle.setBigContentTitle(title);
            textStyle.bigText(msg);
            notif.setStyle(textStyle);

            myLog.notificationMultiple(title, String.valueOf(msg), "Currently " + LogInManagement.getActiveWeacons().size()
                    + " weacons active", String.valueOf(false));
        } else {
            //InboxStyle
            notif.setTicker("Weacon detected\n" + we.getName());
            if (sound) {
                notif.setLights(0xE6D820, 300, 100)
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
            }
            notif.setStyle(getInboxStyle());

            myLog.notificationMultiple(title, sInbox, "Currently " + LogInManagement.getActiveWeacons().size()
                    + " weacons active", String.valueOf(sound));
        }

        notif.setContentIntent(resultPendingIntent);//TODO whath to do when they click on in the notification

        return notif;

    }


    private ArrayList processSantiago(String response) {
        ArrayList<BusLine> arr = new ArrayList<>();

        try {


            JSONObject json = new JSONObject(response);
            stopCode = json.getString("paradero");
            description = json.getString("nomett");
            updateTime = json.getString("fechaprediccion") + "|" + json.getString("horaprediccion");

            JSONObject services = json.getJSONObject("servicios");
            JSONArray items = services.getJSONArray("item");
            myLog.add("tenemos algunso servicios de bus:" + items.length(), "aut");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                if (!(item.getString("codigorespuesta").equals("00") || item.getString("codigorespuesta").equals("01")))
                    continue;
                myLog.add("oneitem: " + item.toString(), "aut");
                BusLineSantiago line = new BusLineSantiago(item);
                arr.add(line);
            }

        } catch (JSONException e) {
            myLog.error(e);
        }
        return arr;
    }

    private ArrayList processStCugat(String response) {
        HashMap<String, BusLine> tableLines = new HashMap<>();

        try {
            JSONArray mJsonArray = new JSONArray(response);

            for (int i = 0; i < mJsonArray.length(); i++) {
                JSONObject json = mJsonArray.getJSONObject(i);
                BusStCugat bus = new BusStCugat(json);

                if (stopCode == null) { //put this info in the stop, only once
                    stopCode = bus.getStopCode();
                    updateTime = bus.getUpdateTime();
                }

                String lineCode = bus.getLineCode();
                if (tableLines.containsKey(lineCode)) {
                    BusLine busLine = tableLines.get(lineCode);
                    busLine.addBus(bus);
                } else {
                    tableLines.put(lineCode, new BusLineStCugat(lineCode, bus));
                }
            }
        } catch (Exception e) {
            myLog.error(e);
        }

        ArrayList<BusLine> arr = new ArrayList<>();
        for (BusLine line : tableLines.values()) {
            arr.add(line);
        }
        return arr;
    }

    private NotificationCompat.InboxStyle getInboxStyle() {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(NotiSingleExpandedTitle());
        inboxStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");


        StringBuilder sb = new StringBuilder();
        for (SpannableString s : summarizeByOneLine()) {
            inboxStyle.addLine(s);
            sb.append("   " + s + "\n");
        }
        sInbox = sb.toString();
        return inboxStyle;
    }

    /**
     * Array with strings that summarizes each line: L1: 12 min, 18 min, 35 min
     * ideal for single notification (inbox format)
     *
     * @return
     */
    public ArrayList<SpannableString> summarizeByOneLine() {
        ArrayList<SpannableString> arr = new ArrayList<>();

        if (we.fetchedElements == null || we.fetchedElements.size() == 0) {
            arr.add(new SpannableString("No info for this stop by now."));
        } else {

            for (Object o : we.fetchedElements) {
                BusLine lt = (BusLine) o;
                String name = lt.lineCode;

                StringBuilder sb = new StringBuilder(name + " ");

                for (Bus bus : lt.buses) {
                    sb.append(bus.arrivalTimeText + ", ");
                }

                String s = sb.toString();
                String sub = s.substring(0, s.length() - 2);

                arr.add(StringUtils.getSpannableString(sub, name.length()));

            }
        }
        return arr;
    }

    public String getBusStopId() {
        return we.getString("paradaId");
    }

    /**
     * Shows only the first arrival by line:  L1:10m | B3: 5m | R4:18m
     *
     * @param compact for having L1:10|B3:5|R4:18
     * @return
     */
    public String summarizeAllLines(boolean compact) {
        String substring = "No info Available";

        int del = 0;

        if (we.fetchedElements == null || we.fetchedElements.size() == 0)
            return "No lines available";

        if (we.fetchedElements.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (Object o : we.fetchedElements) {
                BusLine line = (BusLine) o;
                String name = line.lineCode;

                if (compact) {
                    sb.append(name + ":" + line.getShortestTime() + "|");
                    del = 1;
                } else {
                    sb.append(name + ": " + line.getShortestTime() + "m | ");
                    del = 2;
                }
            }
            String s = sb.toString();
            substring = s.substring(0, s.length() - del);
        }

        return substring;
    }

    public String summarizeAllLines() {
        return summarizeAllLines(false);
    }


}