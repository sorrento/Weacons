package com.stupidpeople.weacons.WeaconRestaurant;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

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
public class HelperRestaurantOLD implements WeaconHelper {
    private final WeaconParse we;

    public HelperRestaurantOLD(WeaconParse we) {
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
    public String getFetchingFinalUrl() {
        return we.getFetchingPartialUrl();
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
    public SpannableString NotiSingleExpandedContent() {
//        if(fetched()) TODO, putdescription if not fetched
        //TODO only en ciertas horas el menú, por la tarde poner la decripcion

        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLACK);
        SpannableString sst = null;

        for (Object o : we.fetchedElements) {
            ArrayList<String> arr = (ArrayList<String>) o;

            String title = arr.get(0);
            StringBuilder sb = new StringBuilder(title + ": ");

            // Dishes
            for (int i = 1; i < arr.size() - 1; i++) {
                sb.append(arr.get(i));
                if (i < arr.size() - 2) {
                    sb.append((" | "));
                }
            }

            myLog.add("Hasta ahora:\n" + sb.toString(), "aut");


            SpannableString ssNew = StringUtils.getSpannableString(sb.toString(), title.length());

            if (sst == null) {
                sst = ssNew;
            } else {
                sst = SpannableString.valueOf(TextUtils.concat(sst, "\n", ssNew));
            }

        }

        return sst;
    }

    @Override
    public SpannableString NotiOneLineSummary() {
        return getOneLineSummary();
    }

    @Override
    public SpannableString getOneLineSummary() {
        String name;
        String greyPart = "See today's menu.";
        int len = 16;

        if (we.getName().length() > len) {
            name = we.getName().substring(0, len) + ".";
        } else {
            name = we.getName();
        }

        return StringUtils.getSpannableString(name + " " + greyPart, name.length());//TODO put this method in common
    }

    @Override
    public Class getActivityClass() {
        return CardsActivity.class;
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

        SpannableString msg = NotiSingleExpandedContent();

        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
        textStyle.setBigContentTitle(title);
        textStyle.bigText(msg);
        notif.setStyle(textStyle);

        if (sound) {
            notif.setLights(0xE6D820, 300, 100)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
        }
        myLog.notificationMultiple(title, String.valueOf(msg), "Currently " + LogInManagement.getActiveWeacons().size()
                + " weacons active", String.valueOf(false));

        notif.setContentIntent(resultPendingIntent);//TODO whath to do when they click on in the notification

        return notif;
    }
}