package com.stupidpeople.weacons.Location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.stupidpeople.weacons.R;

import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 25/09/2015.
 */
public class LocationAsker implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private String tag = "LocAsk";
    private Context mContext;

    private LocationCallback mLocationCallback;
    private GoogleApiClient mGoogleApiClient;
    private int iFail;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public LocationAsker(Context ctx, final LocationCallback locationCallback) {
        mContext = ctx;
        mLocationCallback = locationCallback;
        buildGoogleApiClient();
    }

    public LocationAsker(final Context ctx, final double accuracyNeededMts, final LocationCallback locationCallback) {
//        this(ctx, locationCallback);
        mContext = ctx;
        mLocationCallback = locationCallback;

        // For tests
        if (parameters.doFakePosition) {
            locationCallback.LocationReceived(parameters.fakeCoords, 3);
            return;
        }

        iFail = 0;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(ctx, R.string.turn_on_gps, Toast.LENGTH_SHORT).show();
            return;

        }
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                myLog.add("ha cambiad loa localcixzacion , dentro de la precision. Tiene vleovidad:" + location.hasSpeed(), tag);
                if (location.hasAccuracy()) {
                    double accuracy = location.getAccuracy();
                    if (accuracy <= accuracyNeededMts) {

                        myLog.add("estamos a con precision mejor de " + accuracyNeededMts + " mts " + accuracy, tag);
                        reportPositionAndClose(location);
                    } else {
                        iFail++;
                        String text = ctx.getString(R.string.location_precision) + accuracy;
                        myLog.add(text, tag);
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                        if (iFail == 10) {
                            text = ctx.getString(R.string.unable_gps_precision);
                            myLog.add(text, tag);
                            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                            reportPositionAndClose(null);
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                myLog.add("ha camnbiado del statusl de location: provider" + provider + "|stauss" + status, tag);
            }

            @Override
            public void onProviderEnabled(String provider) {
                myLog.add("encendido el provider:" + provider, tag);
            }

            @Override
            public void onProviderDisabled(String provider) {
                myLog.add("apagado el proivder" + provider, tag);
            }
        };

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            myLog.error(e);
        }
    }

    private synchronized void buildGoogleApiClient() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();

        } catch (Exception e) {
            myLog.error(e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (mLastLocation == null) {

            int priority = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    ? LocationRequest.PRIORITY_LOW_POWER
                    : LocationRequest.PRIORITY_HIGH_ACCURACY;

            askOneUpdate(priority);

        } else {
            LocationReceivedOk(mLastLocation);
        }
    }

    private void askOneUpdate(int priority) {

        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(priority)
                .setNumUpdates(1)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void LocationReceivedOk(Location mLastLocation) {
        GPSCoordinates gps = new GPSCoordinates(mLastLocation);
        double acc = Math.round(mLastLocation.getAccuracy());

        myLog.add("Last location is: " + gps, tag);

        if (mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();

        mLocationCallback.LocationReceived(gps, acc);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        myLog.add("Connection to google location has failed: " + connectionResult, tag);

    }

    private void reportPositionAndClose(Location location) {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (SecurityException e) {
            myLog.error(e);
        }
        if (location == null) {
            mLocationCallback.NotPossibleToReachAccuracy();
        } else {
            mLocationCallback.LocationReceived(new GPSCoordinates(location), location.getAccuracy());
        }
//        mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnectionSuspended(int i) {
        myLog.add("Connection to google location was suspended", tag);
    }

    @Override
    public void onLocationChanged(Location location) {
        myLog.add("Localización obtenida mediante solicitude de updates", tag);
        if (location == null) {
            myLog.add("Hemos recibido un update null", tag);
        } else {
            myLog.add("Tenemos la localización from updates:" + location, tag);
            LocationReceivedOk(location);
        }
    }
}