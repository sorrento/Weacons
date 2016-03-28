package com.stupidpeople.weacons.WeaconBus.Santiago;

import android.graphics.Color;

import com.stupidpeople.weacons.WeaconBus.Bus;
import com.stupidpeople.weacons.WeaconBus.BusLine;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Milenko on 03/02/2016.
 */
public class BusLineSantiago extends BusLine {
    public int color;
    private String destination;

    //TODO arreglar a  la nueva fomra de hacerlo
    public BusLineSantiago(JSONObject jLine) {
        super();

        try {

            if (jLine.getString("codigorespuesta").equals("01") || jLine.getString("codigorespuesta").equals("00")) {
                color = Color.parseColor(jLine.getString("color"));
                lineCode = jLine.getString("servicio");
                destination = jLine.getString("destino");

                String arrivalTimeText = jLine.getString("horaprediccionbus1");
                int arrivalTimeMins = Bus.ExtractMinsFromText(arrivalTimeText);
                int distanceMts = Integer.parseInt(jLine.getString("distanciabus1"));
                String plate = jLine.getString("ppubus1");

                BusSantiago busStgo = new BusSantiago(arrivalTimeMins, arrivalTimeText, plate, distanceMts);
                addBus(busStgo);
                if (jLine.getString("codigorespuesta").equals("00")) { //dos autobuses por línea
                    arrivalTimeText = jLine.getString("horaprediccionbus2");
                    arrivalTimeMins = Bus.ExtractMinsFromText(arrivalTimeText);
                    distanceMts = Integer.parseInt(jLine.getString("distanciabus2"));
                    plate = jLine.getString("ppubus2");
                    BusSantiago busStgo2 = new BusSantiago(arrivalTimeMins, arrivalTimeText, plate, distanceMts);
                    addBus(busStgo2);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
