package util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

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

    //TODO mover todo esto como estatic de linea stcugat

//    /**
//     * reformat the times as
//     * L1: 3 min, 4 min, 50 min.
//     * L9: 6min, 72 min.
//     *
//     * @return
//     */
//    public static String TimesSummarySorted(ArrayList<LineTimeStCgOld> lineTimes) {
//        String substring = "No info";
//
//        if (lineTimes.size() > 0) {
//            try {
//                formatter form = new formatter(lineTimes);
//                HashMap<String, ArrayList<LineTimeStCgOld>> tableLines = form.getTable();
//
//                StringBuilder sb = new StringBuilder();
//                for (String name : tableLines.keySet()) {
//                    sb.append(summaryLineTimes(name, tableLines.get(name)) + "\n");
//                }
//                String s = sb.toString();
//                substring = s.substring(0, s.length() - 2);
//
//            } catch (Exception e) {
//                myLog.add("error mostrando resumen ordenado de timeline" + e.getLocalizedMessage());
//            }
//        }
//
//        return substring;
//    }
//
//    public static String summaryLineTimes(String name, ArrayList<LineTimeStCgOld> lineTimes) {
//        StringBuilder sb = new StringBuilder(name + ": ");
//        for (LineTimeStCgOld lineTime : lineTimes) {
//            sb.append(lineTime.roundedTime + ", ");
//        }
//        String s = sb.toString();
//        return (s.substring(0, s.length() - 2) + ".");
//    }

    //TODO mover a utils images o similar
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}

