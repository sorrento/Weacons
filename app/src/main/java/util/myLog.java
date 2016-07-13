package util;

import android.os.Environment;
import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.Helpers.WeaconParse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Milenko on 16/07/2015.
 */
public class myLog {
    private static boolean initialized = false;
    private static String currentDateandTime;
    private static String folder = "/WEACLOG/";

    public static void initialize() {
        if (!parameters.isMilenkosPhone()) return;

        if (initialized) return;
        initialized = true;
        WriteUnhandledErrors();
        currentDateandTime = currentDate();

        File folderm = new File(Environment.getExternalStorageDirectory() + folder);
        if (!folderm.exists()) folderm.mkdir();

    }

    /***
     * Add the text to a file which has TAG in the name. It also prints in this tag.
     *
     * @param text
     * @param TAG
     */
    public static void add(String text, String TAG) {
        if (!parameters.isMilenkosPhone()) return;
        try {
            Log.d(TAG, text);

            File logFile = new File(Environment.getExternalStorageDirectory(), folder + currentDateandTime + "_" + TAG + ".txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss (dd)| ");
            String currentDateandTime = sdf.format(new Date());

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(currentDateandTime + text);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void error(Exception e) {
        addToParse(Log.getStackTraceString(e), "ERROR");
    }

    public static void logNotification(String title, String body, String summary, boolean sound, boolean silencBtn, boolean refreshBtn) {
        String btnSilence, btnRefresh;
        btnSilence = silencBtn ? "SILENCE" : "-";
        btnRefresh = refreshBtn ? "REFRESH" : "-";

        myLog.add("\n***********************************SOUND:" + sound + "\n   " + title + "\n" + body +
                "   " + summary + "\n***" + btnSilence + "|" + btnRefresh + "\n***********************************\n", "NOTI");
    }


    /***
     * Send unhandled errors to a text file in the phone
     */
    private static void WriteUnhandledErrors() {
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                PrintWriter pw;
                try {
                    pw = new PrintWriter(
                            new FileWriter(Environment.getExternalStorageDirectory() + folder + "/rt.txt", true));
                    pw.append("*******" + currentDate() + "\n");
                    ex.printStackTrace(pw);
                    pw.flush();
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static String currentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return sdf.format(new Date());
    }

    public static void weaconsDetected(HashMap<WeaconParse, ArrayList<String>> weaconsHash, int nWifisDetected, int nWifis) {
        StringBuilder sb = new StringBuilder();

        String first = "  " + nWifisDetected + "/" + nWifis + " detected, meaning " +
                weaconsHash.size() + " weacons.\n";

        for (Map.Entry<WeaconParse, ArrayList<String>> entry : weaconsHash.entrySet()) {
            WeaconParse we = entry.getKey();

            boolean c1 = we.isInteresting();
//            boolean c2 = false;
//            if (occurrences.size() != 0) c2 = occurrences.get(we) == 1;
            boolean c3 = we.inHome();

            ArrayList<String> arr = new ArrayList<>();
            String extra = "";

            if (parameters.isMilenkosPhone()) {
                if (c1) arr.add("<3");
                //if (c2) arr.add("N");
                if (c3) arr.add("H");
                if (c1 || c3) extra = "[" + StringUtils.concatenate(arr, " ") + "]";
            }

            sb.append("     ").append(extra).append(we.getName()).append("<-").append(ListOfSsids(entry.getValue(), 5)).append("\n");
        }

        addToParse(first + sb.toString(), "Detection");
    }

    private static String ListOfSsids(ArrayList<String> arrayList, int i) {
        return "(" + StringUtils.ConcatenateComma(arrayList, i) + ")";
    }

    public static void addToParse(String text, String type) {
        int pid = android.os.Process.myPid();

        if (parameters.isMilenkosPhone()) {
            add(text, type);

        } else {
            ParseObject po = new ParseObject("log");
            po.put("msg", currentDate() + " " + type + " | " + pid + "|" + text);
            po.put("type", type);
            po.pinInBackground(parameters.pinParseLog);
        }
    }

    public static void directLogParse(String msg, String tag) {
        ParseObject po = new ParseObject("log");
        po.put("msg", msg);
        po.put("type", tag);
        po.put("n", 1);
        final ParseUser user = ParseUser.getCurrentUser();
        if (user != null) po.put("user", user);

        po.saveInBackground();
    }

    public static void directLogParse(String msg, int n, SaveCallback callback, String tag) {
        ParseObject po = new ParseObject("log");
        po.put("msg", msg);
        po.put("type", tag);
        po.put("n", n);
        final ParseUser user = ParseUser.getCurrentUser();
        if (user != null) po.put("user", user);

        po.saveInBackground(callback);
    }
}

