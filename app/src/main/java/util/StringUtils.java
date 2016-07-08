package util;

import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Removable.LogBump;
import com.stupidpeople.weacons.Wifi.WifiSpot;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        m = Math.min(text.length(), m);
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
            sb.append(ws.getSSID() + " | " + ws.getObjectId() + " | udpd:" + ws.getUpdatedAt() + "\n");
        }
        return sb.toString();
    }

    public static String ListarSR(List<ScanResult> sr) {
        StringBuilder sb = new StringBuilder();

        for (ScanResult s : sr) sb.append(s.SSID + " | " + s.BSSID + " | " + s.level + "\n");

        return sb.toString();
    }

    public static String shorten(String s, int m) {
        return s.substring(0, Math.min(m, s.length()));
    }

    @NonNull
    static String Notif2String(String cqTitle, String cqContent, String title, String body, String bottom) {

        return ("  " + "CQ" + "-------------------------------\n") + "  " + "  " + cqTitle + "\n" + "  " + "  " + cqContent + "\n" + "  " + "EX" + "-------------------------------\n" + "  " + "  " + title + "\n" + body + "\n" + "  " + "  " + bottom + "\n";
    }

    public static String FirstWord(String s) {
        String[] pp = s.split(" ");
        return pp[0];
    }

    private static String TrimFirstWord(String s) {
        int i = s.indexOf(" ");
        return s.substring(i + 1);
    }

    @NonNull
    public static String ConcatenateComma(String[] lista) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lista.length - 1; i++) {
            sb.append(lista[i] + ", ");
        }
        sb.append(lista[lista.length - 1]);

        return sb.toString();
    }

    public static String TrimFirstWords(String s, int n) {
        String sa = s;

        for (int i = 0; i < n; i++) sa = TrimFirstWord(sa);

        return sa;
    }


    public static String ConcatenateComma(ArrayList<String> arr, int m) {
        return concatenate(arr, m, ", ");
    }

    public static String concatenate(ArrayList<String> arr, String s) {
        return concatenate(arr, 100, s);
    }

    public static String ConcatenateComma(ArrayList<LogBump.ReasonToNotify> arr) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.size() - 1; i++) {
            sb.append(arr.get(i) + ", ");
        }

        sb.append(arr.get(arr.size() - 1));

        return sb.toString();

    }

    @NonNull
    private static String concatenate(ArrayList<String> arr, int nShorten, String sep) {
        if (arr == null || arr.size() == 0) return "";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.size() - 1; i++) {
            sb.append(shorten(arr.get(i), nShorten) + sep);
        }
        sb.append(shorten(arr.get(arr.size() - 1), nShorten));

        return sb.toString();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
