package com.stupidpeople.weacons.WeaconBus;

import com.stupidpeople.weacons.WeaconBus.SantCugat.BusStCugat;

import java.util.ArrayList;

/**
 * Created by Milenko on 02/02/2016.
 */
public abstract class BusLine {
    public String lineCode;
    public ArrayList<Bus> buses = new ArrayList<>();

    public BusLine(String lineCode, BusStCugat bus) {
        this.lineCode = lineCode;
//        buses = new ArrayList<>();
        addBus(bus);
    }

    public BusLine() {
    }

    public void addBus(Bus bus) {
        buses.add(bus);
    }

    public String summary() {
        StringBuilder sb = new StringBuilder(lineCode + " ");
        for (Bus bus : buses) {
            sb.append(bus.arrivalTimeText + " | ");
        }

        String s = sb.toString();

        return s;
    }

    public int getShortestTime() {
        int min = 1100;
        for (Bus bus : buses) {
            if (bus.arrivalTimeMins < min) min = bus.arrivalTimeMins;
        }
        return min;
    }

}
