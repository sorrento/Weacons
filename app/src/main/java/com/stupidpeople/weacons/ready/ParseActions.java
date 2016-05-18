package com.stupidpeople.weacons.ready;

import android.content.Context;
import android.net.wifi.ScanResult;
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

    private static final int WEEK_IN_MILI = 7 * 24 * 60 * 60 * 1000;
    private static String tag = "PIN";

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
     * get the weacons around and pin them
     */
    public static void getNearWeacons(Context ctx) {
        LocationCallback listener = new LocationCallback() {
            @Override
            public void LocationReceived(GPSCoordinates gps) {
                ParseGeoPoint pos = gps.getGeoPoint();
                getNearWeacons(pos, null);
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


    public static void getNearWeacons(ParseGeoPoint pos, final MultiTaskCompleted mtc) {

//        //Query Weacons
//        ParseQuery<WeaconParse> queryWe = ParseQuery.getQuery(WeaconParse.class);
//        queryWe.whereNear("GPS", pos)
//                .setLimit(300)
//                .whereEqualTo("Type", "bus_stop");
//        myLog.add("---For bringing spots, around" + pos, "OJO");
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

    public static void getBusStopsInRadius(GPSCoordinates mGps, double kms, FindCallback<WeaconParse> listener) {
        ParseQuery<WeaconParse> query = ParseQuery.getQuery(WeaconParse.class);
        query.whereEqualTo("Type", "bus_stop")
                .whereWithinKilometers("GPS", mGps.getGeoPoint(), kms)
                .findInBackground(listener);
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

        //To remove the Silence button:
//        LogInManagement.NotifyFetching(false, false);//interesting=true for starting the timer 30segs
        Notifications.RemoveSilenceButton();//TODO quitar de aqui

        for (WeaconParse we : notifiedWeacons) arr.add(we.getObjectId());

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
        query.whereContainedIn("WeaconId", arr)
                .fromPin(parameters.pinFavorites)
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null) {
                            myLog.add("Recibidos favoritos para borrar:" + list.size(), tag);
                            ParseObject.unpinAllInBackground(parameters.pinFavorites, list, new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        myLog.add("Borrads los elementso de favoritos", tag);
                                    } else {
                                        myLog.add("No se han borrado los elementos de favo", tag);
                                    }
                                }
                            });
                        } else {
                            myLog.error(e);
                        }
                    }
                });
    }

    //HOME

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
                    for (WifiSpot ws : list) {
                        spotsAlreadyCreated.add(ws.getBSSID());
                    }

                    for (ScanResult r : sr) {
                        if (!spotsAlreadyCreated.contains(r.BSSID)) {
                            double dist = weBusStop.getGPS().distanceInKilometersTo(gps.getGeoPoint());
                            newOnes.add(new WifiSpot(r, weBusStop, gps, dist * 1000));
                        }
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
        ;
    }


    // Load weacons management

    /**
     * Check if there is some newer weacons in the area (respect to the last pinning in local) and if the nearest weacon
     * is the same as the one on local (to check if we are in new area)
     */
    public static void DownloadWeaconsIfNeeded(Context ctx) {
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
                            getNearWeacons(point, null);
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
                            getNearWeacons(point, null);
                        } else {
                            //Check if we moved
                            didWeMoved(point, newInAreaCB);
                        }
                    }
                };

                anyNewer(point, anyUpdatedCallback);
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
    private static void didWeMoved(ParseGeoPoint point, final booleanCallback bcb) {
        try {
            // if the nearest is <500m then no
            ParseQuery<WifiSpot> q = ParseQuery.getQuery(WifiSpot.class);
            WifiSpot first = q.whereNear("GPS", point).fromPin(parameters.pinWeacons).getFirst();
            if (point.distanceInKilometersTo(first.getGPS()) < 0.5) {
                bcb.OnResult(false);
                myLog.add("no nos hemos movido, el mas cercano está a menos de 500mts", "OJO");
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
                            myLog.add("[SPOTs]En 1km hay:" + nLocal + "(local) y " + i + "(web)", "aut");
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
        ParseQuery<WeaconParse> queryWe = ParseQuery.getQuery(WeaconParse.class);
        queryWe.whereNear("GPS", point)
                .setLimit(300)
                .whereEqualTo("Type", "bus_stop");
        myLog.add("----For comparation, quering aaround         " + point, "OJO");

        ParseQuery<WifiSpot> queryWi = ParseQuery.getQuery(WifiSpot.class);
        queryWi.whereWithinKilometers("GPS", point, 1)
                .whereMatchesQuery("associated_place", queryWe)
                .orderByDescending("updatedAt")
                .getFirstInBackground(new GetCallback<WifiSpot>() {
                    @Override
                    public void done(WifiSpot wifiSpot, ParseException e) {
                        if (e != null) {
                            myLog.add("tenemos un error al recibir los de la web en la zona:" + e, "OJO");
                        } else {
                            if (wifiSpot != null) {

                                boolean b = getWifisSameDateInLocalDB(wifiSpot.getCreatedAt(), point) == 0;

                                if (b) myLog.add("[newer ]Need to update localBBD", "OJO");
                                else myLog.add("[newer ]NO Need to update localBBD", "OJO");

                                bcb.OnResult(b);

//                                    Date dateWeb = wifiSpot.getUpdatedAt();
//                                    myLog.add("DateWeb=" + dateWeb + " DateLocal=" + wiLocal.getUpdatedAt(), "OJO");
//                                    myLog.add("obWeb=" + wifiSpot.getObjectId() + " OBLocal=" + wiLocal.getObjectId(), "OJO");
//                                    myLog.add("obWeb=" + wifiSpot + " OBLocal=" + wiLocal, "OJO");
                            } else {
                                myLog.add("wifispoit de la web es null", "OJO");
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
        myLog.add("# locales dcon misma fecha de creación (" + date + ")=" + count, "OJO");
        return count;
    }


    public static void SaveBumpLog(String text) {
        ParseObject po = new ParseObject("log");
        po.put("msg", text);
        po.put("type", "Bump");
        po.pinInBackground(parameters.pinParseLog);
    }

    /**
     * Gets all buststps without ssids associated in the radios, sorted by distance
     *
     * @param gps
     * @param mts
     * @param listener
     */
    public static void getFreeBusStopsInRadius(final GPSCoordinates gps, final int mts, final FindCallback<WeaconParse> listener) {
        ParseGeoPoint geoPoint = gps.getGeoPoint();


        //Filtramos aquellos menor a "mts"
        FindCallback<WeaconParse> listener2 = new FindCallback<WeaconParse>() {
            @Override
            public void done(List<WeaconParse> list, ParseException e) {
                List<WeaconParse> nearest = new ArrayList<>();
                for (WeaconParse we :
                        list) {
                    if (we.getGPS().distanceInKilometersTo(gps.getGeoPoint()) < (mts / 1000))
                        nearest.add(we);
                    else break;
                }
                listener.done(nearest, e);
            }
        };

        ParseQuery<WeaconParse> query = ParseQuery.getQuery(WeaconParse.class);
        query.whereEqualTo("Type", "bus_stop")
//                .whereWithinKilometers("GPS", geoPoint, mts / 1000)
                .whereNear("GPS", geoPoint)
                .setLimit(7)
                .whereDoesNotExist("n_scannings")
                .findInBackground(listener2);
    }

    public static void SaveIntensities2(List<ScanResult> sr, GPSCoordinates mGps) {
        ArrayList<ParseObject> intensities = new ArrayList<>();
        for (ScanResult r : sr) {
//            ParseObject intensity = ParseObject.create("Intensities");
            ParseObject intensity = new ParseObject("Intensities");
            intensity.put("level", r.level);
            intensity.put("GPS", mGps.getGeoPoint());
            intensity.put("ssid", r.SSID);
            intensity.put("bssid", r.BSSID);

            intensities.add(intensity);
        }
        myLog.add("..savingseveral intensities " + intensities.size(), "ADD_STOP");
        ParseObject.saveAllInBackground(intensities, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                myLog.add("saved several intensities ", "ADD_STOP");
            }
        });
    }

    static void LogInParse() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    myLog.add("Looged as anonimous", "");
                } else {
                    myLog.add("NOTLooged as anonimous e= " + e.getLocalizedMessage(), tag);
                }
            }
        });
    }
}