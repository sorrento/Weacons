package com.stupidpeople.weacons.ready;

import com.stupidpeople.weacons.WeaconParse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Milenko on 10/03/2016.
 */
public interface CallBackWeacons {
    void OnReceive(HashMap<WeaconParse, ArrayList<String>> weaconHash);

}
