package com.stupidpeople.weacons;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.stupidpeople.weacons.Advanced.Chat;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;

import java.util.ArrayList;
import java.util.Date;
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
    // last appearance
    //    public static boolean anyChange = false;  //if entering or quitting a weacon in the last scanning. We may have some persistence
    public static CurrentSituation now;
    static boolean newAppearance = false;
    private static HashMap<WeaconParse, Integer> occurrences = new HashMap<>();
    //    private static boolean sound;//should the notification be silent?
    private static ArrayList<WeaconParse> weaconsToNotify = new ArrayList<>();//Will be notified
    private static boolean lastTimeWeFetched;
    private static boolean someoneQuitting = false;
    private static boolean anyInterestingAppearing = false;


    /**
     * Informs the weacons detected, in order to send/update/remove  notification
     * and log in /out in the chat
     */
    public static void setNewWeacons(HashSet<WeaconParse> weaconsDetected) {

        lastWeaconsDetected = weaconsDetected;
        someoneQuitting = false;
        newAppearance = false;

        try {
            //Check differences with last scanning and keep accumulation history
            checkDisappearing();
            checkAppearing();

            now = new CurrentSituation(weaconsDetected, occurrences);

            myLog.add("Entering:" + newAppearance + " | quitting: " + someoneQuitting + "\n anyfetchable?" + now.anyFetchable() +
                    "\n should fetch?" + now.shouldFetch + "\n Last time fetched " + lastTimeWeFetched
                    + " anyhome" + now.anyHome, tag);

            //Notify or change notification
            if (newAppearance || someoneQuitting) {
                Notify();
            }

            if (isMilenkosPhone()) Notifications.notifyOccurrences(occurrences);

        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
    }

    private static boolean isMilenkosPhone() {
        String model = Build.MODEL;
        return model.equals("GT-I9505");//Samsung S4
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
                        itOld.remove();
                        Notify(); //to remove from the message bar ("currently around...")
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
        anyInterestingAppearing = false;

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
                if (!anyInterestingAppearing) {
                    anyInterestingAppearing = ParseActions.isInteresting(we.getObjectId());
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
        newAppearance = true;
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
        boolean sound = anyInterestingAppearing && !now.anyHome;
        boolean automaticFetching = anyInterestingAppearing && !now.anyHome;
        boolean silenceButton = now.anyInteresting;


        if (now.anyFetchable()) {
            if (automaticFetching) {
                NotifyMultipleFetching(sound, now.anyInteresting);
            } else { //simply notify
                Notifications.showNotification(weaconsToNotify, anyInterestingAppearing, true, now.anyInteresting);
                lastTimeWeFetched = false;
            }

        } else {
            Notifications.showNotification(weaconsToNotify, anyInterestingAppearing, false, now.anyInteresting);
            lastTimeWeFetched = false;

        }

    }

    public static void NotifyMultipleFetching(final boolean sound, final boolean anyInteresting) {
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
                    Notifications.showNotification(weaconsToNotify, sound, true, anyInteresting);
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

/////////////////


    //NOTIFICATIONS
    private static boolean IsInNotification(WeaconParse we) {
        return weaconsToNotify.contains(we);
    }

    public static void refresh(Context ctx) {
        myLog.add("Refreshing the notification", tag);
        Toast.makeText(ctx, R.string.refreshing_notif, Toast.LENGTH_SHORT).show();
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
        NotifyMultipleFetching(false, true);
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
        public boolean anyHome;
        public int nFetchings;
        public boolean shouldFetch;
        public boolean anyInteresting;
        private ArrayList<WeaconParse> interestingOnes = new ArrayList();

        public CurrentSituation(HashSet<WeaconParse> weaconsDetected, HashMap<WeaconParse, Integer> occurrences) {

            int i = 0;

            try {
                for (WeaconParse we : weaconsDetected) {
                    int repetitions = occurrences.get(we);

                    //Count
                    if (we.notificationRequiresFetching()) i++;

                    //Check if at home
                    if (repetitions == 1) {
                        we.setTimeFirstApperaringInThisRow(new Date());
                    } else if (repetitions > 15 && !we.inHome()) {
                        long timeDiff = new Date().getTime() - we.getTimeFirstApperaringInThisRow().getTime();
                        if (timeDiff > 30 * 60 * 1000) { //Half an hour
                            we.setInHome(true); //TODO Cómo se deja de ser Home?
                            anyHome = true;
                        }
                    }


                    // Should fetch
                    if ((we.notificationRequiresFetching() &&
                            repetitions < parameters.repetitionsTurnOffFetching) && //
                            !we.inHome()) {/*avoid keep fetching if you live near a bus stop*/
                        shouldFetch = true;
                    }

                    //Interesting
                    if (we.isInteresting()) {
                        interestingOnes.add(we);
                        anyInteresting = true;
                    }

                }

                nFetchings = i;
                if (interestingOnes.size() > 0)
                    myLog.add("These are interesting: " + WeaconParse.Listar(interestingOnes), "NOTI");
            } catch (Exception e) {
                myLog.error(e);
            }
        }

        public boolean anyFetchable() {
            return nFetchings > 0;
        }

    }
}