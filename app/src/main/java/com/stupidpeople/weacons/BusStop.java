package com.stupidpeople.weacons;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.util.ArrayList;

/**
 * Created by Milenko on 02/02/2016.
 */
public abstract class BusStop {
    protected ArrayList<NewBusLine> lineTimes;
    protected String updateTime;
    protected String stopCode;

    public BusStop(String response) {
        lineTimes = createArray(response);
    }

    protected abstract ArrayList<NewBusLine> createArray(String response);

    //    public ArrayList<NewBusLine> getLineTimes() {
//        return lineTimes;
//    }

    public ArrayList embedInArray() {
        ArrayList arr = new ArrayList();
        arr.add(this);
        return arr;
    }

    /**
     * Array with strings that summarizes each line: L1: 12 min, 18 min, 35 min
     * ideal for single notification (inbox format)
     *
     * @return
     */
    public ArrayList<SpannableString> summarizeByOneLine() {
        ArrayList<SpannableString> arr = new ArrayList<>();

        if (lineTimes == null || lineTimes.size() == 0) {
            arr.add(new SpannableString("No info for this stop by now."));
        } else {

            for (NewBusLine lt : lineTimes) {
                String name = lt.lineCode;

                StringBuilder sb = new StringBuilder(name + " ");

                for (NewBus bus : lt.buses) {
                    sb.append(bus.arrivalTimeText + ", ");
                }

                String s = sb.toString();
                String sub = s.substring(0, s.length() - 2);

                //add format
                SpannableString span = new SpannableString(sub);
                span.setSpan(new ForegroundColorSpan(Color.BLACK), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new RelativeSizeSpan(1.1f), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                arr.add(span);

            }
        }
        return arr;
    }

    /**
     * Shows only the first arrival by line:  L1:10m | B3: 5m | R4:18m
     *
     * @param compact for having L1:10|B3:5|R4:18
     * @return
     */
    public String summarizeAllLines(boolean compact) {
        String substring = "No info";

        int del = 0;

        if (lineTimes == null) return "No lines available";

        if (lineTimes.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (NewBusLine line : lineTimes) {
                String name = line.lineCode;

                if (compact) {
                    sb.append(name + ":" + line.getShortestTime() + "|");
                    del = 1;
                } else {
                    sb.append(name + ": " + line.getShortestTime() + "m | ");
                    del = 2;
                }
            }
            String s = sb.toString();
            substring = s.substring(0, s.length() - del);
        }

        return substring;
    }

    public String summarizeAllLines() {
        return summarizeAllLines(false);
    }
}
