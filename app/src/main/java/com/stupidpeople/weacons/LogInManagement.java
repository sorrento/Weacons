package com.stupidpeople.weacons;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.stupidpeople.weacons.Advanced.Chat;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import util.myLog;
import util.parameters;

import static com.stupidpeople.weacons.WeaconParse.Listar;

/**
 * Created by Milenko on 10/08/2015.
 * It manages the situation where the user approach or left a weacon.
 * It's responsible of notification, and login in/out the chat *
 */
public abstract class LogInManagement {
    private static final String tag = "LIM";
    public static HashSet<WeaconParse> lastWeaconsDetected;
    static CurrentSituation now;
    private static HashMap<WeaconParse, Integer> occurrences = new HashMap<>();
    //{we, n} n = times  appeared in a row. If negative, n of time not appearing consecutively since
    // last appeareance
    private static boolean anyChange = false;  //if entering or quitting a weacon in the last scanning. We may have some persistence
    private static boolean sound;//should the notification be silent?
    private static ArrayList<WeaconParse> weaconsToNotify = new ArrayList<>();//Will be notified

    private static boolean lastTimeWeFetched;


    /**
     * Informs the weacons detected, in order to send/update/remove  notification
     * and log in /out in the chat
     */
    public static void setNewWeacons(HashSet<WeaconParse> weaconsDetected) {

        lastWeaconsDetected = weaconsDetected;
        anyChange = false;
        sound = false;

        try {
            //Check differences with last scanning and keep accumulation history
            checkDisappearing();
            checkAppearing();

            now = new CurrentSituation(weaconsDetected, occurrences);

            myLog.add("Entering or quitting any Weacon (anychange)?" + anyChange + "\n anyfetchable?" + now.anyFetchable() +
                    "\n should fetch?" + now.shouldFetch + "\n Last time fetched " + lastTimeWeFetched +
                    "\n  --> Total= " + (!anyChange && now.anyFetchable() && !now.shouldFetch && lastTimeWeFetched), "MHP");

            //Notify or change notification
            if (anyChange || now.shouldFetch) {
                Notify();
            } else if (!anyChange && now.anyFetchable() && !now.shouldFetch && lastTimeWeFetched) {
                NotifyRemovingObsoleteInfo();
            }

            Notifications.notifyOccurrences(occurrences);

        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
    }

    /**
     * List of DISAPPEARING (NOT IN NEW). Modifies field occurrences
     */
    private static void checkDisappearing() {
        Iterator<Map.Entry<WeaconParse, Integer>> itOld = occurrences.entrySet().iterator();

        while (itOld.hasNext()) {
            Map.Entry<WeaconParse, Integer> entry = itOld.next();

            WeaconParse we = entry.getKey();
            if (!lastWeaconsDetected.contains(we)) {
                int n = entry.getValue();

                // +++ -
                if (n > 0) {
                    entry.setValue(-1);
                    myLog.add("just leaving " + we.getName(), tag);

                    // --- -
                } else {
                    entry.setValue(n - 1);

                    if (n < -parameters.repeatedOffToDisappear) {
                        myLog.add("Forget it, too far: " + we.getName(), tag);
                        itOld.remove();
                    } else if (n == -we.getRepeatedOffRemoveFromNotification() && IsInNotification(we)) {
                        movingOutForNotification(we); //remove from notification
                    } else if (n == -parameters.repeatedOffToChatOff && Chat.IsInChat(we)) {
                        Chat.movingOutForChat(we); // Log out from chat
                    }
                }
            }
        }
    }

    /**
     * List of APPEARING (YES IN NEW). Modifies the field occurrences.
     * For each new we, if not present or negative, we put it with 1.\n
     * If present, just add 1
     */
    private static void checkAppearing() {

        Iterator<WeaconParse> it = lastWeaconsDetected.iterator();
        while (it.hasNext()) {
            WeaconParse we = it.next();

            if (occurrences.containsKey(we)) {
                int n = occurrences.get(we);
                // +++ +
                if (n >= 0) {
                    occurrences.put(we, n + 1);
                    if (n == parameters.repeatedOnToChatOn && !Chat.IsInChat(we)) {
                        Chat.WeChatIn(we);
                    }
                    // ++-- +
                } else {
                    occurrences.put(we, 1);
                    myLog.add("Entering Again: " + we.getName(), tag);
                }
            } else {
                //First time
                movingInForNotification(we);
            }
        }
    }

    public static ArrayList<WeaconParse> getActiveWeacons() {
        ArrayList arr = new ArrayList(occurrences.keySet());
        return arr;
    }

    //Notifications

    private static void NotifyRemovingObsoleteInfo() {
        //removing last info
        myLog.add("Removing info of paradas (last feching) from everyweacon", tag);
        for (WeaconParse we : weaconsToNotify) we.setObsolete(true);
        Notifications.showNotification(weaconsToNotify, false, true);
        lastTimeWeFetched = false;
    }

    private static void movingInForNotification(WeaconParse we) {
        occurrences.put(we, 1);
        myLog.add("Just entering in: " + we.getName(), tag);
        anyChange = true;//Just appeared this weacon
        sound = true;
        weaconsToNotify.add(we);
    }

    private static void movingOutForNotification(WeaconParse we) {
        weaconsToNotify.remove(we);
        myLog.add("Remove from notification:" + we.getName(), tag);
        anyChange = true;
    }


    private static void Notify() {

        if (!now.anyFetchable()) {
            Notifications.showNotification(weaconsToNotify, sound, now.anyFetchable());
            lastTimeWeFetched = false;
        } else {
            MultiTaskCompleted listener = new MultiTaskCompleted() {
                int i = 0;

                @Override
                public void OneTaskCompleted() {
                    i += 1;
                    myLog.add("terminada ina task=" + i + "/" + now.nFetchings, "MHP");

                    if (i == now.nFetchings) {
                        myLog.add("a lazanr la notificaicno congunta", tag);
                        lastTimeWeFetched = true;
                        Notifications.showNotification(weaconsToNotify, sound, now.anyFetchable());
                    }
                }

                @Override
                public void OnError(Exception e) {
                    i++;
                    myLog.add("Teminada una tarea, pero con error " + i + "/" + now.nFetchings, "MHP");
                    myLog.add(Log.getStackTraceString(e), "err");
                }
            };

            for (final WeaconParse we : weaconsToNotify) {
                if (we.notificationRequiresFetching()) {
                    we.fetchForNotification(listener);

                }
            }
        }
    }
/////////////////


    //NOTIFICATIONS
    private static boolean IsInNotification(WeaconParse we) {
        return weaconsToNotify.contains(we);
    }


    public static void refresh(Context ctx) {
        myLog.add("refresing the notification", tag);
        Toast.makeText(ctx, "Refreshing notification...", Toast.LENGTH_SHORT).show();
        NotifyForcingFetching();
    }


    private static void NotifyForcingFetching() {

        // Put zero en occurrences
        try {
            for (WeaconParse we : occurrences.keySet()) {
                occurrences.put(we, 0);
            }
        } catch (Exception e) {
            myLog.error(e);
        }

        Notify();
    }


    /**
     * Created by Milenko on 04/03/2016.
     */
    public static class CurrentSituation {
        private final HashMap<WeaconParse, Integer> occurrences;
        private final HashSet<WeaconParse> weacons;
        public int nFetchings;
        public boolean shouldFetch;

        public CurrentSituation(HashSet<WeaconParse> weaconsDetected, HashMap<WeaconParse, Integer> occurrences) {
            this.occurrences = occurrences;
            this.weacons = weaconsDetected;
            try {
                nFetchings = countFetchingWeacons();
                shouldFetch = shouldFetch();

                myLog.add("conta: " + Listar(occurrences), "MHP");

            } catch (Exception e) {
                myLog.error(e);
            }
        }

        /**
         * Indicates if should fetch. The criteria is "if has been active by more than n scanners, then no.
         *
         * @param
         * @return
         */
        private boolean shouldFetch() {
            boolean res = false;

            if (anyFetchable()) {
                Iterator<WeaconParse> it = weaconsToNotify.iterator();
                while (it.hasNext() && !res) {
                    WeaconParse we = it.next();
                    if ((we.notificationRequiresFetching() && occurrences.get(we) < parameters.repetitionsTurnOffFetching) ||
                            we.forceFetching) {//avoid keep fetching if you live near a bus stop
                        res = true;
                        //                myLog.add(we.getName() + " requires feticn. this is the " + occurrences.get(we) + "time", tag);
                    }
                }
            }

            return res;
        }

        public boolean anyFetchable() {
            return nFetchings > 0;
        }

        private int countFetchingWeacons() {
            int i = 0;

            for (WeaconParse we : weaconsToNotify) {
                if (we.notificationRequiresFetching()) i++;
            }
            return i;
        }

    }
}