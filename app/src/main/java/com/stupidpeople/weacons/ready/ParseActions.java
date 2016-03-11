package com.stupidpeople.weacons.ready;

import android.net.wifi.ScanResult;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
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
}
