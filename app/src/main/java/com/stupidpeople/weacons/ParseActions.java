package com.stupidpeople.weacons;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 03/03/2016.
 */
public abstract class ParseActions {

    /**
     * Verifies if any of these ssids or bssids is in Parse (local) and log in the user
     *
     * @param bssids
     * @param ssids
     */
    public static void CheckSpotMatches(ArrayList<String> bssids, ArrayList<String> ssids) {

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
        mainQuery.fromPin(parameters.pinWeacons);
        mainQuery.include("associated_place");

        mainQuery.findInBackground(new FindCallback<WifiSpot>() {

            @Override
            public void done(List<WifiSpot> spots, ParseException e) {
                if (e == null) {
                    int n = spots.size();
                    HashSet<WeaconParse> weaconHashSet = new HashSet<>();

                    if (n == 0) {
                        myLog.add("MegaQuery no match", "WE");
                    } else { //There are matches

                        StringBuilder sb = new StringBuilder("***********\n" + "From megaquery we have several matches: " + n);
                        for (WifiSpot spot : spots) {
//                            sb.append(spot.toString() + "\n");
                            sb.append(spot.summarizeWithWeacon() + "\n");
//                            registerHitSSID(spot); todo,
                            WeaconParse we = spot.getWeacon();

                            weaconHashSet.add(we);
                        }
                        myLog.add(sb.toString(), "WE");
                    }
                    myLog.add("Detected spots: " + spots.size() + " | Different weacons: " + weaconHashSet.size(), "LIM");
                    myLog.add(" " + WeaconParse.Listar(weaconHashSet), "LIM");

                    LogInManagement.setNewWeacons(weaconHashSet);

                } else {
                    myLog.add("EEE en Chechkspotmarches:" + e, "aut");
                }
            }
        });
    }
}
