package com.stupidpeople.weacons.ready;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Location.GPSCoordinates;
import com.stupidpeople.weacons.Location.LocationAsker;
import com.stupidpeople.weacons.Location.LocationCallback;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.Wifi.WifiSpot;
import com.stupidpeople.weacons.Wigles;
import com.stupidpeople.weacons.booleanCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.StringUtils;
import util.myLog;
import util.parameters;

import static util.StringUtils.Listar;

/**
 * Created by Milenko on 03/03/2016.
 */
public abstract class ParseActions {

    private static final int WEEK_IN_MILI = 7 * 24 * 60 * 60 * 1000;
    private static String tag = "PARSE";

    /**
     * Verifies if any of these ssids or bssids is in Parse (local) and log in the user
     *
     * @param ctx
     * @param callBackWeacons for returning the set of weacons in the zone in this scanning
     */
    public static void checkSpotMatches(final List<ScanResult> sr, final Context ctx, final CallBackWeacons callBackWeacons) {
        final ArrayList<String> bssids = new ArrayList<>();

        myLog.add(StringUtils.ListarSR(sr), "SSIDS");

        for (ScanResult r : sr) bssids.add(r.BSSID);

        // TEST
        if (parameters.simulateWifi) bssids.add(parameters.wifiToSimulateMac);

        //Query BSSID
        ParseQuery<WifiSpot> qb = ParseQuery.getQuery(WifiSpot.class);
        qb.whereContainedIn("bssid", bssids)
                .fromPin(parameters.pinWeacons)
                .whereNotEqualTo("distanceWe", -1) //wigle tienen -1
                .include("associated_place")
                .findInBackground(new FindCallback<WifiSpot>() {

                    @Override
                    public void done(List<WifiSpot> spots, ParseException e) {
                        if (e == null) {
                            HashMap<WeaconParse, ArrayList<String>> weaconHash = new HashMap<>();

                            if (spots.size() == 0) {
                                //WIGLE
                                if (parameters.useWigle)
                                    checkOnWigle(bssids, weaconHash, ctx);
                            } else { //There are matches

                                for (WifiSpot spot : spots) {
                                    addWeAndSpotToHash(weaconHash, spot);
                                    registerSpotLastTimeSeen(spot);
                                }

                                // It's important always deliver built weacons (in this way, they are of subclasses, as bus
                                WeaconParse.build(weaconHash, ctx);
                            }


                            //insertion of TEST weacons
                            if (parameters.testWeacons) addTestWeacons(weaconHash, ctx);

                            myLog.weaconsDetected(weaconHash, spots.size(), sr.size());

                            callBackWeacons.OnReceive(createHashSet(weaconHash.keySet()));

                        } else {
                            myLog.add("EEE en Chechkspotmarches:" + e.getLocalizedMessage(), tag);
                        }
                    }
                });


    }

    private static void registerSpotLastTimeSeen(WifiSpot spot) {
        spot.put("lastTimeSeen", new Date());
        spot.put("seenBy", ParseUser.getCurrentUser());
        spot.pinInBackground(parameters.pinLastTimeSeen);
    }

    private static void addWeAndSpotToHash(HashMap<WeaconParse, ArrayList<String>> weaconHash, WifiSpot spot) {
        WeaconParse we = spot.getWeacon();
        ArrayList<String> arr = new ArrayList<>();
        if (weaconHash.containsKey(we)) arr = weaconHash.get(we);
        arr.add(spot.getSSID());
        weaconHash.put(we, arr);
    }

    private static void addTestWeacons(HashMap<WeaconParse, ArrayList<String>> weaconsDetected, Context mContext) {
        ParseQuery<WeaconParse> q = ParseQuery.getQuery(WeaconParse.class);
        List<String> arr = Arrays.asList(parameters.weaconsTest);
        List<WeaconParse> res = null;

        try {
            res = q.whereContainedIn("objectId", arr).find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (WeaconParse we : res) {
            we.build(mContext);
            ArrayList<String> arr2 = new ArrayList<>();
            arr2.add("NONE");
            weaconsDetected.put(we, arr2);
        }
    }

    private static HashSet<WeaconParse> createHashSet(Set<WeaconParse> weaconParses) {
        HashSet<WeaconParse> res = new HashSet<>();

        for (WeaconParse we : weaconParses) res.add(we);

        return res;
    }

    /***
     * get wifispots from parse in a area and pin them the object includes the weacon
     *
     * @param bLocal  if should be queried in local database
     * @param radio   kms
     * @param center  center of queried area
     * @param context
     */
//    public static void getSpots(final boolean bLocal, final double radio, final GPSCoordinates center, final Context context) {
//        try {
//            //1.Remove spots and weacons in local
//            myLog.add("retrieving SSIDS from local:" + bLocal + " user: " + ParseUser.getCurrentUser(), tag);
//            ParseObject.unpinAllInBackground(parameters.pinWeacons, new DeleteCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if (e == null) {
//
//                        //2. Load them
//                        ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
//                        query.whereWithinKilometers("GPS", new ParseGeoPoint(center.getLatitude(), center.getLongitude()), radio);
//                        query.include("associated_place");
//                        query.setLimit(900);
//
//                        if (bLocal) query.fromLocalDatastore();
//                        query.findInBackground(new FindCallback<WifiSpot>() {
//                            @Override
//                            public void done(List<WifiSpot> spots, ParseException e) {
//                                if (e == null) {
//
//                                    //3. Pin them
//                                    myLog.add("number of SSIDS Loaded for weacons:" + spots.size(), tag);
//                                    if (!bLocal)
//                                        ParseObject.pinAllInBackground(parameters.pinWeacons, spots, new SaveCallback() {
//                                            @Override
//                                            public void done(ParseException e) {
//                                                if (e == null) {
//                                                    myLog.add("Wecaons pinned ok", "aut");
//                                                    Toast.makeText(context, "Weacons Loaded", Toast.LENGTH_SHORT).show();
//                                                } else {
//                                                    myLog.add("---Error retrieving Weacons from web: " + e.getMessage(), tag);
//                                                }
//                                            }
//                                        });
//                                } else {
//                                    myLog.add("---ERROR from parse obtienning ssids" + e.getMessage(), tag);
//                                }
//                            }
//                        });
//                    } else {
//                        myLog.error(e);
//                    }
//                }
//            });
//        } catch (Exception e) {
//            myLog.add("---Error: failed retrieving SPOTS: " + e.getMessage(), tag);
//        }
//    }

    /**
     * get the weacons around and pin them
     */
    public static void getNearWifiSpots(Context ctx) {
        LocationCallback listener = new LocationCallback() {


            @Override
            public void NotPossibleToReachAccuracy() {

            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {
                ParseGeoPoint pos = gps.getGeoPoint();
                getNearWifiSpots(pos, null);
            }
        };
        new LocationAsker(ctx, listener);
    }


    public static void getNearWifiSpots(ParseGeoPoint pos, final MultiTaskCompleted mtc) {

//        //Query Weacons
//        ParseQuery<WeaconParse> queryWe = ParseQuery.getQuery(WeaconParse.class);
//        queryWe.whereNear("GPS", pos)
//                .setLimit(300)
//                .whereEqualTo("Type", "bus_stop");
//        myLog.add("---For bringing spots, around" + pos, "WE_DOWNLOAD");
        //Query WifiSpots
        ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
        query
//                .whereMatchesQuery("associated_place", queryWe)
                .whereWithinKilometers("GPS", pos, 1)
                .setLimit(1000)
                .include("associated_place")
                .findInBackground(new FindCallback<WifiSpot>() {
                    @Override
                    public void done(final List<WifiSpot> list, ParseException e) {
                        if (e == null) {
                            if (!hasPassedAWeek()) {
                                try {
                                    ParseObject.pinAll(parameters.pinWeacons, list);
                                } catch (ParseException e1) {
                                    myLog.error(e1);
                                }
                            } else { //delete local database once a week
                                ParseObject.unpinAllInBackground(parameters.pinWeacons, new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        try {
                                            WifiObserverService.prefs.edit()
                                                    .putLong("deletedLocalDb", new Date()
                                                            .getTime()).commit();

                                            ParseObject.pinAll(parameters.pinWeacons, list);
                                        } catch (ParseException e1) {
                                            myLog.error(e1);
                                        }
                                    }
                                });
                            }

                            if (mtc != null) mtc.OneTaskCompleted();
                        } else {
                            myLog.error(e);
                        }
                    }
                });
    }

    private static boolean hasPassedAWeek() {

        long last = WifiObserverService.prefs.getLong("deletedLocalDb", 0);
        long now = new Date().getTime();
        long timeDiff = now - last;

        return timeDiff > WEEK_IN_MILI;
    }


    //Interesting

    /**
     * Mark in local parse that these weacons are interesting, so they will sound and will be fetched
     * automatically (first time)
     *
     * @param weacons
     */
    public static void AddToInteresting(final ArrayList<WeaconParse> weacons) {
        for (final WeaconParse we : weacons) AddToInteresting(we);
    }

    public static void AddToInteresting(final WeaconParse we) {
        if (we.isInteresting()) return;

        we.setInteresting(true);

        ParseObject fav = new ParseObject("Favorites");
        fav.put("WeaconId", we.getObjectId());
        try {
            fav.pin(parameters.pinFavorites);
        } catch (ParseException e) {
            myLog.error(e);
        }
        LogInManagement.now.update();
//        fav.pinInBackground(parameters.pinFavorites, new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    myLog.add("se ha pinneado el favorito  " + we.getName(), tag);
//
//                } else {
//                    myLog.add("No se ha pinneaso el favorito " + we.getName() + e.getLocalizedMessage(), tag);
//                }
//            }
//        });
    }

    public static boolean isInteresting(String objectId) {
        int i = 0;
        try {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
            i = query.whereEqualTo("WeaconId", objectId)
                    .fromPin(parameters.pinFavorites).count();
        } catch (ParseException e) {
            myLog.error(e);
        }
        return i > 0;
    }

    public static void removeInteresting(ArrayList<WeaconParse> notifiedWeacons) {
        ArrayList arr = new ArrayList();

        for (WeaconParse we : notifiedWeacons) {
            we.setInteresting(false);
            arr.add(we.getObjectId());
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
        try {
            List list = query.whereContainedIn("WeaconId", arr)
                    .fromPin(parameters.pinFavorites)
                    .find();
            ParseObject.unpinAll(parameters.pinFavorites, list);
        } catch (ParseException e) {
            myLog.error(e);
        }
//lo mismo pero en BG
//        query.whereContainedIn("WeaconId", arr)
//                .fromPin(parameters.pinFavorites)
//                .findInBackground(new FindCallback<ParseObject>() {
//                    @Override
//                    public void done(List<ParseObject> list, ParseException e) {
//                        if (e == null) {
//                            myLog.add("Recibidos favoritos para borrar:" + list.size(), tag);
//                            ParseObject.unpinAllInBackground(parameters.pinFavorites, list, new DeleteCallback() {
//                                @Override
//                                public void done(ParseException e) {
//                                    if (e == null) {
//                                        myLog.add("Borrads los elementso de favoritos", tag);
//                                    } else {
//                                        myLog.add("No se han borrado los elementos de favo", tag);
//                                    }
//                                }
//                            });
//                        } else {
//                            myLog.error(e);
//                        }
//                    }
//                });
    }

    //HOME

    public static void setHome(final WeaconParse we) {
        ParseObject home = new ParseObject("Home");
        home.put("WeaconId", we.getObjectId());
        home.pinInBackground(parameters.pinFavorites, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.addToParse("Se ha pinneado el home" + we.getName(), tag);
                } else {
                    myLog.add("No se ha pinneaso el home " + we.getName() + e.getLocalizedMessage(), tag);
                }
            }
        });
    }

    public static boolean IsHome(String obId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Home");
        try {
            List<ParseObject> found = query.whereEqualTo("WeaconId", obId)
                    .fromPin(parameters.pinFavorites)
                    .find();
            if (found != null && found.size() > 0) return true;
        } catch (ParseException e) {
            myLog.error(e);
        }
        return false;
    }

    public static void ClearHomes() {
        ParseQuery<ParseObject> q = ParseQuery.getQuery("Home");
        q.fromPin(parameters.pinFavorites)
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        try {
                            ParseObject.unpinAll(parameters.pinFavorites, list);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
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

        for (ScanResult r : sr) macs.add(r.BSSID);

        // Create only the new ones
        ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
        query.whereContainedIn("bssid", macs);
        query.findInBackground(new FindCallback<WifiSpot>() {
            @Override
            public void done(List<WifiSpot> list, ParseException e) {
                List<String> spotsAlreadyCreated = new ArrayList<>();
                final ArrayList<WifiSpot> newOnes = new ArrayList<>();

                if (e == null) {
                    myLog.add("Detected: " + sr.size() + " alread created:y " + list.size(), "ADD_STOP");
                    for (WifiSpot ws : list) spotsAlreadyCreated.add(ws.getBSSID());

                    for (ScanResult r : sr) {
                        if (!spotsAlreadyCreated.contains(r.BSSID)) {
                            double dist = weBusStop.getGPS().distanceInKilometersTo(gps.getGeoPoint());
                            newOnes.add(new WifiSpot(r, weBusStop, gps, dist * 1000));
                        }
                    }

                    //Case there is no new wifi, we assign just one (it will produce that gathers two weacons)
                    if (newOnes.size() == 0) {
                        myLog.add("Assingnango una wifi que ya estaba asignada", "ADD_STOP");
                        double dist = weBusStop.getGPS().distanceInKilometersTo(gps.getGeoPoint());
                        newOnes.add(new WifiSpot(sr.get(0), weBusStop, gps, dist * 1000));
                    }

                    //Upload batch
                    WifiSpot.saveAllInBackground(newOnes, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                myLog.add("subidos varios wifispots: " + WifiSpot.Listar(newOnes), "ADD_STOP");
                                String text = ctx.getString(R.string.busstop_uploaded) + weBusStop.getName();
                                myLog.add(text, "ADD_STOP");
                                Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();

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


    public static void increaseNScannings(WeaconParse weBusStop) {
        // aumentar el n_scannings del weacon  en uno
        weBusStop.increment("n_scannings");
        weBusStop.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("incrementado el we de parada en uno", "ADD_STOP");
                } else
                    myLog.error(e);
            }
        });
    }


    // Load weacons management

    /**
     * Check if there is some newer weacons in the area (respect to the last pinning in local) and if the nearest weacon
     * is the same as the one on local (to check if we are in new area)
     */
    public static void DownloadWeaconsIfNeeded(Context ctx) {
        myLog.add("\n*******Veamos si se necesita bajar weacons:", "WE_DOWNLOAD");
        //AskLocation
        new LocationAsker(ctx, new LocationCallback() {

            @Override
            public void NotPossibleToReachAccuracy() {
                myLog.add("No he obtenido la posición GPS", "WE_DOWNLOAD");
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {
                final ParseGeoPoint point = gps.getGeoPoint();

                final booleanCallback newInAreaCB = new booleanCallback() {
                    @Override
                    public void OnResult(boolean b) {
                        if (b) {
                            //actualizamos
                            myLog.addToParse("***There are new SPOTS in the zone , gonna update", "WE_DOWNLOAD");
                            getNearWifiSpots(point, null);
                        } else {
                            myLog.add("***Nada que actualizar, en web lo mismo que en local (1km)", "WE_DOWNLOAD");
                        }
                    }
                };

                booleanCallback anyUpdatedCallback = new booleanCallback() {
                    @Override
                    public void OnResult(boolean b) {
                        if (b) {
                            //actualizamos
                            myLog.addToParse("***There are newer in the zone (updatedAt), gonna update", "WE_DOWNLOAD");
                            getNearWifiSpots(point, null);
                        } else {
                            //Check if we moved
                            didWeMoved(point, newInAreaCB);
                        }
                    }
                };

                anyNewer(point, anyUpdatedCallback);

            }

        });
    }

    /**
     * Check if the number of wifis is the same in local wrt web in 1km
     *
     * @param point
     */
    private static void didWeMoved(ParseGeoPoint point, final booleanCallback bcb) {
        try {
            // if the nearest is <100m then no
            ParseQuery<WifiSpot> q = ParseQuery.getQuery(WifiSpot.class);
            WifiSpot first = q.whereNear("GPS", point).fromPin(parameters.pinWeacons).getFirst();
            if (point.distanceInKilometersTo(first.getGPS()) < 0.1) {
                bcb.OnResult(false);
                myLog.add("no nos hemos movido, el mas cercano está a menos de 100mts", "WE_DOWNLOAD");
                return;
            }

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
                            myLog.add("[SPOTs]En 1km hay:" + nLocal + "(local) y " + i + "(web)", "WE_DOWNLOAD");
                            bcb.OnResult(nLocal != i);
                        }
                    });
        } catch (ParseException e) {
            myLog.error(e);
        }

    }


    /**
     * Compares the latest update time in local wrt web (of spots in the area of 1km
     *
     * @param point
     * @param bcb
     */
    private static void anyNewer(final ParseGeoPoint point, final booleanCallback bcb) {
        // Web. Query Weacons first
//        ParseQuery<WeaconParse> queryWe = ParseQuery.getQuery(WeaconParse.class);
//        queryWe.whereNear("GPS", point)
//                .setLimit(300)
//                .whereEqualTo("Type", "bus_stop");
//        myLog.add("----For comparation, quering aaround         " + point, "WE_DOWNLOAD");

        ParseQuery<WifiSpot> queryWi = ParseQuery.getQuery(WifiSpot.class);
        queryWi.whereWithinKilometers("GPS", point, 1)
//                .whereMatchesQuery("associated_place", queryWe)
                .orderByDescending("updatedAt")
                .getFirstInBackground(new GetCallback<WifiSpot>() {
                    @Override
                    public void done(WifiSpot wifiSpot, ParseException e) {
                        if (e != null) {
                            myLog.add("tenemos un error al recibir los de la web en la zona:" + e, "WE_DOWNLOAD");
                        } else {
                            if (wifiSpot != null) {

                                boolean b = getWifisSameDateInLocalDB(wifiSpot.getCreatedAt(), point) == 0;

                                if (b) myLog.add("[newer ]Need to update localBBD", "WE_DOWNLOAD");
                                else myLog.add("[newer ]NO Need to update localBBD", "WE_DOWNLOAD");

                                bcb.OnResult(b);

//                                    Date dateWeb = wifiSpot.getUpdatedAt();
//                                    myLog.add("DateWeb=" + dateWeb + " DateLocal=" + wiLocal.getUpdatedAt(), "WE_DOWNLOAD");
//                                    myLog.add("obWeb=" + wifiSpot.getObjectId() + " OBLocal=" + wiLocal.getObjectId(), "WE_DOWNLOAD");
//                                    myLog.add("obWeb=" + wifiSpot + " OBLocal=" + wiLocal, "WE_DOWNLOAD");
                            } else {
                                myLog.add("wifispoit de la web es null", "WE_DOWNLOAD");
                            }
                        }

                    }
                });

    }

    private static int getWifisSameDateInLocalDB(Date date, ParseGeoPoint point) {
        ParseQuery<WifiSpot> queryWi = ParseQuery.getQuery(WifiSpot.class);
        int count = 0;
        try {
            count = queryWi.whereWithinKilometers("GPS", point, 1)
                    .whereEqualTo("createdAt", date)
                    .fromPin(parameters.pinWeacons)
                    .count();
        } catch (ParseException e) {
            myLog.error(e);
        }
        myLog.add("# locales dcon misma fecha de creación (" + date + ")=" + count, "WE_DOWNLOAD");
        return count;
    }


    /**
     * Gets all buststps without ssids associated in the radios, sorted by distance
     *
     * @param gps
     * @param mts
     * @param listener
     */
    public static void getBusStopsInRadius(final GPSCoordinates gps, final double mts, final FindCallback<WeaconParse> listener) {
        ParseGeoPoint geoPoint = gps.getGeoPoint();
        // Filtramos aquellos menor a "mts"
        FindCallback<WeaconParse> listener2 = new FindCallback<WeaconParse>() {
            @Override
            public void done(List<WeaconParse> list, ParseException e) {
                List<WeaconParse> nearest = new ArrayList<>();
                for (WeaconParse we : list) {
                    if (we.getGPS().distanceInKilometersTo(gps.getGeoPoint()) < (mts / 1000))
                        nearest.add(we);
                }
                listener.done(nearest, e);
            }
        };

        ParseQuery<WeaconParse> query = ParseQuery.getQuery(WeaconParse.class);
        query.whereEqualTo("Type", "bus_stop")
                .whereNear("GPS", geoPoint)
                .setLimit(7)
//                .whereDoesNotExist("n_scannings")
                .findInBackground(listener2);
    }

    public static void SaveIntensities2(List<ScanResult> sr, GPSCoordinates mGps) {
        ArrayList<ParseObject> intensities = new ArrayList<>();
        for (ScanResult r : sr) {
            ParseObject intensity = new ParseObject("Intensities");
            intensity.put("level", r.level);
            intensity.put("GPS", mGps.getGeoPoint());
            intensity.put("ssid", r.SSID);
            intensity.put("bssid", r.BSSID);

            intensities.add(intensity);
        }
        ParseObject.saveAllInBackground(intensities, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("saved several intensities ", "ADD_STOP");
                } else {
                    myLog.add("Not possible to saved intensities " + e.getLocalizedMessage(), "ADD_STOP");
                }
            }
        });
    }

    public static void LogInParse() {

        try {
            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (e == null) {
                            myLog.add("Looged as anonimous", tag);
                        } else {
                            myLog.add("NOTLooged as anonimous e= " + e.getLocalizedMessage(), tag);
                        }
                    }
                });
            }
        } catch (Exception e) {
            myLog.error(e);
        }
    }


    // WIFI SPOTS

    public static void removeSpotsOfWeacon(WeaconParse we, final DeleteCallback deleteCB) {
        FindCallback<WifiSpot> findCallback = new FindCallback<WifiSpot>() {
            @Override
            public void done(List<WifiSpot> list, ParseException e) {
                ParseObject.deleteAllInBackground(list, deleteCB);
            }
        };
        getSpotsOfWeacon(we, findCallback);

    }

    private static void getSpotsOfWeacon(WeaconParse we, FindCallback<WifiSpot> findCallback) {
        ParseQuery<WifiSpot> q = new ParseQuery<>(WifiSpot.class);
        q.whereEqualTo("associated_place", we)
                .findInBackground(findCallback);
    }


    // WIGLE

    private static void checkOnWigle(ArrayList<String> bssids, final HashMap<WeaconParse, ArrayList<String>> weaconHash, final Context ctx) {

        //Query BSSID
        ParseQuery<WifiSpot> qb = ParseQuery.getQuery(WifiSpot.class);
        qb.whereContainedIn("bssid", bssids)
                .fromPin(parameters.pinWeacons)
                .whereEqualTo("distanceWe", -1)
                .include("associated_place")
                .findInBackground(new FindCallback<WifiSpot>() {

                    @Override
                    public void done(List<WifiSpot> spots, ParseException e) {
                        if (e == null) {
//                            HashMap<WeaconParse, ArrayList<String>> weaconHash = new HashMap<>();

                            if (spots.size() != 0) {
                                //There are matches

                                //we take only the first, to avoid messing up
                                WifiSpot wifiSpot = spots.get(0);
                                WeaconParse we = wifiSpot.getWeacon();

//                                Vibrator vi = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
//                                vi.vibrate(1000);
                                Toast.makeText(ctx, "Hemos encontrado un wifi d WIGLE!\n" + we.getName(), Toast.LENGTH_SHORT).show();
                                final String text = "FOUND WIGLE\n" + we + "\n\n" + Listar(spots);
                                myLog.addToParse(text, "Wigle");

                                // It's important always deliver built weacons (in this way, they are of subclasses, as bus
                                we.build(ctx);
                                addWeAndSpotToHash(weaconHash, wifiSpot);

                                new Wigles(we, ctx);
                            }

                        } else {
                            myLog.add("EEE en Chechkspotmarches:" + e.getLocalizedMessage(), tag);
                        }
                    }
                });

    }

    public static void wigleRemoveSSIDS(final WeaconParse we) {
        ParseQuery<WifiSpot> q = ParseQuery.getQuery(WifiSpot.class);
        q.whereEqualTo("distanceWe", -1)
                .whereEqualTo("associated_place", we)
                .findInBackground(new FindCallback<WifiSpot>() {
                    @Override
                    public void done(final List<WifiSpot> list, ParseException e) {
                        if (e == null) {
                            myLog.add("Tenemos wigle asociacos a este weacon:" + list.size() + " " + we.getName(), "WIGLE");
                            ParseObject.deleteAllInBackground(list, new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        myLog.add("Se han borrado tods los wigle de aquí:" + list.size(), "WIGLE");
                                    } else {

                                        myLog.add("NO Se han borrado tods los wigle de aquí:" + e.getLocalizedMessage(), "WIGLE");
                                    }
                                }
                            });
                        } else {
                            myLog.error(e);
                        }
                    }
                });

    }

    public static void wigleHasWeaconGoodSSID(WeaconParse we, final booleanCallback bcb) {
        ParseQuery<WifiSpot> q = ParseQuery.getQuery(WifiSpot.class);
        q.whereNotEqualTo("distanceWe", -1)
                .whereEqualTo("associated_place", we)
                .countInBackground(new CountCallback() {
                    @Override
                    public void done(int i, ParseException e) {
                        if (e == null) bcb.OnResult(i > 0);
                        else myLog.error(e);
                    }
                });
    }

    public static void saveInstalationCoordinates(Context ctx) {
        new LocationAsker(ctx, new LocationCallback() {
            @Override
            public void NotPossibleToReachAccuracy() {
                myLog.add("Not possible to get coordiates to save", tag);
                saveCoords(new GPSCoordinates(0, 0), 0);
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {
                saveCoords(gps, accuracy);
            }

            private void saveCoords(GPSCoordinates gps, double accuracy) {
                try {
                    ParseObject ob = new ParseObject("InstalationCoords");
                    ob.put("GPS", gps.getGeoPoint());
                    ob.put("accuracy", accuracy);
                    ob.put("model", Build.MODEL);
                    ob.put("phoneId", Build.ID);
                    ParseUser user = ParseUser.getCurrentUser();
                    if (user != null) {
                        ob.put("user", user);
                    }
                    ob.saveEventually();
                } catch (Exception e) {
                    myLog.error(e);
                }
            }
        });
    }
}