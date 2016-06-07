package com.stupidpeople.weacons;

import android.net.wifi.ScanResult;

import com.stupidpeople.weacons.ready.ParseActions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 03/05/2016.
 */

public class LogBump {
    private static final String DIVIDER = "********************************************\n";
    public static String tag = "NOTIF";
    private final LogType mLogType;
    private final String mTime;
    private List<ScanResult> spots;
    private List<WifiSpot> wifiSpots;
    private HashMap<WeaconParse, ArrayList<String>> weaconsHash;
    private HashMap<WeaconParse, Integer> occurrences;
    private LogInManagement.CurrentSituation situation;
    private ArrayList<String> quitting = new ArrayList<>();
    private ArrayList<ReasonToNotify> reasonsToNotify = new ArrayList<>();
    private boolean mSound;
    private String mSoundReason = "";
    private String mSilenceButtonReason = "";
    private boolean mSilenceButton;
    private String mRefreshButtonReason = "";
    private boolean mRefreshButton;
    private String mAutomaticFetchingReason = "";
    private boolean mAutomaticFetching;
    private String notificationText;

    public LogBump(LogType LogType) {
        mLogType = LogType;
        mTime = new SimpleDateFormat("MMdd_HHmmss").format(new Date());

    }

    public String getNotificationText() {
        return notificationText == null ? "" : notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public void setOccurrences(HashMap<WeaconParse, Integer> occurrences) {
        this.occurrences = occurrences;
    }

    //setters

    public void setSituation(LogInManagement.CurrentSituation situation) {
        this.situation = situation;
    }

    public void addQuitting(String name) {
        quitting.add(name);
    }

    public void setWeaconsHash(HashMap<WeaconParse, ArrayList<String>> weaconsHash) {
        this.weaconsHash = weaconsHash;
    }

    public void setSpots(List<ScanResult> spots) {
        this.spots = spots;
    }

    public void setWifiSpots(List<WifiSpot> wifiSpots) {
        this.wifiSpots = wifiSpots;
    }

    public void setReasonToNotify(ReasonToNotify reason) {
        reasonsToNotify.add(reason);
    }

    public void setSound(boolean sound, String reason) {
        mSound = sound;
        mSoundReason = reason;
    }

    public void setSilenceButton(boolean silenceButton, String reason) {
        mSilenceButton = silenceButton;
        mSilenceButtonReason = reason;
    }

    public void setRefreshButton(boolean refreshButton, String reason) {
        mRefreshButton = refreshButton;
        mRefreshButtonReason = reason;
    }

    public void setAutomaticFetching(boolean automaticFetching, String reason) {
        mAutomaticFetching = automaticFetching;
        mAutomaticFetchingReason = reason;
    }

    //final

    public void build() {
        int nRead = spots == null ? 0 : spots.size();
        StringBuilder sb = new StringBuilder();

        if (reasonsToNotify.size() == 0) {
//            reasonsToNotify = new ArrayList<>();
            reasonsToNotify.add(ReasonToNotify.NONE);
        }

        if (mLogType == LogType.READ && wifiSpots == null) {
            sb.append("\n" + mTime + " " + nRead + " ssids read. No Matches.\n");
        } else {
            sb.append(Head(mLogType));

            if (mLogType == LogType.READ) {
                sb.append(SummaryWeaconsFound());
                sb.append(QuittingList());
                sb.append(OccurrenceSummary());
            }

            if (reasonsToNotify.get(0) != ReasonToNotify.NONE) {
                sb.append(ReasonToNotifyText());
                sb.append(Fetching());
                sb.append(Sound());
//            sb.append(Lights());
                sb.append(BtnSilence());
                sb.append(BtnRefresh());
                sb.append(getNotificationText());
            }
        }

        String text = sb.toString();
//        ParseActions.SaveBumpLog(text);
        if (parameters.isMilenkosPhone())
            myLog.add(text, tag);
        else ParseActions.SaveBumpLog(text);//TODO que se manden todas juntas
    }


    //fragments

    private String Sound() {
        return "  " + "- Sound:" + mSound + "(" + mSoundReason + ")\n";
    }

    private String BtnRefresh() {
        return "  " + "- RefreshBTN:" + mRefreshButton + "(" + mRefreshButtonReason + ")\n";
    }

    private String BtnSilence() {
        return "  " + "- SilenceBTN:" + mSilenceButton + "(" + mSilenceButtonReason + ")\n";
    }

    private String Fetching() {
        return "  " + "- Fetching:" + mAutomaticFetching + " (" + mAutomaticFetchingReason + ")\n";
    }

    private String ReasonToNotifyText() {
        return "  " + "Reasons to notify:" + StringUtils.ConcatenateComma(reasonsToNotify) + "\n";
    }

    private String OccurrenceSummary() {
        return "  " + "OC: " + StringUtils.Listar(occurrences, 5) + "\n";
    }

    private String QuittingList() {
        return quitting.size() > 0 ? "  " + "Quitting: " +
                StringUtils.ConcatenateComma(quitting, 15) + "\n" : "";
    }

    private String SummaryWeaconsFound() {
//        String first = "  " + wifiSpots.size() + "/" + spots.size() + " detected, meaning " +
//                weaconsHash.size() + " weacons.\n";
//
//        StringBuilder sb = new StringBuilder();
//
//        for (Map.Entry<WeaconParse, ArrayList<String>> entry : weaconsHash.entrySet()) {
//            WeaconParse we = entry.getKey();
//            boolean c1 = we.isInteresting();
//            boolean c2 = occurrences == null ? false : occurrences.get(we) == 1;
//            boolean c3 = we.inHome();
//            ArrayList<String> arr = new ArrayList<>();
//            String extra = "";
//
//            if (c1) arr.add("<3");
//            if (c2) arr.add("N");
//            if (c3) arr.add("H");
//            if (c1 || c2 || c3) extra = "[" + stringUtils.concatenate(arr, " ") + "]";
//
//            sb.append("     " + extra + we.getName() + "<-" + ListOfSsids(entry.getValue(), 5) + "\n");
//        }//todo Agregar el "(NEW)"
//
//        return first + sb.toString();
        return null;
    }
    //TODO agregar sistema de niveles de indentación


    private String Head(LogType type) {
        return mTime + " [" + type.toString() + "]" + DIVIDER;
    }

    public enum LogType {
        READ, DETECTION, BTN_REFRESH, BTN_SILENCE, REMOVING_SILENCE_BUTTON, REFRESHING, OBSOLETE_REMOVAL, FORCED_REFRESH_ACTIVE_SCREEN, UPLOADED_BUSSTOP, FORCED_REFRESH
    }

    public enum ReasonToNotify {NONE, APPEARING, DISSAPIRARING, FETCHING, REMOVING_OBSOLETE_DATA, PUT_REFRESHING, REMOVE_SILENCE_BUTTON}
}
