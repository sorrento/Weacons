package com.stupidpeople.weacons.Helpers.WeaconBus.Santiago;

import android.graphics.Color;

import com.stupidpeople.weacons.Helpers.WeaconBus.Bus;
import com.stupidpeople.weacons.Helpers.WeaconBus.BusLine;

import org.json.JSONException;
import org.json.JSONObject;

import util.myLog;

/**
 * Created by Milenko on 03/02/2016.
 */
public class BusLineSantiago extends BusLine {
    public int color;

    public BusLineSantiago(JSONObject jLine) {
        super();

        try {
            myLog.add(jLine.toString(), "aut");
            final String cod = jLine.getString("codigorespuesta");

            if (cod.equals("01") || cod.equals("00")) {
                if (jLine.has("color")) color = Color.parseColor(jLine.getString("color"));
                if (jLine.has("destination")) destination = jLine.getString("destino");

                lineCode = jLine.getString("servicio");
                msgLine = jLine.getString("respuestaServicio");

                String arrivalTimeText = jLine.getString("horaprediccionbus1");
                int arrivalTimeMins = Bus.ExtractMinsFromText(arrivalTimeText);
                int distanceMts = Integer.parseInt(jLine.getString("distanciabus1"));
                String plate = jLine.getString("ppubus1");

                BusSantiago busStgo = new BusSantiago(arrivalTimeMins, arrivalTimeText, plate, distanceMts);
                addBus(busStgo);
                if (cod.equals("00")) { //dos autobuses por línea
                    arrivalTimeText = jLine.getString("horaprediccionbus2");
                    arrivalTimeMins = Bus.ExtractMinsFromText(arrivalTimeText);
                    distanceMts = Integer.parseInt(jLine.getString("distanciabus2"));
                    plate = jLine.getString("ppubus2");
                    BusSantiago busStgo2 = new BusSantiago(arrivalTimeMins, arrivalTimeText, plate, distanceMts);
                    addBus(busStgo2);
                }
            } else if (cod.equals("9")) {
                // "respuestaServicio" : "Frecuencia estimada es 1 bus cada 10 min.",
                lineCode = jLine.getString("servicio");
                msgLine = jLine.getString("respuestaServicio");
            }


        } catch (JSONException e) {
            myLog.add("error json" + e.getLocalizedMessage(), "aut");
            myLog.error(e);
        }
    }


}
