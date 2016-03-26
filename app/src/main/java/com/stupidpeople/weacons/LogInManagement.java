package com.stupidpeople.weacons;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.stupidpeople.weacons.Advanced.Chat;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;

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
public class LogInManagement {
    private static final String tag = "LIM";
    public static HashSet<WeaconParse> lastWeaconsDetected;
    //{we, n} n = times  appeared in a row. If negative, n of time not appearing consecutively since
    // last appeareance
    //    public static boolean anyChange = false;  //if entering or quitting a weacon in the last scanning. We may have some persistence
    public static CurrentSituation now;
    static boolean newAppearence = false;
    private static HashMap<WeaconParse, Integer> occurrences = new HashMap<>();
    //    private static boolean sound;//should the notification be silent?
    private static ArrayList<WeaconParse> weaconsToNotify = new ArrayList<>();//Will be notified
    private static boolean lastTimeWeFetched;
    private static boolean someoneQuitting = false;
    private static boolean anyInterestinAppearing = false;


    /**
     * Informs the weacons detected, in order to send/update/remove  notification
     * and log in /out in the chat
     */
    public static void setNewWeacons(HashSet<WeaconParse> weaconsDetected) {

        lastWeaconsDetected = weaconsDetected;
        someoneQuitting = false;
        newAppearence = false;

        try {
            //Check differences with last scanning and keep accumulation history
            checkDisappearing();
            checkAppearing();

            now = new CurrentSituation(weaconsDetected, occurrences);

            myLog.add("Entering:" + newAppearence + " | quitting: " + someoneQuitting + "\n anyfetchable?" + now.anyFetchable() +
                    "\n should fetch?" + now.shouldFetch + "\n Last time fetched " + lastTimeWeFetched, "MHP");

            //Notify or change notification
            if (newAppearence || someoneQuitting) {
                Notify();
//            } else if (now.anyFetchable() && !now.shouldFetch && lastTimeWeFetched) {
//                NotifyRemovingObsoleteInfo();
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
        anyInterestinAppearing = false;

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
                if (!anyInterestinAppearing) {
                    anyInterestinAppearing = ParseActions.isInteresting(we.getObjectId());
                }
                movingInForNotification(we);
            }
        }
    }

    public static ArrayList<WeaconParse> getActiveWeacons() {
        ArrayList arr = new ArrayList();
        if (occurrences != null) {
//            myLog.add("Active weacons are: " + WeaconParse.Listar(occurrences), "aut");
            arr = new ArrayList(occurrences.keySet());
        } else {
//            myLog.add("No tenemos weacons en occurrences", "aut");
        }

        return arr;
    }

    //Notifications

    static void NotifyRemovingObsoleteInfo() {
        //removing last info
        myLog.add("Removing info of paradas (last feching) from everyweacon", tag);
        for (WeaconParse we : weaconsToNotify) {
            Notifications.obsolete = true;
            we.setObsolete(true);
        }
        Notifications.showNotification(weaconsToNotify, false, true, now.anyInteresting);
        lastTimeWeFetched = false;
    }

    private static void movingInForNotification(WeaconParse we) {
        occurrences.put(we, 1);
        myLog.add("Just entering in: " + we.getName(), tag);
        newAppearence = true;
//        anyChange = true;//Just appeared this weacon
        weaconsToNotify.add(we);
    }

    private static void movingOutForNotification(WeaconParse we) {
        someoneQuitting = true;
        weaconsToNotify.remove(we);
        myLog.add("Remove from notification:" + we.getName(), tag);
//        anyChange = true;
    }


    private static void Notify() {
        if (!now.anyFetchable()) {
            Notifications.showNotification(weaconsToNotify, anyInterestinAppearing, false, now.anyInteresting);
            lastTimeWeFetched = false;
        } else {
            if (!now.anyInteresting) {
                Notifications.showNotification(weaconsToNotify, anyInterestinAppearing, true, now.anyInteresting);
                lastTimeWeFetched = false;
                return;
            }

            MultiTaskCompleted listener = new MultiTaskCompleted() {
                int i = 0;

                @Override
                public void OneTaskCompleted() {
                    i += 1;
                    myLog.add("terminada ina task=" + i + "/" + now.nFetchings, "MHP");

                    if (i == now.nFetchings) {
                        myLog.add("a lazanr la notificaicno congunta", tag);
                        lastTimeWeFetched = true;
                        Notifications.obsolete = false;
                        Notifications.showNotification(weaconsToNotify, anyInterestinAppearing, true, now.anyInteresting);
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

    public static ArrayList<WeaconParse> getNotifiedWeacons() {
        return weaconsToNotify;
    }

    public static String bottomMessage(Context mContext) {
        int n = numberOfActiveNonNotified();
        String summary = n > 1 ? mContext.getString(R.string.currently_active) : mContext.getString(R.string.currently_active_one);
        return String.format(summary, n);
    }

    /**
     * indicates if there are wecons active that are not present in the notification
     *
     * @return
     */
    public static boolean othersActive() {
        return numberOfActiveNonNotified() > 0;

    }

    private static int numberOfActiveNonNotified() {
        return getActiveWeacons().size() - getNotifiedWeacons().size();
    }


    /**
     * Created by Milenko on 04/03/2016.
     */
    public static class CurrentSituation {
        public int nFetchings;
        public boolean shouldFetch;
        public boolean anyInteresting;
        private ArrayList<WeaconParse> interestings = new ArrayList();

        public CurrentSituation(HashSet<WeaconParse> weaconsDetected, HashMap<WeaconParse, Integer> occurrences) {

            int i = 0;

            try {
                for (WeaconParse we : weaconsDetected) {
                    //Count
                    if (we.notificationRequiresFetching()) i++;

                    // Should fetch
                    if ((we.notificationRequiresFetching() && occurrences.get(we) < parameters.repetitionsTurnOffFetching) ||
                            we.forceFetching) {/*avoid keep fetching if you live near a bus stop*/
                        shouldFetch = true;
                    }

                    //Interesting
                    if (we.isInteresting()) {
                        interestings.add(we);
                        anyInteresting = true;
                    }

                }

                nFetchings = i;

                myLog.add("Inters: " + WeaconParse.Listar(interestings), "NOTI");
            } catch (Exception e) {
                myLog.error(e);
            }
        }

        public boolean anyFetchable() {
            return nFetchings > 0;
        }

    }
}