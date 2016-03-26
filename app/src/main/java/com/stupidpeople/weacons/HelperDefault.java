package com.stupidpeople.weacons;

import android.content.Context;
import android.text.SpannableString;

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

    @Override
    protected SpannableString NotiOneLineSummary() {
        String name = we.getName();
        return StringUtils.getSpannableString(name + ". " + we.getDescription(), name.length());
    }
}
