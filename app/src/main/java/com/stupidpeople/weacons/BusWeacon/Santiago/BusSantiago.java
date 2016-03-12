package com.stupidpeople.weacons.BusWeacon.Santiago;

import com.stupidpeople.weacons.BusWeacon.Bus;

import org.json.JSONObject;

/**
 * Created by Milenko on 03/02/2016.
 */
public class BusSantiago extends Bus {
    public int distanceMts;
    public String plate;

    public BusSantiago(JSONObject json) {
        super(json);
    }

    public BusSantiago(int arrivalTimeMins, String arrivalTimeText, String plate, int distanceMts) {
        super();
        this.arrivalTimeMins = arrivalTimeMins;
        this.arrivalTimeText = arrivalTimeText;
        this.plate = plate;
        this.distanceMts = distanceMts;
    }

    @Override
    protected void createBus(JSONObject json) {

    }
}