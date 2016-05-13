package util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

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

            if (!initialized) {
                initialize();
                //Just to observe when we lost continuiti
                File logFile = new File(Environment.getExternalStorageDirectory(), folder + currentDateandTime + "_REC" + TAG + ".txt");
                logFile.createNewFile();

            }

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
        myLog.error(e);
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
    public static void WriteUnhandledErrors() {
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

}
