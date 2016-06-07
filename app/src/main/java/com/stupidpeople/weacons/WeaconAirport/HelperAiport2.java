package com.stupidpeople.weacons.WeaconAirport;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.stupidpeople.weacons.HelperBase;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.NotifFeatures;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.WeaconParse;

/**
 * Created by Milenko on 18/03/2016.
 */

public class HelperAiport2 extends HelperBase {
//TODO airport meritates its own notification. Implementate that possibility

    public HelperAiport2(WeaconParse we, Context ctx) {
        super(we, ctx);
    }

    @Override
    protected String typeString() {
        return "AIRPORT";
    }


    @Override
    protected NotificationCompat.Builder buildSingleNotification(
            PendingIntent resultPendingIntent, Context ctx) {

        //TODO agregar que vata a cards acivitys al pinchar en la notif, que diferencie arrivals, y agregarle el extra que necesita
        NotifFeatures f = LogInManagement.notifFeatures;


        NotificationCompat.Builder notif = baseNotif(ctx, f.sound, f.refreshButton);

        NotificationCompat.Action DepartureAction = new NotificationCompat.Action(R.drawable.ic_notif_we, "Departures", resultPendingIntent);//TODO replace by planes and intents
        NotificationCompat.Action ArrivalAction = new NotificationCompat.Action(R.drawable.ic_notif_we, "Arrivals", resultPendingIntent);

        //Actions
        notif.addAction(DepartureAction)
                .addAction(ArrivalAction);

        notif.setContentIntent(resultPendingIntent);

        return notif;
    }
}