package com.stupidpeople.weacons.WeaconBus;


import java.util.ArrayList;

/**
 * Created by Milenko on 02/02/2016.
 */
public class BusLine {
    public String lineCode, destination;
    public ArrayList<Bus> buses = new ArrayList<>();

    public BusLine(Bus bus) {
        this.lineCode = bus.lineCode;
        destination = bus.destination;
        addBus(bus);
    }

    public BusLine() {
    }

    public void addBus(Bus bus) {
        buses.add(bus);
    }

    public int getShortestTime() {
        int min = 1100;
        for (Bus bus : buses) {
            if (bus.arrivalTimeMins < min) min = bus.arrivalTimeMins;
        }
        return min;
    }

}
