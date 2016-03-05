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
        initialized = true;
        WriteUnhandledErrors();
        currentDateandTime = currentDate();
    }

    /***
     * Add the text to a file which has TAG in the name. It also prints in this tag.
     *
     * @param text
     * @param TAG
     */
    public static void add(String text, String TAG) {
        try {
            Log.d(TAG, text);

            if (!initialized) {
                //Just to observe when we lost continuiti
                File logFile = new File(Environment.getExternalStorageDirectory(), folder + currentDateandTime + "_REC" + TAG + ".txt");
                logFile.createNewFile();

                initialize();
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
