package com.stupidpeople.weacons;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Milenko on 30/07/2015.
 * it represents the objects SSIDS from Parse
 */
@ParseClassName("SSIDS")
public class WifiSpot extends ParseObject {


    public WifiSpot() {
    }

    //GETTERS
    public String getBSSID() {
        String BSSID = getString("bssid");
        return BSSID;
    }

    public String getSSID() {
        String SSID = getString("ssid");
        return SSID;
    }

    //SETTERS

    public boolean isRelevant() {
        boolean relevant = getBoolean("relevant");
        return relevant;
    }

    @Override
    public String toString() {
        return "WifiSpot{'" + getSSID() + "' | " + getBSSID() + " | relevant=" + isRelevant() + "}";
    }

    public WeaconParse getWeacon() {
        WeaconParse weacon = (WeaconParse) getParseObject("associated_place");
        return weacon;
    }


    public String summarizeWithWeacon() {
        return getSSID() + "(" + getBSSID() + ") -> \"" + getWeacon().getName() + "\"";
    }
}
