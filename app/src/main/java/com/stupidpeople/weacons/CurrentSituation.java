package com.stupidpeople.weacons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 04/03/2016.
 */
public class CurrentSituation {
    private final HashMap<WeaconParse, Integer> occurrences;
    private final HashSet<WeaconParse> weacons;
    public int nFetchings;
    public boolean shouldFetch;

    public CurrentSituation(HashSet<WeaconParse> weaconsDetected, HashMap<WeaconParse, Integer> occurrences) {
        this.occurrences = occurrences;
        this.weacons = weaconsDetected;
        nFetchings = nFetchings();
        shouldFetch = shouldFetch();
    }

    /**
     * Indicates if should fetch. The criteria is "if has been active by more than n scanners, then no.
     *
     * @param weacons
     * @return
     */
    private boolean shouldFetch() {
        boolean res = false;

        Iterator<WeaconParse> it = weacons.iterator();
        while (it.hasNext() && !res) {
            WeaconParse we = it.next();
            if (we.notificationRequiresFetching() && occurrences.get(we) < parameters.repetitionsTurnOffFetching) {//avoid keep fetching if you live near a bus stop
                res = true;
//                myLog.add(we.getName() + " requires feticn. this is the " + occurrences.get(we) + "time", tag);
            }
        }
        return res;
    }

    public boolean anyFetchable() {
        return nFetchings > 0;
    }

    private int nFetchings() {
        int i = 0;
        for (WeaconParse we : weacons) {
            if (we.notificationRequiresFetching()) i++;
        }
        return i;
    }

}
