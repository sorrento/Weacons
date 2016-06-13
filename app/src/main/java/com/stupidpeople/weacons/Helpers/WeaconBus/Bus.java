package com.stupidpeople.weacons.Helpers.WeaconBus;

import org.json.JSONException;
import org.json.JSONObject;

import util.myLog;

/**
 * Created by Milenko on 02/02/2016.
 */
public abstract class Bus {
    public String arrivalTimeText;
    protected int arrivalTimeMins;
    protected String lineCode;
    protected String destination;
    protected boolean isNocturne;

    protected Bus(JSONObject json) {
        try {
            createBus(json);
        } catch (JSONException e) {
            myLog.error(e);
        }
    }

    protected Bus() {

    }

    /**
     * If there are more than one, it takes the mean value ("from 2 to 5 mins")
     * If no number appears, assigns, 0
     *
     * @param arrivalTimeText
     * @return
     */
    public static int ExtractMinsFromText(String arrivalTimeText) {
        String[] words = arrivalTimeText.split(" ");
        int count = 0;
        int i = 0;
        for (String word : words) {
            if (isInteger(word)) {
                i += Integer.valueOf(word);
                count++;
            }
        }
        if (count == 0) return 0;

        return i / count;
    }

    private static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    private static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    public String getLineCode() {
        return lineCode;
    }

    protected abstract void createBus(JSONObject json) throws JSONException;
}