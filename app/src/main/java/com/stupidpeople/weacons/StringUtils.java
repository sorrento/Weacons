package com.stupidpeople.weacons;

import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Milenko on 14/03/2016.
 */
public class StringUtils {
    /**
     * The first m characters are BOLD and slighty bigger
     *
     * @param text
     * @param m
     * @return
     */
    @NonNull
    public static SpannableString getSpannableString(String text, int m) {
        SpannableString span = new SpannableString(text);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            span.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(1.1f), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }


    //to list


    public static String Listar(HashSet<WeaconParse> weacons) {
        StringBuilder sb = new StringBuilder();
        for (WeaconParse we : weacons) {
            sb.append(we.getName() + " | ");
        }
        return sb.toString();
    }

    public static String Listar(HashMap<WeaconParse, Integer> hash) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<WeaconParse, Integer> entry : hash.entrySet()) {
            sb.append(entry.getKey().getName() + ":" + entry.getValue() + " | ");
        }
        return sb.toString();
    }

    public static String Listar(ArrayList<WeaconParse> weacons) {
        StringBuilder sb = new StringBuilder();
        for (WeaconParse we : weacons) {
            sb.append(we.getName() + " | ");
        }
        return sb.toString();
    }

    public static String Listar(HashMap<WeaconParse, Integer> hash, int i) {
        if (hash == null || hash.size() == 0) return "[EMPTY]";

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<WeaconParse, Integer> entry : hash.entrySet()) {
            sb.append(shorten(entry.getKey().getName(), i) + ":" + entry.getValue() + " | ");
        }
        return sb.toString();
    }

    public static String Listar(List<WifiSpot> list) {
        StringBuilder sb = new StringBuilder();
        for (WifiSpot ws : list) {
            sb.append("(" + ws.getSSID() + ", " + ws.getObjectId() + ") |");
        }
        return sb.toString();
    }

    public static String ListarSR(List<ScanResult> sr) {
        StringBuilder sb = new StringBuilder();
        for (ScanResult s : sr) {
            sb.append(s.SSID + " | " + s.BSSID + " | " + s.level + "\n");
        }
        return sb.toString();
    }

    public static String shorten(String s, int m) {
        return s.substring(0, Math.min(m, s.length()));
    }


    @NonNull
    static String Notif2String(String cqTitle, String cqContent, String title, String body, String bottom) {
        StringBuilder sb = new StringBuilder();

        sb.append("  " + "CQ" + "-------------------------------\n");
        sb.append("  " + "  " + cqTitle + "\n");
        sb.append("  " + "  " + cqContent + "\n");
        sb.append("  " + "EX" + "-------------------------------\n");
        sb.append("  " + "  " + title + "\n");
        sb.append(body + "\n");
        sb.append("  " + "  " + bottom + "\n");
        return sb.toString();
    }
}
