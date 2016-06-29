package com.stupidpeople.weacons.Helpers.WeaconBus.Santiago;


import com.stupidpeople.weacons.Helpers.WeaconBus.Bus;

import org.json.JSONObject;

/**
 * Created by Milenko on 03/02/2016.
 */
public class BusSantiago extends Bus {
    public String plate;

    public BusSantiago(JSONObject json) {
        super(json);
    }

    public BusSantiago(int arrivalTimeMins, String arrivalTimeText, String plate, int distanceMts) {
        super();
        this.distanceMts = distanceMts;
        this.arrivalTimeMins = arrivalTimeMins;
        this.arrivalTimeText = arrivalTimeText;
        this.plate = plate;
    }

    @Override
    protected void createBus(JSONObject json) {

    }
}
