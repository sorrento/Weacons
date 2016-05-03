package com.stupidpeople.weacons.ready;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.GPSCoordinates;
import com.stupidpeople.weacons.LocationAsker;
import com.stupidpeople.weacons.LocationCallback;
import com.stupidpeople.weacons.LogBump;
import com.stupidpeople.weacons.Notifications;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WifiSpot;
import com.stupidpeople.weacons.booleanCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 03/03/2016.
 */
public abstract class ParseActions {

    private static String tag = "PAR";

    /**
     * Verifies if any of these ssids or bssids is in Parse (local) and log in the user
     *
     * @param logBump
     * @param callBackWeacons for returning the set of weacons in the zone in this scanning
     * @param ctx
     */
    public static void CheckSpotMatches(List<ScanResult> sr, final LogBump logBump, final CallBackWeacons callBackWeacons, final Context ctx) {

        ArrayList<String> bssids = new ArrayList<>();

        for (ScanResult r : sr) {
            bssids.add(r.BSSID);
        }

        //Query BSSID
        ParseQuery<WifiSpot> qb = ParseQuery.getQuery(WifiSpot.class);
        qb.whereContainedIn("bssid", bssids)
                .fromPin(parameters.pinWeacons)
                .include("associated_place");

        qb.findInBackground(new FindCallback<WifiSpot>() {

            @Override
            public void done(List<WifiSpot> spots, ParseException e) {
                if (e == null) {
//                    HashSet<WeaconParse> weaconHash = new HashSet<>();
                    HashMap<WeaconParse, ArrayList<String>> weaconHash = new HashMap<WeaconParse, ArrayList<String>>();

                    if (spots.size() == 0) {
                        logBump.build();

                    } else { //There are matches

                        for (WifiSpot spot : spots) {
                            WeaconParse we = spot.getWeacon();
                            ArrayList<String> arr = new ArrayList<>();
                            if (weaconHash.containsKey(we)) arr = weaconHash.get(we);
                            arr.add(spot.getSSID());
                            weaconHash.put(we, arr);

//                            registerHitSSID(spot); todo
                        }
                        logBump.setWifiSpots(spots);

                        // It's important always deliver built weacons (in this way, they are of subclasses, as bus
                        WeaconParse.build(weaconHash, ctx);

//                        myLog.add(sb.toString(), tag);
//                        myLog.add("Detected spots: " + spots.size() + " | Different weacons: " + weaconHash.size(), tag);
                    }

                    callBackWeacons.OnReceive(weaconHash);

                } else {
                    myLog.add("EEE en Chechkspotmarches:" + e.getLocalizedMessage(), tag);
                }
            }
        });
    }


    /***
     * get wifispots from parse in a area and pin them the object includes the weacon
     *
     * @param bLocal  if should be queried in local database
     * @param radio   kms
     * @param center  center of queried area
     * @param context
     */

    public static void getSpots(final boolean bLocal, final double radio, final GPSCoordinates center, final Context context) {
        try {
            //1.Remove spots and weacons in local
            myLog.add("retrieving SSIDS from local:" + bLocal + " user: " + ParseUser.getCurrentUser(), tag);
            ParseObject.unpinAllInBackground(parameters.pinWeacons, new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {

                        //2. Load them
                        ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
                        query.whereWithinKilometers("GPS", new ParseGeoPoint(center.getLatitude(), center.getLongitude()), radio);
                        query.include("associated_place");
                        query.setLimit(900);

                        if (bLocal) query.fromLocalDatastore();
                        query.findInBackground(new FindCallback<WifiSpot>() {
                            @Override
                            public void done(List<WifiSpot> spots, ParseException e) {
                                if (e == null) {

                                    //3. Pin them
                                    myLog.add("number of SSIDS Loaded for weacons:" + spots.size(), tag);
                                    if (!bLocal)
                                        ParseObject.pinAllInBackground(parameters.pinWeacons, spots, new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    myLog.add("Wecaons pinned ok", "aut");
                                                    Toast.makeText(context, "Weacons Loaded", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    myLog.add("---Error retrieving Weacons from web: " + e.getMessage(), tag);
                                                }
                                            }
                                        });
                                } else {
                                    myLog.add("---ERROR from parse obtienning ssids" + e.getMessage(), tag);
                                }
                            }
                        });
                    } else {
                        myLog.error(e);
                    }
                }
            });
        } catch (Exception e) {
            myLog.add("---Error: failed retrieving SPOTS: " + e.getMessage(), tag);
        }
    }

    /**
     * get the bus stops around and pin them
     */
    public static void getSpotsForBusStops(Context ctx) {
        LocationCallback listener = new LocationCallback() {
            @Override
            public void LocationReceived(GPSCoordinates gps) {
                ParseGeoPoint pos = gps.getGeoPoint();
                getSpotsForBusStops(pos, null);
            }

            @Override
            public void NotPossibleToReachAccuracy() {

            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {

            }
        };
        new LocationAsker(ctx, listener);
    }

    /**
     * The 300 nearest weacons bus and their spots. Ten pin them in local
     *
     * @param pos
     */
    public static void getSpotsForBusStops(ParseGeoPoint pos, final MultiTaskCompleted mtc) {

        //Query Weacons
        ParseQuery<WeaconParse> queryWe = ParseQuery.getQuery(WeaconParse.class);
        queryWe.whereNear("GPS", pos)
                .setLimit(300)
                .whereEqualTo("Type", "bus_stop");
        myLog.add("---For bringing spots, around" + pos, "OJO");
        //Query WifiSpots
        ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
        query.whereMatchesQuery("associated_place", queryWe)
                .setLimit(1000)
                .include("associated_place")
                .findInBackground(new FindCallback<WifiSpot>() {
                    @Override
                    public void done(List<WifiSpot> list, ParseException e) {
                        if (e == null) {
                            myLog.add("****el numero de wifispots recogideos es: " + list.size(), "aut");
                            try {
//                                ParseObject.unpinAll(parameters.pinWeacons); TODO is it necesary to unpin?
                                myLog.add("---Estamos pinneando:\n" + Listar(list), "OJO");
                                ParseObject.pinAll(parameters.pinWeacons, list);
                            } catch (ParseException e1) {
                                myLog.error(e);
                            }
                            if (mtc != null) mtc.OneTaskCompleted();
                        } else {
                            myLog.error(e);
                        }
                    }
                });
    }

    private static String Listar(List<WifiSpot> list) {
        StringBuilder sb = new StringBuilder();
        for (WifiSpot ws : list) {
            sb.append("(" + ws.getSSID() + ", " + ws.getObjectId() + ") |");
        }
        return sb.toString();
    }

    /**
     * Mark in local parse that these weacons are interesting, so they will sound and will be fetched
     * automatically (first time)
     *
     * @param notifiedWeacons
     */
    public static void AddToInteresting(ArrayList<WeaconParse> notifiedWeacons) {

        for (final WeaconParse we : notifiedWeacons) {
            ParseObject fav = new ParseObject("Favorites");
            fav.put("WeaconId", we.getObjectId());
            fav.pinInBackground(parameters.pinFavorites, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        myLog.add("se ha pinneado el favorito  " + we.getName(), tag);
                    } else {
                        myLog.add("No se ha pinneaso el favorito " + we.getName() + e.getLocalizedMessage(), tag);
                    }
                }
            });
        }
    }

    public static void AddToInteresting(WeaconParse we) {
        ArrayList<WeaconParse> arr = new ArrayList<WeaconParse>();
        arr.add(we);
        AddToInteresting(arr);
    }

    public static boolean isInteresting(String objectId) {
        int i = 0;
        try {
//            myLog.add("Checkado si " + objectId + "es interesante", "aut");
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
            query.whereEqualTo("WeaconId", objectId);
            query.fromPin(parameters.pinFavorites);
            i = query.count();
//            myLog.add("     La cuenta ha dado " + i, "aut");
        } catch (ParseException e) {
            myLog.error(e);
        }
        boolean b = i > 0;
        myLog.add("is interesting?" + b, tag);
        return b;
    }

    public static void removeInteresting(ArrayList<WeaconParse> notifiedWeacons) {
        ArrayList arr = new ArrayList();

        //To remove the Silence button:
//        LogInManagement.NotifyFetching(false, false);//interesting=true for starting the timer 30segs
        Notifications.RemoveSilenceButton2();

        for (WeaconParse we : notifiedWeacons) {
            arr.add(we.getObjectId());
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
        query.whereContainedIn("WeaconId", arr)
                .fromPin(parameters.pinFavorites)
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null) {
                            myLog.add("Recibidos favoritos para borrar:" + list.size(), "aut");
                            ParseObject.unpinAllInBackground(parameters.pinFavorites, list, new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        myLog.add("Borrads los elementso de favoritos", "aut");
                                    } else {
                                        myLog.add("No se han borrado los elementos de favo", "aut");
                                    }
                                }
                            });
                        } else {
                            myLog.error(e);
                        }
                    }
                });

    }

    public static void getClosestBusStop(GPSCoordinates gps, GetCallback<WeaconParse> oneParadaCallback) {
        ParseQuery<WeaconParse> query = ParseQuery.getQuery(WeaconParse.class);
        query.whereNear("GPS", gps.getGeoPoint())
                .whereEqualTo("Type", "bus_stop")
                .getFirstInBackground(oneParadaCallback);
    }

    /**
     * create in parse the SSID not already created, an assign the weacon of the bustop.
     * Also register the intensities
     *
     * @param weBusStop
     * @param sr
     * @param ctx
     */
    public static void assignSpotsToWeacon(final WeaconParse weBusStop, final List<ScanResult> sr,
                                           final GPSCoordinates gps, final Context ctx, final MultiTaskCompleted taskCompleted) {
        final ArrayList<String> macs = new ArrayList<>();
        for (ScanResult r : sr) {
            macs.add(r.BSSID);
        }

        // Create only the new ones
        ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
        query.whereContainedIn("bssid", macs);
        query.findInBackground(new FindCallback<WifiSpot>() {
            @Override
            public void done(List<WifiSpot> list, ParseException e) {
                List<String> spotsAlreadyCreated = new ArrayList<>();
                final ArrayList<WifiSpot> newOnes = new ArrayList<>();

                if (e == null) {
                    myLog.add("Detected: " + sr.size() + " alread created:y " + list.size(), tag);
                    for (WifiSpot ws : list) {
                        spotsAlreadyCreated.add(ws.getBSSID());
                    }

                    for (ScanResult r : sr) {
                        if (!spotsAlreadyCreated.contains(r.BSSID)) {
                            newOnes.add(new WifiSpot(r, weBusStop, gps));
                        }
                    }

                    //Upload batch
                    WifiSpot.saveAllInBackground(newOnes, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                myLog.add("subidos varios wifispots: " + WifiSpot.Listar(newOnes), tag);
                                String text = ctx.getString(R.string.busstop_uploaded) + weBusStop.getName();
                                myLog.add(text, "DBG");
                                Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();

                                SaveIntensities(sr, weBusStop);
                                taskCompleted.OneTaskCompleted();
                            } else {
                                myLog.error(e);
                            }
                        }
                    });

                } else {
                    myLog.error(e);
                }
            }
        });
    }


    private static void SaveIntensities(List<ScanResult> sr, final WeaconParse weBusStop) {
        final ArrayList<ParseObject> intensities = new ArrayList<ParseObject>();
        for (ScanResult r : sr) {
            ParseObject intensity = ParseObject.create("Intensities");
            intensity.put("level", r.level);
            intensity.put("weMeasured", weBusStop);
            intensity.put("ssid", r.SSID);
            intensity.put("bssid", r.BSSID);
            intensities.add(intensity);
        }
        ParseObject.saveAllInBackground(intensities, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("saved several intensities " + intensities.size(), tag);
                    // aumentar el n_scannings del weacon  en uno
                    weBusStop.increment("n_scannings");
                    weBusStop.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                myLog.add("incrementado el we de parada en uno", tag);
                            } else
                                myLog.error(e);
                        }
                    });
                } else {
                    myLog.error(e);
                }

            }
        });
    }

    /**
     * Check if there is some newer weacons in the area (respect to the last pinning in local) and if the nearest weacon
     * is the same as the one on local (to check if we are in new area)
     */
    public static void DownloadWeaconsIfNeded(Context ctx) {
        myLog.add("VEamos si se necesita bajar weacons:", "OJO");
        //AskLocation
        new LocationAsker(ctx, new LocationCallback() {
            @Override
            public void LocationReceived(final GPSCoordinates gps) {
                final ParseGeoPoint point = gps.getGeoPoint();
                final booleanCallback newInAreaCB = new booleanCallback() {
                    @Override
                    public void OnResult(boolean b) {
                        if (b) {
                            //actualizamos
                            myLog.add("***There are new SPOTS in the zone , gonna update", "OJO");
                            getSpotsForBusStops(point, null);
                        } else {
                            myLog.add("***Nada que actualizar, en web lo mismo que en local (1km)", "OJO");
                        }
                    }
                };
                booleanCallback anyUpdatedCallback = new booleanCallback() {
                    @Override
                    public void OnResult(boolean b) {
                        if (b) {
                            //actualizamos
                            myLog.add("***There are newer in the zone (updatedAt), gonna update", "OJO");
                            getSpotsForBusStops(point, null);
                        } else {
                            //Check if we moved
                            anyNewInArea(point, newInAreaCB);
                        }
                    }
                };

                anyUpdated(point, anyUpdatedCallback);
            }

            @Override
            public void NotPossibleToReachAccuracy() {

            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {

            }

        });
    }

    /**
     * Check if the number of wifis is the same in local wrt web in 1km
     *
     * @param point
     */
    private static void anyNewInArea(ParseGeoPoint point, final booleanCallback bcb) {
        try {
            // Local
            ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
            query.whereWithinKilometers("GPS", point, 1)
                    .fromPin(parameters.pinWeacons);
            final int nLocal = query.count();

            // en web
            ParseQuery<WifiSpot> queryW = ParseQuery.getQuery(WifiSpot.class);
            queryW.whereWithinKilometers("GPS", point, 1)
                    .countInBackground(new CountCallback() {
                        @Override
                        public void done(int i, ParseException e) {
                            myLog.add("[SPOTs]En 1km hay:" + nLocal + "(local) y " + i + "(web)", "aut");
                            bcb.OnResult(nLocal != i);
                        }
                    });
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Compares the latest update time in local wrt web (of spots in the area of 1km
     *
     * @param point
     * @param bcb
     */
    private static void anyUpdated(ParseGeoPoint point, final booleanCallback bcb) {
        try {
            // Local
            ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
            query.whereWithinKilometers("GPS", point, 1)
                    .fromPin(parameters.pinWeacons)
                    .orderByDescending("updatedAt");
            final WifiSpot wiLocal = query.getFirst();

            // Web
            //Query Weacons first
            ParseQuery<WeaconParse> queryWe = ParseQuery.getQuery(WeaconParse.class);
            queryWe.whereNear("GPS", point)
                    .setLimit(300)
                    .whereEqualTo("Type", "bus_stop");
            myLog.add("----For comparation, quering aaround         " + point, "OJO");
            ParseQuery<WifiSpot> queryW = ParseQuery.getQuery(WifiSpot.class);
            queryW.whereWithinKilometers("GPS", point, 1)
                    .whereMatchesQuery("associated_place", queryWe)
                    .orderByDescending("updatedAt")
                    .getFirstInBackground(new GetCallback<WifiSpot>() {
                        @Override
                        public void done(WifiSpot wifiSpot, ParseException e) {
                            if (e != null) {
                                myLog.add("tenemos un error al recibir los de la web en la zona:" + e, "OJO");
                            } else {
                                if (wifiSpot == null) {
                                    myLog.add("wifispoit de la web es null", "OJO");

                                } else {
                                    Date dateWeb = wifiSpot.getUpdatedAt();
                                    myLog.add("DateWeb=" + dateWeb + " DateLocal=" + wiLocal.getUpdatedAt(), "OJO");
                                    myLog.add("obWeb=" + wifiSpot.getObjectId() + " OBLocal=" + wiLocal.getObjectId(), "OJO");
                                    myLog.add("obWeb=" + wifiSpot + " OBLocal=" + wiLocal, "OJO");
                                    bcb.OnResult(dateWeb.after(wiLocal.getUpdatedAt()));
                                }
                            }

                        }
                    });
        } catch (ParseException e) {
            myLog.error(e);
        }
    }

    public static void setHome(final WeaconParse we) {
        ParseObject home = new ParseObject("Home");
        home.put("WeaconId", we.getObjectId());
        home.pinInBackground(parameters.pinFavorites, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("se ha pinneado el home" + we.getName(), tag);
                } else {
                    myLog.add("No se ha pinneaso el home " + we.getName() + e.getLocalizedMessage(), tag);
                }
            }
        });
    }

    public static boolean IsHome(WeaconParse we) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
        try {
            List<ParseObject> found = query.whereEqualTo("WeaconId", we.getObjectId())
                    .fromPin(parameters.pinFavorites)
                    .find();
            if (found != null && found.size() > 0) return true;
        } catch (ParseException e) {
            myLog.error(e);
            return false;
        }
        return false;
    }

    public static void SaveBumpLog(String text) {
        ParseObject po = new ParseObject("log");
        po.put("msg", text);
        po.put("type", "Bump");
        po.pinInBackground(parameters.pinParseLog);
    }
}