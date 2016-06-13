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

    public Wigles(WeaconParse we, Context ctx) {
        mWe = we;
        mContext = ctx;

        booleanCallback anyGoodSSID = new booleanCallback() {
            @Override
            public void OnResult(boolean b) {
                myLog.add("Habia algun wifi que no fuera wigle? " + b, tag);
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
        final Timer t = new Timer();
        myLog.add("Looking for the best position en next 5 mins", tag);

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mCount++;

                myLog.add(mCount + " count of timer:", tag);
                if (mCount > 10 || satisfied) {
                    myLog.add("satisfied" + satisfied + " mcount" + mCount, tag);
                    this.cancel();
                    t.purge();
                    t.cancel();
                    myLog.add("starting SavingResults", tag);
                    saveResults();
                    return;
                }

                new LocationAsker(mContext, 15, new LocationCallback() {

                    @Override
                    public void NotPossibleToReachAccuracy() {

                    }

                    @Override
                    public void LocationReceived(GPSCoordinates gps, double accuracy) {
                        double dist = mWe.getGPS().distanceInKilometersTo(gps.getGeoPoint()) * 1000;
                        mGps = gps;

                        if (dist < bestDistance) {
                            bestDistance = dist;
                            new WifiAsker(mContext, new askScanResults() {
                                @Override
                                public void OnReceiveWifis(List<ScanResult> sr) {

                                    myLog.add("wifis recibidas por WIGLE \n" + ListarSR(sr), tag);
                                    bestSRs = (ArrayList<ScanResult>) sr;
                                    myLog.add("The best ditance is " + bestDistance, tag);
                                    if (bestDistance < 25) {
                                        myLog.add("REACHED <25", tag);
                                        satisfied = true;
                                    }
                                }

                                @Override
                                public void noWifiDetected() {
                                    myLog.add("No se ha detectado ninguna wifi en los alrededores", tag);
                                }
                            });
                        }
                    }
                });

            }
        };

        t.schedule(task, 0, 30000);
    }

    private void saveResults() {
        if (satisfied) {
            myLog.add("Gonna remove SSIDS de wigle", tag);
            ParseActions.wigleRemoveSSIDS(mWe);
            myLog.add("Gonna assign wifis to weacon:\n" + ListarSR(bestSRs) + "\n GPS:" + mGps + "\nWeacon " + mWe, tag);

            ParseActions.assignSpotsToWeacon(mWe, bestSRs, mGps, mContext, new MultiTaskCompleted() {
                @Override
                public void OneTaskCompleted() {
                    myLog.add("Se han subido las ssids del we capturadas automaticamente" + mGps + ListarSR(bestSRs), tag);
                }

                @Override
                public void OnError(Exception e) {
                    myLog.error(e);
                }
            });
        }
    }
}
