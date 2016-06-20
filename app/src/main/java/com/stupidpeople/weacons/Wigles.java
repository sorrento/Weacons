package com.stupidpeople.weacons;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Location.GPSCoordinates;
import com.stupidpeople.weacons.Location.LocationAsker;
import com.stupidpeople.weacons.Location.LocationCallback;
import com.stupidpeople.weacons.Wifi.WifiAsker;
import com.stupidpeople.weacons.Wifi.askScanResults;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import util.myLog;

import static util.StringUtils.ListarSR;

/**
 * Created by Milenko on 22/05/2016.
 */
public class Wigles {
    private final WeaconParse mWe;
    private String tag = "WIGLE";
    private Context mContext;
    private double bestDistance = 1000;
    private ArrayList<ScanResult> bestSRs = new ArrayList<>();
    private int mCount = 0;
    private boolean satisfied = false;
    private GPSCoordinates mGps;
    private Timer t;

    public Wigles(WeaconParse we, Context ctx) {
        mWe = we;
        mContext = ctx;

        booleanCallback anyGoodSSID = new booleanCallback() {
            @Override
            public void OnResult(boolean b) {
                myLog.addToParse("Habia algun wifi que no fuera wigle? " + b, tag);
                if (b) {
                    ParseActions.wigleRemoveSSIDS(mWe);
                } else {
                    LookForBestSSIDS();
                }
            }
        };

        ParseActions.wigleHasWeaconGoodSSID(mWe, anyGoodSSID);

    }

    private void LookForBestSSIDS() {
        t = new Timer();
        myLog.addToParse("Looking for the best position en next 5 mins", tag);

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mCount++;

                myLog.addToParse(mCount + ". count of timer:", tag);
                if (mCount > 5 || satisfied) {
                    myLog.addToParse("satisfied" + satisfied + " mcount" + mCount, tag);
                    this.cancel();
                    t.purge();
                    t.cancel();
                    myLog.addToParse("starting SavingResults", tag);
                    saveResults();
                    return;
                }

                new LocationAsker(mContext, 20, new LocationCallback() {

                    @Override
                    public void NotPossibleToReachAccuracy() {
                        myLog.addToParse("not posible to get accuracy", tag);
                    }

                    @Override
                    public void LocationReceived(GPSCoordinates gps, double accuracy) {
                        myLog.addToParse("received location", tag);
                        // in mts
                        double dist = mWe.getGPS().distanceInKilometersTo(gps.getGeoPoint()) * 1000;
                        mGps = gps;
                        myLog.addToParse("dist = " + dist + " Bestdistance=  " + bestDistance, tag);

                        if (dist < bestDistance) {
                            bestDistance = dist;
                            new WifiAsker(mContext, new askScanResults() {
                                @Override
                                public void OnReceiveWifis(List<ScanResult> sr) {

                                    myLog.addToParse("wifis recibidas por WIGLE \n" + ListarSR(sr), tag);
                                    bestSRs = (ArrayList<ScanResult>) sr;
                                    myLog.addToParse("The best ditance is " + bestDistance, tag);

                                    if (bestDistance < 25) {
                                        myLog.addToParse("REACHED <25", tag);
                                        satisfied = true;
                                    }
                                }

                                @Override
                                public void noWifiDetected() {
                                    myLog.addToParse("No se ha detectado ninguna wifi en los alrededores", tag);
                                }
                            });
                        }
                    }
                });

            }
        };

        t.schedule(task, 0, 60000); //one minute
    }

    private void saveResults() {
        if (satisfied) {
            myLog.addToParse("Gonna remove SSIDS de wigle", tag);
            ParseActions.wigleRemoveSSIDS(mWe);
            myLog.addToParse("Gonna assign wifis to weacon:\n" + ListarSR(bestSRs) + "\n GPS:" + mGps + "\nWeacon " + mWe, tag);

            ParseActions.assignSpotsToWeacon(mWe, bestSRs, mGps, mContext, new MultiTaskCompleted() {
                @Override
                public void OneTaskCompleted() {
                    myLog.addToParse("Se han subido las ssids del we capturadas automaticamente" + mGps + ListarSR(bestSRs), tag);
                }

                @Override
                public void OnError(Exception e) {
                    myLog.error(e);
                }
            });
        }
    }
}
