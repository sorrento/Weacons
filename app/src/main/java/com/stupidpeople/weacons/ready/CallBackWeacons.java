package com.stupidpeople.weacons.ready;

import com.stupidpeople.weacons.Helpers.WeaconParse;

import java.util.HashSet;

/**
 * Created by Milenko on 10/03/2016.
 */
interface CallBackWeacons {
    void OnReceive(HashSet<WeaconParse> weaconHash);

}
