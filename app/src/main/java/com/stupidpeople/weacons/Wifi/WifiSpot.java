package com.stupidpeople.weacons.Wifi;

import android.net.wifi.ScanResult;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Location.GPSCoordinates;

import java.util.ArrayList;

/**
 * Created by Milenko on 30/07/2015.
 * it represents the objects SSIDS from Parse
 */
@ParseClassName("SSIDS")
public class WifiSpot extends ParseObject {

    private double distanceToWeacon;

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

    public WifiSpot(ScanResult r, WeaconParse weBusStop, GPSCoordinates gps, double distanceToWeaconMts) {
        this(r.SSID, r.BSSID, weBusStop, gps.getLatitude(), gps.getLongitude());
        setDistanceToWeacon(distanceToWeaconMts);
    }

    public static String Listar(ArrayList<WifiSpot> newOnes) {
        StringBuilder sb = new StringBuilder();
        for (WifiSpot ws : newOnes) {
            sb.append(ws.summarizeWithWeacon() + "\n");
        }
        return sb.toString();
    }

    //GETTERS
    public String getBSSID() {
        return getString("bssid");
    }

    public void setBSSID(String BSSID) {
        put("bssid", BSSID);
    }

    //SETTERS

    public ParseGeoPoint getGPS() {
        return getParseGeoPoint("GPS");
    }

    public void setGPS(ParseGeoPoint GPS) {
        put("GPS", GPS);
    }

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

    public void setOwner(ParseUser owner) {
        put("owner", owner);
    }

    public void setAutomatic(boolean automatic) {
        put("Automatic", automatic);
    }

    public void setDistanceToWeacon(double distanceToWeacon) {
        put("distanceWe", distanceToWeacon);
    }
}
