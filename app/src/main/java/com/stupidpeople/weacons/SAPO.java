package com.stupidpeople.weacons;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 07/06/2016.
 */
public class SAPO {
    static final String tag = "SAPO";
    private static final String pinSapo = parameters.pinSapo;
    private static final String parseSapoClass = "WifiSapo";
    private static final long Week = 1 * 24 * 60 * 60 * 1000;//7 * 24 * 60 * 60 * 1000 todo , puse un dia para tenstibg

    public static void pinSpots(final List<ScanResult> sr, final Context ctx, final GPSCoordinates gps) {
        try {
            myLog.add("entering in pinSpots", tag);

            for (final ScanResult r : sr) {

                ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);
                q.whereEqualTo("bssid", r.BSSID);
                q.fromPin(pinSapo);
                q.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject wifiSpot, ParseException e) {

                        if (e == null) {

                            if (gps != null) {
                                wifiSpot.put("GPS", new ParseGeoPoint(gps.getLatitude(), gps.getLongitude()));
                            }

                            incrementSapoSpot(wifiSpot);


                            int counter = wifiSpot.getInt("counter");
                            ParseGeoPoint gps = wifiSpot.getParseGeoPoint("GPS");
                            String name = wifiSpot.getString("ssid");

                            myLog.add("\t--- " + counter + "|" + gps + "|" + name, tag);

                            if (counter == 100 && gps == null) {
                                myLog.add("*****BINGO ono tiene 100", tag);
                                pinSpotsPuttingGPS(ctx, sr);
                                return;
                            }

                        } else {
                            //no existe el objeto
                            myLog.add("error al traer wifisapo:" + e.getLocalizedMessage(), tag);
                            createWifiSpotSapo(r, gps);
                        }
                    }
                });
            }
        } catch (Exception e) {
            myLog.error(e);
        }
    }

    private static void pinSpotsPuttingGPS(final Context ctx, final List<ScanResult> sr) {
        new LocationAsker(ctx, new LocationCallback() {
            @Override
            public void LocationReceived(GPSCoordinates gps) {
                myLog.add("received le coordineates:" + gps, tag);
                pinSpots(sr, ctx, gps);
            }

            @Override
            public void NotPossibleToReachAccuracy() {
                myLog.add("Not possible to get GPS", tag);
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {

            }
        });
    }

    private static void createWifiSpotSapo(ScanResult r, GPSCoordinates gps) {
        ParseObject ws = new ParseObject(parseSapoClass);
        ws.put("ssid", r.SSID);
        ws.put("bssid", r.BSSID);
        ws.put("user", ParseUser.getCurrentUser());
        ws.put("counter", 1);
        if (gps != null) {
            ws.put("GPS", new ParseGeoPoint(gps.getLatitude(), gps.getLongitude()));
        }
        ws.pinInBackground(pinSapo, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("pinned un wifi" + e.getLocalizedMessage(), tag);

                } else {
                    myLog.add("error pinning new sapo" + e.getLocalizedMessage(), tag);
                }
            }
        });
    }

    private static void incrementSapoSpot(ParseObject wifiSpot) {
        myLog.add("we have one spot matching, gonna increment", tag);
        wifiSpot.increment("counter");

        wifiSpot.pinInBackground(pinSapo);
    }

    public static void onWifiUploadSapo() throws ParseException {
        myLog.add("vamos a subir a inernets", tag);
        if (timeSapeando() > Week) {
            // 1. elegimos los de local que tienen el GPS, pero si ya está online, no subimos de nuevo
            ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);
            q.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    ArrayList<String> objs = new ArrayList<>();
                    for (ParseObject o : list) objs.add(o.getObjectId());

                    ParseQuery<ParseObject> q2 = ParseQuery.getQuery(parseSapoClass);

                    ParseQuery<ParseObject> locales = q2.fromPin(pinSapo)
                            .whereExists("GPS")
                            .whereNotContainedIn("objectId", objs);

                    //borramos del local todos
                    try {
                        ParseObject.unpinAll(pinSapo);
                    } catch (ParseException e1) {
                        myLog.add("Error unpinning all" + e.getLocalizedMessage(), tag);
                    }

                    downloadAndPinSapos();

                }
            });


        } else {
            myLog.add("No ha pasado una semanita aun", tag);
        }
    }

    /**
     * bajamos a local los de internet que tienen GPS, dl propio usuairo
     */
    private static void downloadAndPinSapos() {
        ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);
        q.whereEqualTo("user", ParseUser.getCurrentUser())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        ParseObject.pinAllInBackground(pinSapo, list, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    myLog.add("FINAL, bajads a local todos los sapos de inteners, del propio usuario", tag);
                                }
                            }
                        });
                    }
                });
    }

    private static long timeSapeando() throws ParseException {
        ParseQuery<ParseObject> q = ParseQuery.getQuery(parseSapoClass);

        ParseObject first = q.fromPin(pinSapo)
                .orderByAscending("createdAt")
                .getFirst();
        Date dateIni = first.getCreatedAt();

        first = q.fromPin(pinSapo)
                .orderByDescending("updatedAt")
                .getFirst();
        Date dateEnd = first.getCreatedAt();

        myLog.add("old date=" + dateIni + "| new date" + dateEnd, tag);

        return dateEnd.getTime() - dateIni.getTime();
    }
}
