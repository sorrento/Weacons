package com.stupidpeople.weacons;

import android.content.Context;
import android.content.Intent;

import com.stupidpeople.weacons.Advanced.Chat;
import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Notifications.NotifFeatures;
import com.stupidpeople.weacons.Notifications.Notifications;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 10/08/2015.
 * It manages the situation where the user approach or left a weacon.
 * It's responsible of notification, and login in/out the chat *
 */
public class LogInManagement {
    private static final String tag = "LIM";

    public static CurrentSituation now;
    public static HashSet<WeaconParse> lastWeaconsDetected;
    //{we, n} n = times  appeared in a row. If negative, n of time not appearing consecutively since
    public static HashMap<WeaconParse, Integer> occurrences = new HashMap<>();
    public static ArrayList<WeaconParse> weaconsToNotify = new ArrayList<>();//Will be notified
    public static NotifFeatures notifFeatures;
    private static boolean areObsolete;
    private static boolean anyInterestingAppearing = false;
    private static ArrayList<WeaconParse> activeWeacons;
    private static boolean someOneAppearing = false;
    private static boolean someoneQuitting = false;
    private static boolean fetching;
    private static Timer t;

    private static HashMap<WeaconParse, Integer> sortedOccurrences = new HashMap<>();

    public static HashMap<WeaconParse, Integer> getSortedOccurrences() {
        return sortedOccurrences;
    }

    private static void setSortedOccurrences(HashMap<WeaconParse, Integer> occurrences) {
        sortedOccurrences = sortByValue(occurrences);
    }

    /**
     * Informs the weacons detected, in order to send/update/remove  notification
     * and log in /out in the chat
     */
    public static void setNewWeacons(HashSet<WeaconParse> weaconsDetected, Context ctx) {

        lastWeaconsDetected = weaconsDetected;
        someoneQuitting = false;
        someOneAppearing = false;

        try {

            //Check differences with last scanning and keep accumulation history
            checkDisappearing();
            checkAppearing();

            setSortedOccurrences(occurrences);

            now = new CurrentSituation(weaconsDetected, occurrences);

            decider(ctx);

            if (parameters.isMilenkosPhone()) Notifications.notifyOccurrences(occurrences);

        } catch (Exception e) {
            myLog.error(e);
        }
    }

    private static <K, V> HashMap<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
            }
        });

        HashMap<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }


        return result;
    }

    /**
     * Decide if updateInfo or not and the features of the notification
     *
     * @param ctx
     */
    private static void decider(Context ctx) {

        if (someOneAppearing || someoneQuitting) {

            boolean sound = anyInterestingAppearing && !now.anyHome;
            boolean refreshBtn = now.anyFetchable();
            boolean silenceBtn = now.anyInteresting;

            notifFeatures = new NotifFeatures(sound, refreshBtn, silenceBtn);

            //Notify or change notification
            final Intent intent = new Intent(parameters.updateInfo);
            intent.putExtra("anyHome", now.anyHome);
            String reason = someOneAppearing ? " appearing" : " disappearing";
            myLog.addToParse("BROADCAST UPDATE porque" + reason, "BROAD");
            ctx.sendBroadcast(intent);
//            notifySelective(parameters.everybody, ctx);
        }
    }

    public static void fetchAllActiveAndInform(final Context ctx, boolean forceTheFetching, boolean onlyNotifiedWeacons) {

        if (fetching) {
            if (forceTheFetching) cancelTimer();
            else return; // To avoid useless fetching
        }

        fetching = true;

        // Show "refreshing"
        informWeaconsRefreshing(ctx, onlyNotifiedWeacons);

        final MultiTaskCompleted allFetched = new MultiTaskCompleted() {
            @Override
            public void OneTaskCompleted() {
                clearAfterATime(ctx, 30000);
                //show with fetched info
                myLog.addToParse("BROADCAST UPDATE porque" + " fetchiamos todos ya", "BROAD");
                ctx.sendBroadcast(new Intent(parameters.updateInfo));
            }

            @Override
            public void OnError(Exception e) {
                myLog.error(e);
            }
        };

        // Fecth only notified to save time waiting
        ArrayList<WeaconParse> weaconsToFetch = onlyNotifiedWeacons ?
                Notifications.getNotifiedWeacons() : activeWeacons;

        final int nTotal = weaconsToFetch.size();

        MultiTaskCompleted singleFetch = new MultiTaskCompleted() {
            int iTasksCompleted = 0;

            @Override
            public void OneTaskCompleted() {
                iTasksCompleted++;
                myLog.add("Taskes competes=" + iTasksCompleted + "/" + nTotal, "aut");
                if (iTasksCompleted == nTotal) allFetched.OneTaskCompleted();
            }

            @Override
            public void OnError(Exception e) {
                iTasksCompleted++;
                if (iTasksCompleted == nTotal) allFetched.OneTaskCompleted();
            }
        };


        for (final WeaconParse we : weaconsToFetch) {
            if (we.notificationRequiresFetching()) {
                we.fetchForNotification(singleFetch);
            } else {
                singleFetch.OneTaskCompleted();
            }
        }
    }

    public static ArrayList<WeaconParse> getActiveWeacons() {
        activeWeacons = new ArrayList();
        if (occurrences != null) {
//            myLog.add("Active weacons are: " + WeaconParse.Listar(occurrences), "aut");
//            activeWeacons = new ArrayList(occurrences.keySet());
            activeWeacons = new ArrayList(getSortedOccurrences().keySet());

            Collections.reverse(activeWeacons);
        }
        return activeWeacons;
    }

    /**
     * indicates if there are wecons active that are not present in the notification
     *
     * @return
     */
    public static boolean othersActive() {
        return numberOfActiveNonNotified() > 0;
    }

    //private

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

                    // --- -
                } else {
                    entry.setValue(n - 1);

                    if (n < -parameters.repeatedOffToDisappear) {
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

    private static void movingInForNotification(WeaconParse we) {
        occurrences.put(we, 1);
        myLog.add("Just entering in: " + we.getName(), tag);
        someOneAppearing = true;
//        anyChange = true;//Just appeared this weacon
//        weaconsToNotify.add(we);
        weaconsToNotify.add(0, we);
    }

    private static void movingOutForNotification(WeaconParse we) {
        someoneQuitting = true;
        weaconsToNotify.remove(we);
        myLog.add("Remove from notification:" + we.getName(), tag);
//        anyChange = true;
    }


    //NOTIFICATIONS

    private static void Notify() {
        boolean sound = anyInterestingAppearing && !now.anyHome;
        boolean automaticFetching = anyInterestingAppearing && !now.anyHome;
        boolean silenceButton = now.anyInteresting;
        boolean refreshButton = now.anyFetchable();

        String r = anyInterestingAppearing ? "Alguno nuevo interesante." : "Ninguno nuevo interesante";
        String s = now.anyHome ? "Alguno de estos es HOME" : "Ninguno es home";
        String t = now.anyInteresting ? "Hay alguno interesante" : "No hay ninguno interesante";
        String u = now.anyFetchable() ? "Hay alguno fetchable" : "No hay ninguno fetchable";


//        logBump.setSound(sound, r + " Y " + s);
//        logBump.setSilenceButton(silenceButton, t);
//        logBump.setRefreshButton(refreshButton, u);
//        logBump.setAutomaticFetching(automaticFetching, r + " Y " + s);


//        Notifications.Notify(weaconsToNotify, numberOfActiveNonNotified(), sound, automaticFetching,
//                refreshButton, silenceButton);

//
//        if (now.anyFetchable()) {
//            if (automaticFetching) {
//                NotifyFetching(sound, now.anyInteresting);
//            } else { //simply updateInfo
//                Notifications.showNotification(weaconsToNotify, anyInterestingAppearing, true, now.anyInteresting);
////                lastTimeWeFetched = false;
//            }
//
//        } else {
//            Notifications.showNotification(weaconsToNotify, anyInterestingAppearing, false, now.anyInteresting);
////            lastTimeWeFetched = false;
//
//        }

    }

    private static boolean IsInNotification(WeaconParse we) {
        return weaconsToNotify.contains(we);
    }

    public static int numberOfActiveNonNotified() {
        int nactive = getActiveWeacons().size();
        int nnotified = weaconsToNotify.size();

        return nactive - nnotified;
    }

    private static void clearAfterATime(final Context ctx, int delay) {

        cancelTimer();
        t = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                fetching = false;
                myLog.add("Ha acabado el tiempo, a informar como obsoleto", "aut");
                informWeaconsObsolete(ctx);
            }
        };

        t.schedule(task, delay);
    }

    private static void cancelTimer() {
        if (t != null) {
            t.cancel();
            t.purge();
            t = null;
        }
    }

    private static void informWeaconsObsolete(Context ctx) {
        areObsolete = true;
        for (WeaconParse we : activeWeacons) {
            //No poner como obsoletos los de larga duración como restaurant o schedule TODO
//            HelperBaseFecthNotif helperFet = (HelperBaseFecthNotif) we.mHelper;
//            long minutes = helperFet.fetchedDataIsValidDuringMinutes();
//            if (b && minutes > 1) {
//                long timeDiff = new Date().getTime() - helperFet.lastUpdateTime.getTime();
//                ;
//                if (timeDiff < 1000 * 60 * minutes) return;
//            }
//
            we.setObsolete(true);
        }
        myLog.addToParse("BROADCAST UPDATE porque" + " los tiempos son obsoletos", "BROAD");
        ctx.sendBroadcast(new Intent(parameters.updateInfo));
    }

    private static void informWeaconsRefreshing(Context ctx, boolean onlyNotifiedWeacons) {
        ArrayList<WeaconParse> weacons = onlyNotifiedWeacons ? Notifications.getNotifiedWeacons() : activeWeacons;

        for (WeaconParse we : weacons) we.refreshing = true;
        myLog.addToParse("BROADCAST UPDATE porque" + " estamos mostrando  ACTUALIZANDO", "BROAD");
        ctx.sendBroadcast(new Intent(parameters.updateInfo));
    }


//    public static void markAsRefreshing(boolean b) {
//        for (WeaconParse we : getActiveWeacons()) {
//            if (we.notificationRequiresFetching()) we.refreshing = b;
//        }
//    }
//    public static void markAsObsolete(boolean b) {
//        for (WeaconParse we : getActiveWeacons()) {
//            if (we.notificationRequiresFetching()) we.setObsolete(b);
//        }
//    }

    /**
     * Created by Milenko on 04/03/2016.
     */
    public static class CurrentSituation {
        private static final int MILI_TO_BE_HOME = 40 * 60 * 1000;//30 * 60 * 1000;
        private final HashSet<WeaconParse> mDetected;
        private final HashMap<WeaconParse, Integer> mOccurences;
        public boolean anyHome;
        public int nFetchings;
        public boolean anyInteresting;


        public CurrentSituation(HashSet<WeaconParse> weaconsDetected, HashMap<WeaconParse, Integer> occurrences) {

            mOccurences = occurrences;
            mDetected = weaconsDetected;

            update();
        }

        public void update() {
            int i = 0;
            try {
                for (WeaconParse we : mDetected) {
                    int repetitions = mOccurences.get(we);

                    //Count
                    if (we.notificationRequiresFetching()) i++;

                    //Check if at home
                    if (repetitions == 1) {
                        we.setTimeFirstApperaringInThisRow(new Date());
                    } else if (repetitions > 15 && !we.inHome()) {
                        long timeDiff = new Date().getTime() - we.getTimeFirstApperaringInThisRow().getTime();
                        if (timeDiff > MILI_TO_BE_HOME) {
                            we.setInHome(true);
                            anyHome = true;
                        }
                    }
                    if (we.inHome()) anyHome = true;//TODO remove home if has pass 3 months


//                    // Should fetch
//                    if ((we.notificationRequiresFetching() &&
//                            repetitions < parameters.repetitionsTurnOffFetching) && //
//                            !we.inHome()) {/*avoid keep fetching if you live near a bus stop*/
//                        shouldFetch = true;
//                    }

                    //Interesting
                    if (we.isInteresting()) anyInteresting = true;

                }

                nFetchings = i;
            } catch (Exception e) {
                myLog.error(e);
            }
        }

        @Override
        public String toString() {
            return "CurrentSituation{" +
                    "anyHome=" + anyHome +
                    ", nFetchings=" + nFetchings +
//                    ", shouldFetch=" + shouldFetch +
                    ", anyInteresting=" + anyInteresting +
                    '}';

        }

        public boolean anyFetchable() {
            return nFetchings > 0;
        }

    }

}