package com.stupidpeople.weacons.Helpers.WeaconBus.SantCugat;


import com.stupidpeople.weacons.Helpers.WeaconBus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Milenko on 02/02/2016.
 */
public class BusStCugat extends Bus {

    private String stopCode;
    private int lineState;//=1 means working
    private int routeId;//=1 or 2, probably the direction
    private String updateTime;

    public BusStCugat(JSONObject json) {
        super(json);
    }

    @Override
    protected void createBus(JSONObject json) {

        try {
            //bus
            arrivalTimeMins = json.getInt("arrivalTime") / 60;
            arrivalTimeText = json.getString("roundedArrivalTime"); //ej "13 min", "IMMINENT".
            //line
            lineCode = json.getString("lineCode");
            lineState = json.getInt("lineState");
            routeId = json.getInt("routeId");

            stopCode = String.valueOf(json.getInt("stopCode"));
            updateTime = json.getString("updatedTime");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLineCode() {
        return lineCode;
    }

    public String getStopCode() {
        return stopCode;
    }

    public String getUpdateTime() {
        return updateTime;
    }
}
