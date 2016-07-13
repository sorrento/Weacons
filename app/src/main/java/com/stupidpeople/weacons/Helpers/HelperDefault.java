package com.stupidpeople.weacons.Helpers;

import android.content.Context;

import util.parameters;

/**
 * Created by Milenko on 19/03/2016.
 */
public class HelperDefault extends HelperBase {


    HelperDefault(WeaconParse we, Context ctx) {
        super(we, ctx);
    }

    @Override
    protected String typeString() {
        final parameters.typeOfWeacon type = we.getType();

        switch (type) {
            case restaurant:
                return "RESTAURANT";
            case company:
                return "COMPANY";
            default:
                return "WEACON";
        }
    }

}
