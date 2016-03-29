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
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WifiSpot;
import com.stupidpeople.weacons.booleanCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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
     * @param callBackWeacons for returning the set of weacons in the zone in this scanning
     * @param ctx
     */
    public static void CheckSpotMatches(List<ScanResult> sr, final CallBackWeacons callBackWeacons, final Context ctx) {

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
                    int n = spots.size();
                    HashSet<WeaconParse> weaconHashSet = new HashSet<>();

                    if (n == 0) {
                        myLog.add("MegaQuery no match", tag);
                    } else { //There are matches
                        StringBuilder sb = new StringBuilder("***********\n");

                        for (WifiSpot spot : spots) {
                            sb.append("\t" + spot.summarizeWithWeacon() + "\n");
                            weaconHashSet.add(spot.getWeacon());
//                            registerHitSSID(spot); todo
                        }

                        // It's important always deliver built weacons (in this way, they are of subclasses, as bus
                        WeaconParse.build(weaconHashSet, ctx);

                        myLog.add(sb.toString(), tag);
                        myLog.add("Detected spots: " + spots.size() + " | Different weacons: " + weaconHashSet.size(), tag);
                    }

                    callBackWeacons.OnReceive(weaconHashSet);

                } else {
                    myLog.add("EEE en Chechkspotmarches:" + e, tag);
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
                                                    myLog.add("Wecaons pinned ok", tag);
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
        //Query Weacon
        ParseQuery<WeaconParse> queryWe = ParseQuery.getQuery(WeaconParse.class);
        queryWe.whereNear("GPS", pos)
                .setLimit(300)
                .whereEqualTo("Type", "bus_stop");

        //Query Spots
        ParseQuery<WifiSpot> query = ParseQuery.getQuery(WifiSpot.class);
        query.whereMatchesQuery("associated_place", queryWe)
                .setLimit(1000)
                .findInBackground(new FindCallback<WifiSpot>() {
                    @Override
                    public void done(List<WifiSpot> list, ParseException e) {
                        if (e == null) {
                            myLog.add("****el numero de wifispots recogideos es: " + list.size(), "aut");
                            pinSpotsInLocal(list);
                            if (mtc != null) mtc.OneTaskCompleted();
                        } else {
                            myLog.error(e);
                        }
                    }
                });
    }

    private static void pinSpotsInLocal(List<WifiSpot> spots) {
        ParseObject.pinAllInBackground(parameters.pinWeacons, spots, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    myLog.add("Wecaons pinned ok", tag);
                } else {
                    myLog.add("---Error retrieving Weacons from web: " + e.getMessage(), tag);
                }
            }
        });//TODO si funciona quitar el callback
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
            fav.pinInBackground("Favoritos", new SaveCallback() {
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
            query.fromPin("Favoritos");
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
        LogInManagement.NotifyMultipleFetching(false, false);

        for (WeaconParse we : notifiedWeacons) {
            arr.add(we.getObjectId());
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
        query.whereContainedIn("WeaconId", arr);
        query.fromPin("Favoritos");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    myLog.add("Recibidos favoritos para borrar:" + list.size(), "aut");
                    ParseObject.unpinAllInBackground("Favoritos", list, new DeleteCallback() {
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
                    myLog.add("Detected: " + sr.size() + " alread created: " + list.size(), tag);
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
                                String text = "se ha subido la parada:" + weBusStop.getName();
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
     * Check if there is some weacon newer in the area (respect to the last update) and if the nearest weacon
     * is the same as the one on local
     */

    public static void DownloadWeaconsIfNeded(Context ctx) {
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
                            myLog.add("***There are new SPOTS in the zone , gonna update", "aut");
                            getSpotsForBusStops(point, null);
                        } else {
                            myLog.add("***Nada que actualizar, en web lo mismo que en local (1km)", "aut");
                        }
                    }
                };
                booleanCallback bcb = new booleanCallback() {
                    @Override
                    public void OnResult(boolean b) {
                        if (b) {
                            //actualizamos
                            myLog.add("***There are newer in the zone (updatedAt), gonna update", "aut");
                            getSpotsForBusStops(point, null);
                        } else {
                            //Check if we moved
                            anyNewInArea(point, newInAreaCB);
                        }
                    }
                };

                anyUpdated(point, bcb);
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
                            bcb.OnResult(nLocal == i);
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

            // en web
            ParseQuery<WifiSpot> queryW = ParseQuery.getQuery(WifiSpot.class);
            queryW.whereWithinKilometers("GPS", point, 1)
                    .orderByDescending("updatedAt")
                    .getFirstInBackground(new GetCallback<WifiSpot>() {
                        @Override
                        public void done(WifiSpot wifiSpot, ParseException e) {
                            Date dateWeb = wifiSpot.getUpdatedAt();
                            myLog.add("DateWeb=" + dateWeb + " DateLocal=" + wiLocal.getUpdatedAt(), "aut");
                            bcb.OnResult(dateWeb.after(wiLocal.getUpdatedAt()));
                        }
                    });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}