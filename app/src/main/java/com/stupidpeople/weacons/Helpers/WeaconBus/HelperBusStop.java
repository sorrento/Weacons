package com.stupidpeople.weacons.Helpers.WeaconBus;

import android.content.Context;
import android.text.SpannableString;

import com.stupidpeople.weacons.Helpers.HelperBaseFecthNotif;
import com.stupidpeople.weacons.Helpers.WeaconBus.Barcelona.BusBarcelona;
import com.stupidpeople.weacons.Helpers.WeaconBus.Madrid.BusMadrid;
import com.stupidpeople.weacons.Helpers.WeaconBus.SantCugat.BusStCugat;
import com.stupidpeople.weacons.Helpers.WeaconBus.Santiago.BusLineSantiago;
import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Helpers.fetchableElement;
import com.stupidpeople.weacons.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

import util.myLog;

/**
 * Created by Milenko on 18/03/2016.
 */
public class HelperBusStop extends HelperBaseFecthNotif {

    private city mCity;

    public HelperBusStop(WeaconParse we, Context ctx) {
        super(we, ctx);
        this.mCity = determineCity(we);
    }

    @Override
    protected long fetchedDataIsValidDuringMinutes() {
        return -1;
    }

    private city determineCity(WeaconParse we) {
        //TODO esto debe venir de BBDD, no del nombre del logo....
        city res = null;
        String fileName = we.getLogoFileName();

        switch (fileName) {
            case "tfss-85aa0c47-517e-4312-9eed-884796b51ced-logo_bus.jpg":
                res = city.SANT_CUGAT;
                break;
            case "tfss-2fc23731-3306-4908-b049-3ffe0a3601a3-logoBCN2.jpg":
                res = city.BARCELONA;
                break;
            case "tfss-575fd5ef-9e37-4150-8a16-43fc6a1cc69b-logoMadrid2.jpg":
                res = city.MADRID;
                break;
            case "tfss-c074f51b-0696-436a-b8a5-1e0d4c0222b3-Logo Transantiago_red.jpg":
                res = city.SANTIAGO;
                break;
            default:
                myLog.add("NO se identifica la cirdad del bus:" + we.getName(), "WARN");
        }

        return res;
    }

    @Override
    protected ArrayList<fetchableElement> processResponse(Connection.Response response) {
        ArrayList<fetchableElement> arr = new ArrayList<>();
        String bo = response.body();

        switch (mCity) {
            case SANT_CUGAT:
                arr = processStCugat(bo);
                break;
            case BARCELONA:
                arr = processBarcelona(response);//it's html, not JSON
                break;
            case MADRID:
                arr = processMadrid(bo);
                break;
            case SANTIAGO:
                arr = processSantiago(bo);
                break;
            default:
                myLog.add("No podemos procesar respuesta porqeu no sé que ciudad es", "WARN");
        }
        return arr;
    }

    @Override
    protected String getFetchingFinalUrl() {
        String partialUrl;

        //TODO deberia ir en BBDD pero es ljaleo cambiarlo
        if (mCity == city.SANTIAGO) {
            partialUrl = "http://200.29.15.107/predictor/prediccion?codser=&codsimt=";
        } else {
            partialUrl = we.getFetchingPartialUrl();
        }
        return partialUrl + getBusStopId();
    }

    @Override
    protected SpannableString msgPressRefreshLong() {
        return SpannableString.valueOf(mContext.getString(R.string.notif_press_refresh_long));
    }

    @Override
    protected SpannableString msgPullToRefresh() {
        return SpannableString.valueOf(mContext.getString(R.string.ListActivity_pull_down_to_refresh));
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
    public String getNameWithCode() {
        return we.getParadaId() + " " + we.getName();
    }

    private String getBusStopId() {
        return we.getString("paradaId");
    }

    /**
     * Array with strings that summarizes each line: L1: 12 min, 18 min, 35 min
     * ideal for single notification (inbox format)
     *
     * @return
     */

    private ArrayList processStCugat(String response) {
        BusStopSituation bss = new BusStopSituation();

        try {
            JSONArray mJsonArray = new JSONArray(response);

            for (int i = 0; i < mJsonArray.length(); i++) {
                JSONObject json = mJsonArray.getJSONObject(i);
                bss.add(new BusStCugat(json));
            }
        } catch (Exception e) {
            myLog.error(e);
        }

        return bss.getBusLines();
    }

//    /**
//     * Shows only the first arrival by line:  L1:10m | B3: 5m | R4:18m
//     *
//     * @param compact for having L1:10|B3:5|R4:18
//     * @return
//     */
//    public String summarizeAllLines(boolean compact) {
//        String substring = mContext.getString(R.string.press_refresh);
//
//        int del = 0;
//
//        if (we.fetchedElements == null)
//            substring = mContext.getString(R.string.press_refresh);
//
//        else if (we.fetchedElements.size() == 0)
//            substring = mContext.getString(R.string.no_lines_available);
//
//        else if (we.fetchedElements.size() > 0) {
//            StringBuilder sb = new StringBuilder();
//
//            for (Object o : we.fetchedElements) {
//                BusLine line = (BusLine) o;
//                String name = line.lineCode;
//
//                if (compact) {
//                    sb.append(name + ":" + line.getShortestTime() + "|");
//                    del = 1;
//                } else {
//                    sb.append(name + ": " + line.getShortestTime() + "m | ");
//                    del = 2;
//                }
//            }
//            String s = sb.toString();
//            substring = s.substring(0, s.length() - del);
//        }
//
//        return substring;
//    }

//    public String summarizeAllLines() {
//        return summarizeAllLines(false);
//    }

//    private ArrayList<SpannableString> summarizeByOneLine() {
//        ArrayList<SpannableString> arr = new ArrayList<>();
//
//        if (we.refreshing) {
//            arr.add(new SpannableString(mContext.getString(R.string.refreshing)));
//        } else if (we.fetchedElements == null || we.fetchedElements.size() == 0) {
//            arr.add(new SpannableString(mContext.getString(R.string.notif_bus_NoInfo)));
//        } else {
//
//            for (Object o : we.fetchedElements) {
//                BusLine lt = (BusLine) o;
//                String name = lt.lineCode;
//
//                StringBuilder sb = new StringBuilder(name + " ");
//
//                for (Bus bus : lt.buses) sb.append(bus.arrivalTimeText + ", ");
//
//                String s = sb.toString();
//                String sub = s.substring(0, s.length() - 2);
//
//                arr.add(StringUtils.getSpannableString(sub, name.length()));
//
//            }
//        }
//        return arr;
//    }

    private ArrayList processMadrid(String response) {
        BusStopSituation bss = new BusStopSituation();

        try {
            JSONArray jLines = new JSONObject(response).getJSONArray("lines");

            for (int i = 0; i < jLines.length(); i++) {
                JSONObject jLine = jLines.getJSONObject(i);
                bss.add(new BusMadrid(jLine));
            }

        } catch (Exception e) {
            myLog.error(e);
        }

        return bss.getBusLines();
    }

    private ArrayList processSantiago(String response) {
        //Santiago strucuture is different becaus come gathered by line, not isolated buses
        ArrayList arr = new ArrayList();

        try {
            JSONObject json = new JSONObject(response);
            //not used:
//            stopCode = json.getString("paradero");
//            description = json.getString("nomett");
//            updateTime = json.getString("fechaprediccion") + "|" + json.getString("horaprediccion");
            String busstopMessage = json.getString("respuestaParadero");
            we.setDescription(busstopMessage);

            JSONArray jLines = json.getJSONObject("servicios").getJSONArray("item");
            myLog.add("tenemos algunso servicios de bus:" + jLines.length(), "aut");

            for (int i = 0; i < jLines.length(); i++) {
                JSONObject jLine = jLines.getJSONObject(i);
                if (!(jLine.getString("codigorespuesta").equals("00") || jLine.getString("codigorespuesta").equals("01")))
                    continue;

                arr.add(new BusLineSantiago(jLine));
            }

        } catch (JSONException e) {
            myLog.error(e);
        }
        return arr;
    }

    private ArrayList processBarcelona(Connection.Response response) {
        BusStopSituation bss = new BusStopSituation();

        try {
            Document doc = response.parse();
            Elements xmlBuses = doc.select("div[id=linia_amb");

            for (Element xmlBus : xmlBuses) {
                BusBarcelona bus = new BusBarcelona(xmlBus);
                bss.add(bus);
            }

        } catch (Exception e) {
            myLog.error(e);
        }
        return bss.getBusLines();
    }

    @Override
    protected String msgNoFetched() {
        return mContext.getString(R.string.notif_bus_NoInfo);
    }

    // Notifications


    enum city {SANT_CUGAT, BARCELONA, MADRID, SANTIAGO}

    // Reorganize the times for buses, gathering by line
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
            ArrayList arr = new ArrayList();
            for (BusLine line : tableLines.values()) {
                arr.add(line);
            }
            return arr;
        }
    }

}
