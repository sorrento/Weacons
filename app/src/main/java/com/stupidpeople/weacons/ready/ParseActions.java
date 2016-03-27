package com.stupidpeople.weacons.ready;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.widget.Toast;

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
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WifiSpot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 03/03/2016.
 */
public abstract class ParseActions {

    static int iWithOutResults = 0;
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
            if (r.level > -81) bssids.add(r.BSSID);
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
                        iWithOutResults++;
                        myLog.add("MegaQuery no match", tag);
                        if (iWithOutResults % 20 == 0) LoadWeaconsIfNeeded(ctx);
                    } else { //There are matches
                        iWithOutResults = 0;
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

    /**
     * Check if nearest weacon is web is the same as local
     *
     * @param ctx
     */
    private static void LoadWeaconsIfNeeded(Context ctx) {

        new LocationAsker(ctx, new LocationCallback() {
            @Override
            public void LocationReceived(final GPSCoordinates gps) {
                WeaconParse weLocal = null;
                try {
                    ParseQuery<WeaconParse> qLocal = ParseQuery.getQuery(WeaconParse.class);
                    qLocal.whereNear("GPS", gps.getGeoPoint())
                            .fromPin(parameters.pinWeacons);
                    weLocal = qLocal.getFirst();

                    ParseQuery<WeaconParse> qWeb = ParseQuery.getQuery(WeaconParse.class);
                    final String localObId = weLocal.getObjectId();
                    qWeb.whereNear("GPS", gps.getGeoPoint())
                            .getFirstInBackground(new GetCallback<WeaconParse>() {
                                @Override
                                public void done(WeaconParse weaconParse, ParseException e) {
                                    if (weaconParse.getObjectId().equals(localObId)) {
                                        myLog.add("No need to load weacons", "aut");
                                    } else {
                                        myLog.add("We need to load weacons in this area", "aut");
                                        getSpotsForBusStops(gps.getGeoPoint());
                                    }
                                }
                            });
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {

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
                getSpotsForBusStops(pos);
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {

            }
        };
        ;
        new LocationAsker(ctx, listener);
    }

    private static void getSpotsForBusStops(ParseGeoPoint pos) {
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
     * @param paradaId
     * @param sr
     * @param ctx
     */
    public static void assignSpotsToWeacon(final String paradaId, final List<ScanResult> sr, final GPSCoordinates gps, final Context ctx) {
        final ArrayList<String> macs = new ArrayList<>();
        for (ScanResult r : sr) {
            macs.add(r.BSSID);
        }

        ParseQuery<WeaconParse> query = ParseQuery.getQuery(WeaconParse.class);
        query.whereEqualTo("paradaId", paradaId);
        query.getFirstInBackground(new GetCallback<WeaconParse>() {
            @Override
            public void done(final WeaconParse weParada, ParseException e) {
                if (e == null) {
                    final String weId = weParada.getObjectId();

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

                                final WeaconParse we = (WeaconParse) ParseObject.createWithoutData("Weacon", weId);
                                for (ScanResult r : sr) {
                                    if (!spotsAlreadyCreated.contains(r.BSSID)) {
                                        WifiSpot ws = new WifiSpot(r.SSID, r.BSSID, we, gps.getLatitude(), gps.getLongitude());
                                        newOnes.add(ws);
                                    }
                                }

                                //Upload batch
                                WifiSpot.saveAllInBackground(newOnes, new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            myLog.add("subidos varios wifispots " + newOnes.size(), tag);
                                            Toast.makeText(ctx, "se ha subido la parada:" + weParada.getName(), Toast.LENGTH_SHORT).show();

                                            //create the weMeasured
                                            final ParseObject weMeasured = ParseObject.create("WeMeasured");
                                            weMeasured.put("weacon", we);
                                            weMeasured.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        final ArrayList<ParseObject> intiensities = new ArrayList<ParseObject>();
                                                        for (ScanResult r : sr) {
                                                            ParseObject intensity = ParseObject.create("Intensities");
                                                            intensity.put("level", r.level);
                                                            intensity.put("weMeasured", weMeasured);
                                                            intensity.put("ssid", r.SSID);
                                                            intensity.put("bssid", r.BSSID);
                                                            intiensities.add(intensity);
                                                        }
                                                        ParseObject.saveAllInBackground(intiensities, new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    myLog.add("saved several intensities " + intiensities.size(), tag);
                                                                    // aumentar el n_scannings del weacon  en uno
                                                                    we.increment("n_scannings");
                                                                    we.saveInBackground(new SaveCallback() {
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
}
