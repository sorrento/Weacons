package com.stupidpeople.weacons.WeaconRestaurant;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.TextUtils;

import com.stupidpeople.weacons.HelperBaseFecthNotif;
import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.Notifications;
import com.stupidpeople.weacons.StringUtils;
import com.stupidpeople.weacons.WeaconParse;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import util.myLog;

/**
 * Created by Milenko on 18/03/2016.
 */
public class HelperRestaurant extends HelperBaseFecthNotif {

    public HelperRestaurant(WeaconParse weaconParse, Context ctx) {
        super(weaconParse, ctx);
    }

    @Override
    protected ArrayList processResponse(Connection.Response response) {
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
                    if (arr.size() > 0) arrayGrande.add(arr);
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
    protected NotificationCompat.InboxStyle getInboxStyle() {
        return null;
    }

    @Override
    protected String getFetchingFinalUrl() {
        return we.getFetchingPartialUrl();
    }

    @Override
    protected String typeString() {
        return "RESTAURANT";
    }

    @Override
    protected SpannableString NotiOneLineSummary() {
        String name;
        String greyPart = "See today's menu.";
        int len = 16;

        if (we.getName().length() > len) {
            name = we.getName().substring(0, len) + ".";
        } else {
            name = we.getName();
        }

        return StringUtils.getSpannableString(name + " " + greyPart, name.length());

    }

    @Override
    protected SpannableString NotiSingleExpandedContent() {
        if (we.fetchedElements == null || we.fetchedElements.size() == 0)
            return SpannableString.valueOf(we.getDescription());

        SpannableString sst = null;
        try {
            //TODO only en ciertas horas el menú, por la tarde poner la decripcion
            for (Object o : we.fetchedElements) {
                ArrayList<String> arr = (ArrayList<String>) o;

                String title = arr.get(0);
                StringBuilder sb = new StringBuilder(title + ": ");

                // Dishes
                for (int i = 1; i < arr.size() - 1; i++) {
                    sb.append(arr.get(i));
                    if (i < arr.size() - 2) {
                        sb.append(" | ");
                    }
                }

//                myLog.add("Hasta ahora:\n" + sb.toString(), "aut");

                SpannableString ssNew = StringUtils.getSpannableString(sb.toString(), title.length());

                if (sst == null) {
                    sst = ssNew;
                } else {
                    sst = SpannableString.valueOf(TextUtils.concat(sst, "\n", ssNew));
                }
            }
        } catch (Exception e) {
            myLog.error(e);
        }

        return sst;
    }

    @Override
    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext, boolean refreshButton) {
        String title = NotiSingleCompactTitle();
        String summary = Notifications.bottomMessage(mContext);
        NotificationCompat.Builder notif = baseNotif(mContext, sound, refreshButton);

        //Bigtext style
        SpannableString msg = NotiSingleExpandedContent();
        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle()
                .setBigContentTitle(title)
                .bigText(msg);
        if (LogInManagement.othersActive()) textStyle.setSummaryText(summary);

        notif.setStyle(textStyle);

        if (we.getFetchingPartialUrl() != null)
            Notifications.addRefreshButton(notif); //TODO consider restaurant that doesn need fecth in notification

        myLog.logNotification(title, String.valueOf(msg), summary, false, refreshButton, true);

        notif.setContentIntent(resultPendingIntent);

        return notif;
    }

}
