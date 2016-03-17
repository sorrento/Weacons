package com.stupidpeople.weacons.WeaconRestaurant;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.StringUtils;
import com.stupidpeople.weacons.WeaconHelper;
import com.stupidpeople.weacons.WeaconParse;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import util.myLog;

/**
 * Created by Milenko on 16/03/2016.
 */
public class HelperRestaurant implements WeaconHelper {
    private final WeaconParse we;

    public HelperRestaurant(WeaconParse we) {
        this.we = we;
    }

    @Override
    public String typeString() {
        return "Restaurant";
    }

    @Override
    public boolean notificationRequiresFetching() {
        if (we.getName().equals("Versió Original")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ArrayList processResponse(Connection.Response response) {
        //TODO
        Document doc = null;
        ArrayList<ArrayList<String>> arrayGrande = null;
        try {
            try {
                doc = response.parse();

            } catch (IOException e) {
                e.printStackTrace();
            }

            Element cuadro = doc.select("div[id=content_area").first();
            Elements table = cuadro.select("div[class=n diyfeLiveArea]");
            arrayGrande = new ArrayList<>();
            ArrayList<String> arr = new ArrayList<>();

            for (Element el : table) {
                Element child = el.child(0);

                if (child.tagName().equals("h1")) {
                    if (arr.size() > 1) arrayGrande.add(arr);
                    arr = new ArrayList<>();
                    arr.add(child.text());
                } else if (child.tagName().equals("p")) {
                    for (Element hijo : el.children()) {
                        arr.add(hijo.text());
                    }
                } else {
                    continue;
                }

            }

            if (arr.size() > 1)
                arrayGrande.add(arr); //el primer campo de cada array tiene el titulo, tipo "postres"
        } catch (Exception e) {
            myLog.error(e);
        }


        return arrayGrande;
    }

    @Override
    public String getFetchingUrl() {
        return we.getString("FetchingUrl");
    }

    @Override
    public String NotiSingleCompactTitle() {
        return we.getName();
    }

    @Override
    public String NotiSingleCompactContent() {
        return we.getTypeString();
    }

    @Override
    public String NotiSingleExpandedTitle() {
        return we.getName();
    }

    @Override
    public String NotiSingleExpandedContent() {
//        if(fetched()) TODO, putdescription if not fetched

        //Formamos el texto

        StringBuilder sb = new StringBuilder();

        for (Object o : we.fetchedElements) {
            ArrayList<String> arr = (ArrayList<String>) o;
            for (String s :
                    arr) {
                sb.append(s + "|");
            }
            sb.append("||");
        }

        return sb.toString();
    }

    @Override
    public SpannableString NotiOneLineSummary() {
        return getOneLineSummary();
    }

    @Override
    public SpannableString getOneLineSummary() {
        String name;
        String greyPart = "popo"; //TODO put the fetching of the web

        if (we.getName().length() > 10) {
            name = we.getName().substring(0, 10) + ".";
        } else {
            name = we.getName();
        }

        return StringUtils.getSpannableString(name + " " + greyPart, name.length());//TODO put this method in common
    }

    @Override
    public Class getActivityClass() {
        return null;
    }

    @Override
    public Intent getResultIntent(Context mContext) {
        return null;
    }

    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        NotificationCompat.Builder notif;
        String title = NotiSingleCompactTitle();

        NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_volume_off_white_24dp, "Turn Off", resultPendingIntent);//TODO to create the silence intent

        notif = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notif_we)
                .setLargeIcon(we.getLogoRounded())
                .setContentTitle(title)
                .setContentText(NotiSingleCompactContent())
                .setAutoCancel(true)
                .addAction(actionSilence)
                .setTicker("Weacon detected\n" + we.getName());

        //Bigtext style

        String msg = NotiSingleExpandedContent();

        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        textStyle.setBigContentTitle(title);
        textStyle.bigText(msg);
        notif.setStyle(textStyle);

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }
        myLog.notificationMultiple(title, msg, "Currently " + LogInManagement.getActiveWeacons().size()
                + " weacons active", String.valueOf(false));

        notif.setContentIntent(resultPendingIntent);//TODO whath to do when they click on in the notification

        return notif;
    }
}
