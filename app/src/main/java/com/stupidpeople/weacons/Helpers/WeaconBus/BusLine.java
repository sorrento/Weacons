package com.stupidpeople.weacons.Helpers.WeaconBus;

import android.text.SpannableString;

import com.stupidpeople.weacons.Helpers.fetchableElement;

import java.util.ArrayList;

import util.StringUtils;

/**
 * Created by Milenko on 02/02/2016.
 */
public class BusLine implements fetchableElement {
    protected String lineCode;
    private String destination;
    private ArrayList<Bus> buses = new ArrayList<>();

    public BusLine(Bus bus) {
        this.lineCode = bus.lineCode;
        destination = bus.destination;
        addBus(bus);
    }

    protected BusLine() {
    }

    public void addBus(Bus bus) {
        buses.add(bus);
    }

    private int getShortestTime() {
        int minimal = 1100;
        for (Bus bus : buses) {
            if (bus.arrivalTimeMins < minimal) minimal = bus.arrivalTimeMins;
        }
        return minimal;
    }

    @Override
    public SpannableString oneLineSummary() {

        String name = lineCode;
        StringBuilder sb = new StringBuilder(name + " ");

        for (Bus bus : buses) sb.append(bus.arrivalTimeText).append(", ");

        String s = sb.toString();
        String sub = s.substring(0, s.length() - 2);

        return StringUtils.getSpannableString(sub, name.length());
    }

    @Override
    public SpannableString getLongSpan() {
        return oneLineSummary();
    }

    @Override
    public String veryShortSummary() {
        return lineCode + ":" + getShortestTime() + "m";
    }
}
