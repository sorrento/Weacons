package com.stupidpeople.weacons;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Milenko on 04/03/2016.
 */
public class Helper {

    public static Intent getResultIntent(WeaconParse we, Class<?> cls, Context mContext) {
        Intent intent = new Intent(mContext, cls)
                .putExtra("wName", we.getName())
                .putExtra("wLogo", we.getLogoRounded());
        return intent;
    }
}
