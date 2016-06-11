package com.stupidpeople.weacons;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.Location.GPSCoordinates;
import com.stupidpeople.weacons.Location.LocationAsker;
import com.stupidpeople.weacons.Location.LocationCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import util.myLog;
import util.srComparator;

/**
 * Created by Milenko on 07/06/2016.
 */
public class SAPO {
    static final String tag = "SAPO";
    private static final String parseSapoClass = "WifiSapo";
    private static final int repeticiones = 20; //cuantas veces se repite una wifi en scaneos, para ser subidas
    private static HashMap<String, Integer> bssidTable = new HashMap<>();
    private static boolean llegamosalos20 = false;


    public static void pinSpots(final List<ScanResult> sr, final Context ctx, final GPSCoordinates gps) {

        try {
            for (final ScanResult r : sr) {
                if (yaEsta(r)) incrementar(r);
                else agregarATabla(r);
            }


            if (llegamosalos20) {
                myLog.add("*****hemos llegado a " + repeticiones, tag);

                ArrayList<String> bssids = getBssids(sr);

                ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);
                q.whereContainedIn("bssid", bssids);
                q.whereEqualTo("user", ParseUser.getCurrentUser());

                q.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject po, ParseException e) {

                        if (e == null) {
                            myLog.add("Ya había uno de estos en internecs" + po.getString("ssid"), tag);

                            // desconectar sapo
                            if (po.getInt("counter") > 300) {
                                myLog.add("-----------Apagamos sapolio", tag);
                                SharedPreferences prefs = ctx.getSharedPreferences("com.stupidpeople.weacons", Context.MODE_PRIVATE);
                                prefs.edit().putBoolean("sapoActive", false).commit();
                            }

                            incrementar20ysubir(po, repeticiones);
                        } else {
                            myLog.add("No habia ninguno de estos  en internex, lo subirmos: " + e.getLocalizedMessage(), tag);
                            subirConGPS(maspower(sr), ctx);
                        }

                        bssidTable = new HashMap<>();
                    }
                });

            }

            llegamosalos20 = false;

        } catch (Exception e) {
            myLog.add("Error en loop sobre scanresults" + e.getLocalizedMessage(), tag);
        }

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

    private static void incrementar20ysubir(final ParseObject parseObject, int repeticiones) {
        parseObject.increment("counter", repeticiones);
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                myLog.add("subido el elemento incrmentado:" + parseObject.getString("ssid"), tag);
            }
        });
    }

    private static void subirConGPS(final ScanResult r, Context ctx) {

        new LocationAsker(ctx, new LocationCallback() {

            @Override
            public void NotPossibleToReachAccuracy() {
                myLog.add("ERROR, no se obtivo ls coordenadas con precitsion", tag);
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {
                ParseObject ws = new ParseObject(parseSapoClass);
                ws.put("ssid", r.SSID);
                ws.put("bssid", r.BSSID);
                ws.put("user", ParseUser.getCurrentUser());

                ws.put("counter", repeticiones);
                ws.put("GPS", new ParseGeoPoint(gps.getLatitude(), gps.getLongitude()));
                ws.put("radius", accuracy);

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
