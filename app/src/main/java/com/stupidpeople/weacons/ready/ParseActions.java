package com.stupidpeople.weacons.ready;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.GPSCoordinates;
import com.stupidpeople.weacons.WeaconParse;
import com.stupidpeople.weacons.WifiSpot;
import com.stupidpeople.weacons.ready.CallBackWeacons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 03/03/2016.
 */
public abstract class ParseActions {

    private static String tag = "WE";

    /**
     * Verifies if any of these ssids or bssids is in Parse (local) and log in the user
     *
     * @param callBackWeacons for returning the set of weacons in the zone in this scanning
     */
    public static void CheckSpotMatches(List<ScanResult> sr, final CallBackWeacons callBackWeacons) {

        ArrayList<String> bssids = new ArrayList<>();
        ArrayList<String> ssids = new ArrayList<>();

        for (ScanResult r : sr) {
            bssids.add(r.BSSID);
            ssids.add(r.SSID);
        }

        //TODO possibly we will want to have separate queriesin some cases
        //Query BSSID
        ParseQuery<WifiSpot> qb = ParseQuery.getQuery(WifiSpot.class);
        qb.whereContainedIn("bssid", bssids);
        //Query SSID
        ParseQuery<WifiSpot> qs = ParseQuery.getQuery(WifiSpot.class);
        qs.whereContainedIn("ssid", ssids);
        qs.whereEqualTo("relevant", true);
        //Main Query
        List<ParseQuery<WifiSpot>> queries = new ArrayList<>();
        queries.add(qb);
        queries.add(qs);

        ParseQuery<WifiSpot> mainQuery = ParseQuery.or(queries);
        mainQuery.fromPin(parameters.pinWeacons); //TODO veryfy if read from local
        mainQuery.include("associated_place");

        mainQuery.findInBackground(new FindCallback<WifiSpot>() {

            @Override
            public void done(List<WifiSpot> spots, ParseException e) {
                if (e == null) {
                    int n = spots.size();
                    HashSet<WeaconParse> weaconHashSet = new HashSet<>();

                    if (n == 0) {
                        myLog.add("MegaQuery no match", tag);
                    } else { //There are matches
                        StringBuilder sb = new StringBuilder("***********\n" + "From megaquery we have several matches: " + n);

                        for (WifiSpot spot : spots) {
                            sb.append(spot.summarizeWithWeacon() + "\n");
                            weaconHashSet.add(spot.getWeacon());
//                            registerHitSSID(spot); todo,
                        }
                        myLog.add(sb.toString(), tag);
                    }

                    myLog.add("Detected spots: " + spots.size() + " | Different weacons: " + weaconHashSet.size(), tag);

                    //it's important always deliver built weacons (in this way, they are of subclasses, as bus
                    callBackWeacons.OnReceive(WeaconParse.build(weaconHashSet));

                } else {
                    myLog.add("EEE en Chechkspotmarches:" + e, tag);
                }
            }
        });
    }

    /***
     * get wifispots from parse in a area and pin them the object includes the weacon
     *  @param bLocal  if should be queried in local database
     * @param radio   kms
     * @param center  center of queried area
     * @param context
     */

    public static void getSpots(final boolean bLocal, final double radio, final GPSCoordinates center, final Context context) {
        try {
            //TODO ver si tiene sentido leer los weacons de local
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
            myLog.add("---Error: failed retrieving SPOTS: " + e.getMessage(),tag);
        }
    }
}
