package com.stupidpeople.weacons.Helpers;

import android.content.Context;

import util.parameters;

/**
 * Created by Milenko on 19/03/2016.
 */
public class HelperDefault extends HelperBase {


    protected HelperDefault(WeaconParse we, Context ctx) {
        super(we, ctx);
    }

    @Override
    protected String typeString() {
        if (we.getType().equals(parameters.typeOfWeacon.restaurant)) {
            return "RESTAURANT";
        } else {
            return "WEACON";
        }
    }


}
