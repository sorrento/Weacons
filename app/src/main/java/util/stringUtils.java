package util;

import android.support.annotation.NonNull;

import com.stupidpeople.weacons.LogBump;
import com.stupidpeople.weacons.StringUtils;

import java.util.ArrayList;

/**
 * Created by Milenko on 29/10/2015.
 */
public class stringUtils {
    @NonNull
    public static String ConcatenateComma(String[] lista) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lista.length - 1; i++) {
            sb.append(lista[i] + ", ");
        }
        sb.append(lista[lista.length - 1]);

        return sb.toString();
    }

    public static String TrimFirstWord(String s) {
        int i = s.indexOf(" ");
        return s.substring(i + 1);
    }

    public static String TrimFirstWords(String s, int n) {
        String sa = s;
        for (int i = 0; i < n; i++) {
            String sol = TrimFirstWord(sa);
            sa = sol;
        }
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
    public static String concatenate(ArrayList<String> arr, int nShorten, String sep) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.size() - 1; i++) {
            sb.append(StringUtils.shorten(arr.get(i), nShorten) + sep);
        }
        sb.append(StringUtils.shorten(arr.get(arr.size() - 1), nShorten));

        return sb.toString();
    }
}

