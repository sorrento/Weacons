package com.stupidpeople.weacons.BusWeacon;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.stupidpeople.weacons.BusWeacon.SantCugat.BusLineStCugat;
import com.stupidpeople.weacons.BusWeacon.SantCugat.BusStCugat;
import com.stupidpeople.weacons.BusWeacon.Santiago.BusLineSantiago;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.WeaconParse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 10/03/2016.
 */
public class WeaconBusStop extends WeaconParse {
    String updateTime;
    String stopCode;
    private String description;
    private String sInbox;


    @Override
    protected ArrayList processResponse(String response) {
        ArrayList arr = new ArrayList();

        if (near(parameters.stCugat, 20)) {
            arr = processStCugat(response);
        } else if (near(parameters.santiago, 20)) {
            arr = processSantiago(response);
        }
        return arr;
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


    @Override
    protected boolean notificationRequiresFetching() {
        return true;
    }


    @Override
    protected String getFetchingUrl() {
        return getString("FetchingUrl") + getBusStopId();
    }

    /*        Notifications         */

    @Override
    protected String NotiSingleCompactTitle() {
        return getName();
    }

    @Override
    protected String NotiSingleCompactContent() {
        return "BUS STOP. " + summarizeAllLines();
    }

    @Override
    protected String NotiSingleExpandedTitle() {
        return getName();
    }

    @Override
    protected String NotiSingleExpandedContent() {
        //TODO
        return null;
    }

    private NotificationCompat.InboxStyle getInboxStyle() {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getName());
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

        if (fetchedElements == null || fetchedElements.size() == 0) {
            arr.add(new SpannableString("No info for this stop by now."));
        } else {

            for (Object o : fetchedElements) {
                BusLine lt = (BusLine) o;
                String name = lt.lineCode;

                StringBuilder sb = new StringBuilder(name + " ");

                for (Bus bus : lt.buses) {
                    sb.append(bus.arrivalTimeText + ", ");
                }

                String s = sb.toString();
                String sub = s.substring(0, s.length() - 2);

                //add format
                SpannableString span = new SpannableString(sub);
                span.setSpan(new ForegroundColorSpan(Color.BLACK), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new RelativeSizeSpan(1.1f), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                arr.add(span);

            }
        }
        return arr;
    }

    @Override
    protected String NotiOneLineSummary() {
        //TODO
        return null;
    }

    @Override
    protected NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        NotificationCompat.Builder notif;

        Intent refreshIntent = new Intent("popo"); //TODO poner el reciever de esto, para que refresque la notif
        PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);
        NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_volume_off_white_24dp, "Turn Off", resultPendingIntent);//TODO to create the silence intent


        String title = getName();

        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notif_we)
                .setLargeIcon(getLogoRounded())
                .setContentTitle(title)
                .setContentText(NotiSingleCompactContent())
                .setAutoCancel(true)
                .setTicker("Weacon detected\n" + getName())
                .addAction(actionSilence)
                .addAction(actionRefresh);

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }


        notif.setStyle(getInboxStyle());

        myLog.notificationMultiple(title, sInbox, "Currently " + LogInManagement.getActiveWeacons().size() + " weacons active", String.valueOf(sound));

        notif.setContentIntent(resultPendingIntent);//TODO whath to do when they click on i

        return notif;
    }


    public String getBusStopId() {
        return getString("paradaId");
    }

    /**
     * Shows only the first arrival by line:  L1:10m | B3: 5m | R4:18m
     *
     * @param compact for having L1:10|B3:5|R4:18
     * @return
     */
    public String summarizeAllLines(boolean compact) {
        String substring = "No info";

        int del = 0;

        if (fetchedElements == null) return "No lines available";

        if (fetchedElements.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (Object o : fetchedElements) {
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
