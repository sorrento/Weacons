package com.stupidpeople.weacons;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Milenko on 30/07/2015.
 * it represents the objects SSIDS from Parse
 */
@ParseClassName("SSIDS")
public class WifiSpot extends ParseObject {

    public WifiSpot(String selectedSSID, String selectedBSSID, WeaconParse we, double latitude, double longitude) {
        setSSID(selectedSSID);
        setBSSID(selectedBSSID);
        setWeacon(we);
        setGPS(new ParseGeoPoint(latitude, longitude));
        setOwner(ParseUser.getCurrentUser());
        setAutomatic(false);
    }

    public WifiSpot() {
    }

    //GETTERS
    public String getBSSID() {
        String BSSID = getString("bssid");
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        put("bssid", BSSID);
    }

    //SETTERS

    public String getSSID() {
        String SSID = getString("ssid");
        return SSID;
    }

    public void setSSID(String SSID) {
        put("ssid", SSID);
    }

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

    public void setWeacon(WeaconParse weacon) {
        put("associated_place", weacon);
    }

    public String summarizeWithWeacon() {
        return getSSID() + "(" + getBSSID() + ") -> \"" + getWeacon().getName() + "\"";
    }

    public void setGPS(ParseGeoPoint GPS) {
        put("GPS", GPS);
    }

    public void setOwner(ParseUser owner) {
        put("owner", owner);
    }

    public void setAutomatic(boolean automatic) {
        put("Automatic", automatic);
    }

}
