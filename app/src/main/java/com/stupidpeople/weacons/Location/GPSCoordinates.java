package com.stupidpeople.weacons.Location;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

public class GPSCoordinates {

    private double latitude = 0;
    private double longitude = 0;

    public GPSCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GPSCoordinates(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public GPSCoordinates(ParseGeoPoint geoPoint) {
        latitude = geoPoint.getLatitude();
        longitude = geoPoint.getLongitude();
    }

    public String toString() {
        return "GPS{" + latitude + "," + longitude + '}';
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public ParseGeoPoint getGeoPoint() {
        return new ParseGeoPoint(latitude, longitude);
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
