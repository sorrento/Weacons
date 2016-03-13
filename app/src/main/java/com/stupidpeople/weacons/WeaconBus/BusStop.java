package com.stupidpeople.weacons.WeaconBus;

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
