package com.stupidpeople.weacons;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by halatm on 08/12/2015.
 */
public interface askScanResults {
    void OnReceiveWifis(List<ScanResult> sr);

    void noWifiDetected();
}
