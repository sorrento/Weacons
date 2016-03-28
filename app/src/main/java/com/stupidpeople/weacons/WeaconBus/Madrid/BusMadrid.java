package com.stupidpeople.weacons.WeaconBus.Madrid;

import com.stupidpeople.weacons.WeaconBus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Milenko on 28/03/2016.
 */
public class BusMadrid extends Bus {
    public BusMadrid(JSONObject json) {
        super(json);
    }

    @Override
    protected void createBus(JSONObject json) throws JSONException {
        lineCode = json.getString("lineNumber");
        destination = json.getString("lineBound");
        arrivalTimeText = json.getString("waitTime");
        arrivalTimeMins = ExtractMinsFromText(arrivalTimeText);
        isNocturne = json.getBoolean("isNightLine");
    }

}
