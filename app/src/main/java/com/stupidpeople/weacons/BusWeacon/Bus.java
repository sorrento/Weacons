package com.stupidpeople.weacons.BusWeacon;

import org.json.JSONObject;

/**
 * Created by Milenko on 02/02/2016.
 */
public abstract class Bus {
    protected int arrivalTimeMins;
    protected String arrivalTimeText;

    public Bus(JSONObject json) {
        createBus(json);
    }

    public Bus() {

    }

    protected abstract void createBus(JSONObject json);
}