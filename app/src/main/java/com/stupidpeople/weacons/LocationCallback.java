package com.stupidpeople.weacons;


/**
 * Created by Milenko on 25/09/2015.
 */
public interface LocationCallback {
    void LocationReceived(GPSCoordinates gps);

    void NotPossibleToReachAccuracy();

    void LocationReceived(GPSCoordinates gps, double accuracy);
}
