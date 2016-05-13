package com.stupidpeople.weacons;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.stupidpeople.weacons.Advanced.Chat;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    private static HashMap<WeaconParse, Integer> occurrences = new HashMap<>();
    private static boolean someOneAppearing = false;
    private static boolean someoneQuitting = false;
    private static ArrayList<WeaconParse> weaconsToNotify = new ArrayList<>();//Will be notified
    private static boolean anyInterestingAppearing = false;
    private static ArrayList<WeaconParse> activeWeacons;


    /**
     * Informs the weacons detected, in order to send/update/remove  notification
     * and log in /out in the chat
     */
    public static void setNewWeacons(HashSet<WeaconParse> weaconsDetected, Context ctx, LogBump logBump) {


        lastWeaconsDetected = weaconsDetected;
        someoneQuitting = false;
        someOneAppearing = false;

        try {

            //Check differences with last scanning and keep accumulation history
            checkDisappearing(ctx, logBump);
            checkAppearing();

            now = new CurrentSituation(weaconsDetected, occurrences);
            boolean shouldFetch = now.anyFetchable() && now.anyInteresting;

            logBump.setOccurrences(occurrences);
            logBump.setSituation(now);

            //Notify or change notification
            if (someOneAppearing || someoneQuitting) {
                if (someOneAppearing) logBump.setReasonToNotify(LogBump.ReasonToNotify.APPEARING);
                if (someoneQuitting)
                    logBump.setReasonToNotify(LogBump.ReasonToNotify.DISSAPIRARING);

                if (shouldFetch) fetchAllActiveAndInform(ctx, logBump);
                else Inform(ctx, logBump);

            } else {
                logBump.setReasonToNotify(LogBump.ReasonToNotify.NONE);
                logBump.build();
            }

            if (parameters.isMilenkosPhone()) Notifications.notifyOccurrences(occurrences);

        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
    }

    public static void fetchAllActiveAndInform(final Context context, final LogBump logBump) {
        markAsRefreshing(true);
        Inform(context, logBump);
        markAsRefreshing(false);

        final MultiTaskCompleted multiTaskCompleted = new MultiTaskCompleted() {
            @Override
            public void OneTaskCompleted() {
                Inform(context, logBump);
            }

            @Override
            public void OnError(Exception e) {
                myLog.error(e);
            }
        };
        final int nTotal = getActiveWeacons().size();

        MultiTaskCompleted singleFetch = new MultiTaskCompleted() {
            int iTasksCompleted = 0;

            @Override
            public void OneTaskCompleted() {
                iTasksCompleted++;
                if (iTasksCompleted == nTotal) {
                    allHaveBeenFetched(context, null);
                    multiTaskCompleted.OneTaskCompleted();
                }
            }

            @Override
            public void OnError(Exception e) {
                iTasksCompleted++;
                if (iTasksCompleted == nTotal) multiTaskCompleted.OneTaskCompleted();
            }
        };

        for (final WeaconParse we : activeWeacons) {
            if (we.notificationRequiresFetching()) {
                we.fetchForNotification(singleFetch);
            } else {
                singleFetch.OneTaskCompleted();
            }
        }
    }

    private static void Inform(Context context, LogBump logBump) {
        // Notification
        Notify(logBump);

        //WeaconsList
        context.sendBroadcast(new Intent(parameters.NEW_WEACONS_DATA));
    }

    public static ArrayList<WeaconParse> getActiveWeacons() {
        activeWeacons = new ArrayList();
        if (occurrences != null) {
//            myLog.add("Active weacons are: " + WeaconParse.Listar(occurrences), "aut");
            activeWeacons = new ArrayList(occurrences.keySet());
        } else {
//            myLog.add("No tenemos weacons en occurrences", "aut");
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
     *
     * @param logBump
     */
    private static void checkDisappearing(Context ctx, LogBump logBump) {
        Iterator<Map.Entry<WeaconParse, Integer>> itOld = occurrences.entrySet().iterator();

        while (itOld.hasNext()) {
            Map.Entry<WeaconParse, Integer> entry = itOld.next();

            WeaconParse we = entry.getKey();
            if (!lastWeaconsDetected.contains(we)) {
                int n = entry.getValue();

                // +++ -
                if (n > 0) {
                    entry.setValue(-1);
                    logBump.addQuitting(entry.getKey().getName());
//                    myLog.add("just leaving " + we.getName(), tag);

                    // --- -
                } else {
                    entry.setValue(n - 1);

                    if (n < -parameters.repeatedOffToDisappear) {
                        itOld.remove();
                        Inform(ctx, logBump); //to remove from the message bar ("currently around...")
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

    private static void Notify(LogBump logBump) {
        boolean sound = anyInterestingAppearing && !now.anyHome;
        boolean automaticFetching = anyInterestingAppearing && !now.anyHome;
        boolean silenceButton = now.anyInteresting;
        boolean refreshButton = now.anyFetchable();

        String r = anyInterestingAppearing ? "Alguno nuevo interesante." : "Ninguno nuevo interesante";
        String s = now.anyHome ? "Alguno de estos es HOME" : "Ninguno es home";
        String t = now.anyInteresting ? "Hay alguno interesante" : "No hay ninguno interesante";
        String u = now.anyFetchable() ? "Hay alguno fetchable" : "No hay ninguno fetchable";


        logBump.setSound(sound, r + " Y " + s);
        logBump.setSilenceButton(silenceButton, t);
        logBump.setRefreshButton(refreshButton, u);
        logBump.setAutomaticFetching(automaticFetching, r + " Y " + s);


        Notifications.Notify(weaconsToNotify, numberOfActiveNonNotified(), sound, automaticFetching,
                refreshButton, silenceButton, logBump);

//
//        if (now.anyFetchable()) {
//            if (automaticFetching) {
//                NotifyFetching(sound, now.anyInteresting);
//            } else { //simply notify
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

    private static int numberOfActiveNonNotified() {
        int nactive = getActiveWeacons().size();
        int nnotified = weaconsToNotify.size();
        myLog.add("ACTVICE NON NOTIFIED: " + nactive + "-" + nnotified, "aut");

        return nactive - nnotified;
    }

    private static void allHaveBeenFetched(final Context ctx, final LogBump logBump) {
//        if (t != null) {
//            t.cancel();
//            t.purge();
//        }

        final Timer t = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                myLog.add("------han pasado los 30seg", LogBump.tag);
                informWeaconsObsolete(ctx, new LogBump(LogBump.LogType.OBSOLETE_REMOVAL));
                t.cancel();
            }
        };

        t.schedule(task, 30000);

    }

    private static void informWeaconsObsolete(Context ctx, LogBump logBump) {
        for (WeaconParse we : activeWeacons) we.setObsolete(true);
        Inform(ctx, logBump);
    }

    private static void markAsRefreshing(boolean b) {
        for (WeaconParse we : activeWeacons) {
            if (we.notificationRequiresFetching()) we.refreshing = b;
        }
    }

    /**
     * Created by Milenko on 04/03/2016.
     */
    public static class CurrentSituation {
        private static final int MILI_TO_BE_HOME = 40 * 60 * 1000;//30 * 60 * 1000;
        public boolean anyHome;
        public int nFetchings;
        //        public boolean shouldFetch;
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
                        if (timeDiff > MILI_TO_BE_HOME) {
                            we.setInHome(true); //TODO Cómo se deja de ser Home?
                            anyHome = true;
                        }
                    }


//                    // Should fetch
//                    if ((we.notificationRequiresFetching() &&
//                            repetitions < parameters.repetitionsTurnOffFetching) && //
//                            !we.inHome()) {/*avoid keep fetching if you live near a bus stop*/
//                        shouldFetch = true;
//                    }

                    //Interesting
                    if (we.isInteresting()) {
                        interestingOnes.add(we);
                        anyInteresting = true;
                    }

                    //home
                    if (we.inHome()) anyHome = true;//TODO remove home if has pass 3 months

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
                    ", interestingOnes=" + interestingOnes +
                    '}';

        }

        public boolean anyFetchable() {
            return nFetchings > 0;
        }

    }
}