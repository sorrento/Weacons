package com.stupidpeople.weacons.BusWeacon;

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
    protected ArrayList<BusLine> lineTimes;
    protected String updateTime;
    protected String stopCode;

    public BusStop(String response) {
        lineTimes = createArray(response);
    }

    protected abstract ArrayList<BusLine> createArray(String response);






}
