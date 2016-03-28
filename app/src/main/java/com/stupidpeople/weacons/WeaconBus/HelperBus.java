package com.stupidpeople.weacons.WeaconBus;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.TextUtils;

import com.stupidpeople.weacons.HelperBaseFecthNotif;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.StringUtils;
import com.stupidpeople.weacons.WeaconBus.Madrid.BusMadrid;
import com.stupidpeople.weacons.WeaconBus.SantCugat.BusStCugat;
import com.stupidpeople.weacons.WeaconBus.Santiago.BusLineSantiago;
import com.stupidpeople.weacons.WeaconParse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.HashMap;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 18/03/2016.
 */
public class HelperBus extends HelperBaseFecthNotif {

    public HelperBus(WeaconParse we, Context ctx) {
        super(we, ctx);
    }

    @Override
    protected ArrayList processResponse(Connection.Response response) {
        ArrayList arr = new ArrayList();

        if (we.near(parameters.stCugat, 20)) arr = processStCugat(response.body());
        else if (we.near(parameters.santiago, 20)) arr = processSantiago(response.body());
        else if (we.near(parameters.madrid, 30)) arr = processMadrid(response.body());
        else if (we.near(parameters.barcelona, 20))
            arr = processBarcelona(response.body());//TODO separar por ciudad, usar quizas la url para difenreciar la ciudad
        return arr;
    }

    @Override
    protected String getFetchingFinalUrl() {
        return we.getFetchingPartialUrl() + getBusStopId();
    }

    @Override
    protected String typeString() {
        return mContext.getString(R.string.type_busstop);
    }

    @Override
    protected int getRepeatedOffRemoveFromNotification() {
        return 1;
    }

    @Override
    public String NotiSingleCompactContent() {
        String s;
        if (we.obsolete) {
            s = mContext.getString(R.string.press_refresh);
        } else {
            s = mContext.getString(R.string.bus_stop) + summarizeAllLines();
        }
        return s;
    }


    @Override
    public SpannableString NotiSingleExpandedContent() {
        SpannableString msg = new SpannableString("");
        try {

            if (we.obsolete) {
                msg = SpannableString.valueOf(mContext.getString(R.string.press_refresh_bus_long));
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
        String name;
        String greyPart;

        if (we.getName().length() > 10) {
            name = we.getName().substring(0, 10) + ".";
        } else {
            name = we.getName();
        }

        if (we.obsolete) {
            greyPart = we.getTypeString() + ". " + mContext.getString(R.string.press_refresh);
        } else {
            greyPart = summarizeAllLines();
        }
        return StringUtils.getSpannableString(name + " " + greyPart, name.length());
    }

    @Override
    protected NotificationCompat.InboxStyle getInboxStyle() {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(NotiSingleExpandedTitle());
        if (LogInManagement.othersActive())
            inboxStyle.setSummaryText(LogInManagement.bottomMessage(mContext));

        StringBuilder sb = new StringBuilder();
        for (SpannableString s : summarizeByOneLine()) {
            inboxStyle.addLine(s);
            sb.append("   " + s + "\n");
        }
        sInbox = sb.toString();
        return inboxStyle;
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
        String substring = mContext.getString(R.string.press_refresh);

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

    //Specific cities

    private ArrayList processStCugat(String response) {
        BusStopSituation situation = new BusStopSituation();

        try {
            JSONArray mJsonArray = new JSONArray(response);

            for (int i = 0; i < mJsonArray.length(); i++) {
                JSONObject json = mJsonArray.getJSONObject(i);
                situation.add(new BusStCugat(json));
            }
        } catch (Exception e) {
            myLog.error(e);
        }

        return situation.getBusLines();
    }

    private ArrayList processMadrid(String response) {
        BusStopSituation busStopSituation = new BusStopSituation();

        try {
            JSONArray jLines = new JSONObject(response).getJSONArray("lines");

            for (int i = 0; i < jLines.length(); i++) {
                JSONObject jLine = jLines.getJSONObject(i);
                busStopSituation.add(new BusMadrid(jLine));
            }

        } catch (Exception e) {
            myLog.error(e);
        }

        return busStopSituation.getBusLines();
    }

    private ArrayList processSantiago(String response) {
        //Santiago strucuture is different becaus come gathered by line, not isolated buses
        ArrayList<BusLine> arr = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(response);
            //not used:
//            stopCode = json.getString("paradero");
//            description = json.getString("nomett");
//            updateTime = json.getString("fechaprediccion") + "|" + json.getString("horaprediccion");

            JSONArray jLines = json.getJSONObject("servicios").getJSONArray("item");
            myLog.add("tenemos algunso servicios de bus:" + jLines.length(), "aut");

            for (int i = 0; i < jLines.length(); i++) {
                JSONObject jLine = jLines.getJSONObject(i);
                if (!(jLine.getString("codigorespuesta").equals("00") || jLine.getString("codigorespuesta").equals("01")))
                    continue;
                myLog.add("oneitem: " + jLine.toString(), "aut");

                arr.add(new BusLineSantiago(jLine));
            }

        } catch (JSONException e) {
            myLog.error(e);
        }
        return arr;
    }

    private ArrayList processBarcelona(String response) {
        return null;//TODO Barcelona bus
    }

    //** Reorganize the times for buses, gathering by line
    private class BusStopSituation {
        private HashMap<String, BusLine> tableLines = new HashMap<>();

        public BusStopSituation() {
            tableLines = new HashMap<>();
        }

        public void add(Bus bus) {
            String lineCode = bus.getLineCode();
            if (tableLines.containsKey(lineCode)) {
                BusLine busLine = tableLines.get(lineCode);
                busLine.addBus(bus);
            } else {
                tableLines.put(lineCode, new BusLine(bus));
            }
        }

        public ArrayList getBusLines() {
            ArrayList<BusLine> arr = new ArrayList<>();
            for (BusLine line : tableLines.values()) {
                arr.add(line);
            }
            return arr;
        }
    }
}
