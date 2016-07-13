package com.stupidpeople.weacons;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.Location.GPSCoordinates;
import com.stupidpeople.weacons.Location.LocationAsker;
import com.stupidpeople.weacons.Location.LocationCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import util.StringUtils;
import util.myLog;
import util.srComparator;

/**
 * Created by Milenko on 07/06/2016.
 */
public class SAPO {
    private static final String tag = "SAPO";
    private static final String parseSapoClass = "WifiSapo";
    private static final int repeticiones = 20; //cuantas veces se repite una wifi en scaneos, para ser subidas
    private static HashMap<String, Integer> bssidTable = new HashMap<>();
    private static boolean llegamosalos20 = false;


    public static void pinSpots(final List<ScanResult> sr, final Context ctx) {

        try {

            // Locally
            for (final ScanResult r : sr) {
                if (yaEsta(r)) incrementar(r);
                else agregarATabla(r);
            }


            if (llegamosalos20) {
                // reset
                llegamosalos20 = false;
                bssidTable = new HashMap<>();
                myLog.add("*****hemos llegado a " + repeticiones, tag);

                // 1. si está el ssid en internet, se aumenta
                CheckIfanyIsAlreadyInInternet(sr, ctx);

            }


        } catch (Exception e) {
            myLog.add("Error en loop sobre scanresults" + e.getLocalizedMessage(), tag);
        }

    }

    private static void CheckIfanyIsAlreadyInInternet(final List<ScanResult> sr, final Context ctx) {
        ArrayList<String> bssids = new ArrayList<>();

        for (ScanResult r : sr) bssids.add(r.BSSID);

        ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);
        q.whereContainedIn("bssid", bssids);
        q.whereEqualTo("user", ParseUser.getCurrentUser());
        q.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e == null) {
                    myLog.add("coincide BSSID con interntet, aumentamos", tag);
                    incrementar20ysubir(parseObject, repeticiones, ctx);

                } else {
                    myLog.addToParse("NO coincide BSSID con interntet, buscamos en 100 mts", tag);
                    getLocationAndIncrementOrCreate(sr, ctx);
                }
            }
        });
    }

    private static void getLocationAndIncrementOrCreate(final List<ScanResult> sr, final Context ctx) {
        new LocationAsker(ctx, new LocationCallback() {
            @Override
            public void NotPossibleToReachAccuracy() {
                myLog.add("ERROR, no se obtivo ls coordenadas con precitsion", tag);
            }

            @Override
            public void LocationReceived(final GPSCoordinates gps, final double accuracy) {

                //Check if there is already one in the area (150m ) online
                myLog.add("Buscando en internet entorno a (150mt): " + gps + " con precision de (mts): " + accuracy, tag);
                ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);
                q.whereWithinKilometers("GPS", gps.getGeoPoint(), 0.15);
                q.whereEqualTo("user", ParseUser.getCurrentUser());
                q.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject po, ParseException e) {

                        if (e == null) {
                            myLog.add("Ya había uno en internet en esta área: " + po.getString("ssid"), tag);

                            if (accuracy < po.getInt("radius")) {
                                po.put("GPS", gps.getGeoPoint());
                                po.put("radius", accuracy);
                            }

                            incrementar20ysubir(po, repeticiones, ctx);

                        } else {
                            final ScanResult maspower = maspower(sr);
                            myLog.add("No habia ninguno en el area (en internex), subiremos el mas powr:: " + maspower.SSID
                                    + "levl= " + maspower.level, tag);
                            subirConGPS(maspower, gps, accuracy, StringUtils.ListarSR(sr));
                        }

                    }
                });

            }
        });
    }

    private static void incrementar(ScanResult r) {
        final String s = r.BSSID;

        int n = bssidTable.get(s);
        if (n + 1 == repeticiones) llegamosalos20 = true;
        bssidTable.put(s, n + 1);

        myLog.add("+" + r.SSID + "->" + (n + 1), tag);
    }

    private static void agregarATabla(ScanResult r) {
        bssidTable.put(r.BSSID, 1);
        myLog.add("Agregado a tabla:" + r.SSID, tag);
    }

    private static ScanResult maspower(List<ScanResult> sr) {
        Collections.sort(sr, new srComparator());
        return sr.get(0);
    }

    private static void incrementar20ysubir(final ParseObject po, int repeticiones, Context ctx) {

        if (differentDay(po.getCreatedAt())) {
            po.increment("days");
            checkForSapoDisconnection(ctx);
        }

        po.increment("counter", repeticiones);
        po.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                myLog.add("subido el elemento incrmentado:" + po.getString("ssid"), tag);
            }
        });
    }

    /**
     * Disconnect sapo if at least 3 places have been seen on 5 different days
     *
     * @param ctx
     */
    private static void checkForSapoDisconnection(final Context ctx) {

        ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);
        q.whereEqualTo("user", ParseUser.getCurrentUser());
        q.whereGreaterThan("days", 5);
        q.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                if (e == null) {
                    if (i > 3) {
                        myLog.directLogParse("-----------Apagamos sapolio", tag);
                        SharedPreferences prefs = ctx.getSharedPreferences("com.stupidpeople.weacons", Context.MODE_PRIVATE);
                        prefs.edit().putBoolean("sapoActive", false).commit();

                    }
                }
            }
        });

    }

    private static boolean differentDay(Date createdAt) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String dayToday = sdf.format(new Date());
        String dayCreated = sdf.format(createdAt);
        myLog.addToParse("daycreated= " + createdAt + " daytoday= " + dayToday, "tag");
        return dayToday.equals(dayCreated);
    }

    private static void subirConGPS(final ScanResult r, GPSCoordinates gps, double accuracy, String lista) {

        ParseObject ws = new ParseObject(parseSapoClass);
        ws.put("ssid", r.SSID);
        ws.put("bssid", r.BSSID);
        ws.put("user", ParseUser.getCurrentUser());
        ws.put("counter", repeticiones);
        ws.put("GPS", gps.getGeoPoint());
        ws.put("lista", lista);
        ws.put("radius", accuracy);
        ws.put("days", 1);

        ws.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("EXITO, subido con GPS " + r.SSID, tag);
                } else {
                    myLog.add("Error al subir el frecuent con GPS" + e.getLocalizedMessage(), tag);
                }
            }
        });

    }

    private static ArrayList<String> getBssids(List<ScanResult> sr) {
        ArrayList<String> arr = new ArrayList<>();
        for (ScanResult r : sr) arr.add(r.BSSID);

        return arr;
    }

    private static boolean yaEsta(ScanResult r) {
        return bssidTable.containsKey(r.BSSID);
    }
}
