package com.stupidpeople.weacons.ready;

import com.stupidpeople.weacons.WeaconParse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Milenko on 10/03/2016.
 */
public interface CallBackWeacons {
    void OnReceive(HashSet<WeaconParse> weaconHash);

}
