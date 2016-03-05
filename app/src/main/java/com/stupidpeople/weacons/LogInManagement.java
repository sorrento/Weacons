package com.stupidpeople.weacons;

import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 10/08/2015.
 * It manages the situation where the user approach or left a weacon.
 * It's responsible of notification, and login in/out the chat *
 */
public abstract class LogInManagement {
    private static final String tag = "LIM";

    static CurrentSituation now;

    public static HashSet<WeaconParse> lastWeaconsDetected;
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
     *
     * @param weaconsDetected
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

            myLog.add("conta: " + WeaconParse.Listar(occurrences), tag);
            myLog.add("Entering or quitting any We?" + anyChange + "| anyfetchable?" + now.anyFetchable() + "| should fetch?" + now.shouldFetch, tag);

            //Notify or change notification
            if (anyChange || (now.anyFetchable() && now.shouldFetch)) {
                Notify();
            } else if (!anyChange && now.anyFetchable() && !now.shouldFetch && lastTimeWeFetched) {//TODO esta lógica es  un poco "confusa"
                NotifyRemovingObsoleteInfo();
            }

            Notifications.notifyOccurrences(occurrences);
            myLog.add("****************************\n", tag);

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
                movingInForNotificaion(we);
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
        for (WeaconParse we : weaconsToNotify) {
            we.resetFetchingResults();
        }

        Notifications.showNotification(weaconsToNotify, false, true);
        lastTimeWeFetched = false;
    }

    private static void Notify() {
        myLog.add("Will Notify: " + WeaconParse.Listar(weaconsToNotify), tag);
        myLog.add("**Vamos a notificar. Se requiere fetch:" + now.anyFetchable(), tag);

        if (!now.anyFetchable()) {
            Notifications.showNotification(weaconsToNotify, sound, now.anyFetchable());
            lastTimeWeFetched = false;
        } else {
            MultiTaskCompleted listener = new MultiTaskCompleted() {
                int i = 0;

                @Override
                public void OneTaskCompleted() {
                    i += 1;
                    myLog.add("terminada ina task=" + 1 + "/" + now.nFetchings, tag);

                    if (i == nFetchings) {
                        myLog.add("a lazanr la notificaicno congunta", tag);
                        Notifications.showNotification(weaconsToNotify, sound, now.anyFetchable());
                        lastTimeWeFetched = true;
                    }
                }

                @Override
                public void OnError(Exception e) {
                    myLog.add(Log.getStackTraceString(e), "err");
                }
            };

            for (final WeaconParse we : weaconsToNotify) {
                if (we.notificationRequiresFetching()) {
                    if (we.getType().equals("bus_stop")) {
                        if (we.near(parameters.stCugat, 20)) {
                            (new fetchParadaStCugat(listener, we)).execute();
                        } else if (we.near(parameters.santiago, 20)) {
                            (new fetchParadaSantiago(listener, we)).execute();
                        }
                    }
                }
            }
        }
    }


    //NOTIFICATIONS
    private static boolean IsInNotification(WeaconParse we) {
        return weaconsToNotify.contains(we);
    }

    private static void movingInForNotificaion(WeaconParse we) {
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

    public static void refresh() {
        myLog.add("refresing the notification", tag);
        Notify();
    }


}