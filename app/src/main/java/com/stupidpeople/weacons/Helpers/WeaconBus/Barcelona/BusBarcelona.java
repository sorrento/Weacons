package com.stupidpeople.weacons.Helpers.WeaconBus.Barcelona;

import com.stupidpeople.weacons.Helpers.WeaconBus.Bus;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

/**
 * Created by Milenko on 01/04/2016.
 */
public class BusBarcelona extends Bus {
    public BusBarcelona(Element xmlBus) {
        Element el = xmlBus.child(0);
        String[] subs = el.text().split(" ");

        lineCode = "L" + subs[1];
        arrivalTimeText = el.child(0).text();
        arrivalTimeMins = Bus.ExtractMinsFromText(arrivalTimeText);
    }

    @Override
    protected void createBus(JSONObject json) throws JSONException {

    }
}
