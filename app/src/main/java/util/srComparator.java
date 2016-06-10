package util;

import android.net.wifi.ScanResult;

import java.util.Comparator;

/**
 * Created by Milenko on 10/06/2016.
 */ //////////////////////
public class srComparator implements Comparator<ScanResult> {

    @Override
    public int compare(ScanResult lhs, ScanResult rhs) {
        return rhs.level - lhs.level;
    }
}
