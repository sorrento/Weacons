package com.stupidpeople.weacons.WeaconAirport;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.HelperAbstract;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.WeaconParse;

/**
 * Created by Milenko on 18/03/2016.
 */

public class HelperAiport2 extends HelperAbstract {
//TODO airport meritates its own notification. Implementate that possibility

    public HelperAiport2(WeaconParse we, Context ctx) {
        super(we, ctx);
    }

    @Override
    protected String typeString() {
        return "AIRPORT";
    }

    @Override
    protected SpannableString NotiOneLineSummary() {
        return SpannableString.valueOf(we.getDescription());
    }

    @Override
    protected NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext, boolean anyInteresting) {

        //TODO agregar que vata a cards acivitys al pinchar en la notif, que diferencie arrivals, y agregarle el extra que necesita

        NotificationCompat.Builder notif = baseNotif(mContext, sound, anyInteresting);

        NotificationCompat.Action DepartureAction = new NotificationCompat.Action(R.drawable.ic_notif_we, "Departures", resultPendingIntent);//TODO replace by planes and intents
        NotificationCompat.Action ArrivalAction = new NotificationCompat.Action(R.drawable.ic_notif_we, "Arrivals", resultPendingIntent);

        //Actions
        notif.addAction(DepartureAction)
                .addAction(ArrivalAction);

        notif.setContentIntent(resultPendingIntent);
        return notif;
    }
}