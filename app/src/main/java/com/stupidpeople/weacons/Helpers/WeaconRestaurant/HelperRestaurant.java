package com.stupidpeople.weacons.Helpers.WeaconRestaurant;

import android.content.Context;
import android.text.SpannableString;

import com.stupidpeople.weacons.Helpers.HelperBaseFecthNotif;
import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Helpers.fetchableElement;
import com.stupidpeople.weacons.R;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import util.StringUtils;
import util.myLog;

import static util.StringUtils.getSpannableString;
import static util.StringUtils.shorten;

/**
 * Created by Milenko on 18/03/2016.
 */
public class HelperRestaurant extends HelperBaseFecthNotif {

    public HelperRestaurant(WeaconParse weaconParse, Context ctx) {
        super(weaconParse, ctx);
    }

    @Override
    protected boolean doFetchingAEstaHora() {
        return true;
        //TODO
//        //entre las 10 y 16
//        Date date = new Date();   // given date
//        Calendar c = GregorianCalendar.getInstance(); // creates a new calendar instance
//        c.setTime(date);   // assigns calendar to given date
//
//        int h = c.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
//
//        return h > 10 && h < 16;
    }

    @Override
    protected long fetchedDataIsValidDuringMinutes() {
        return 60 * 5;
    }

    @Override
    protected ArrayList processResponse(Connection.Response response) {
        ArrayList<MealSection> arr = new ArrayList<>();

        try {
            Document doc = response.parse();
            Element cuadro = doc.select("div[id=content_area").first();
            Elements table = cuadro.select("div[class=n diyfeLiveArea]");

            MealSection mealSection = new MealSection("");
            for (Element el : table) {
                Element child = el.child(0);

                //Title
                if (child.tagName().equals("h1")) {
                    mealSection = new MealSection(child.text());

                } else if (child.tagName().equals("p")) {
                    for (Element hijo : el.children()) {
                        if (hijo.hasText())
                            mealSection.addDish(hijo.text());
                    }
                } else {
                    continue;
                }
                if (mealSection.isValid()) arr.add(mealSection);
            }

//
//            for (Element el : table) {
//                Element child = el.child(0);
//
//                if (child.tagName().equals("h1")) {
//                    if (arr.size() > 0) arrayGrande.add(arr);
//                    arr = new ArrayList<>();
//                    arr.add(child.text());
//                } else if (child.tagName().equals("p")) {
//                    for (Element hijo : el.children()) {
//                        arr.add(hijo.text());
//                    }
//                } else {
//                    continue;
//                }
//            }
//
//            if (arr.size() > 1)
//                arrayGrande.add(arr); //el primer campo de cada array tiene el titulo, tipo "postres"
//
        } catch (Exception e) {
            myLog.error(e);
        }
        return arr;
    }


    @Override
    protected String getFetchingFinalUrl() {
        return we.getFetchingPartialUrl();
    }

    @Override
    protected String typeString() {
        return "RESTAURANT";
    }
//    @Override
//    protected SpannableString NotiSingleContentExpanded() {
//        if (we.fetchedElements == null || we.fetchedElements.size() == 0)
//            return SpannableString.valueOf(we.getDescription());
//
//        SpannableString sst = null;
//        try {
//            //TODO only en ciertas horas el menú, por la tarde poner la decripcion
//            for (Object o : we.fetchedElements) {
//                ArrayList<String> arr = (ArrayList<String>) o;
//
//                String title = arr.get(0);
//                StringBuilder sb = new StringBuilder(title + ": ");
//
//                // Dishes
//                for (int i = 1; i < arr.size() - 1; i++) {
//                    sb.append(arr.get(i));
//                    if (i < arr.size() - 2) {
//                        sb.append(" | ");
//                    }
//                }
//
//                SpannableString ssNew = StringUtils.getSpannableString(sb.toString(), title.length());
//
//                sst = sst == null ? ssNew : SpannableString.valueOf(TextUtils.concat(sst, "\n", ssNew));
//            }
//        } catch (Exception e) {
//            myLog.error(e);
//        }
//
//        return sst;
//    }

//    @Override
//    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, Context mContext) {
//        mNotifTitle = NotiSingleTitle();
//        mNotifBottom = Notifications.bottomMessage(mContext);
//
//        NotifFeatures f = LogInManagement.notifFeatures;
//
//        NotificationCompat.Builder notif = baseNotif(mContext, f.sound, f.refreshButton);
//
//        //Bigtext style
//        mNotifContent = NotiSingleContentExpanded();
//        NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle()
//                .setBigContentTitle(mNotifTitle)
//                .bigText(mNotifContent);
//        if (LogInManagement.othersActive()) textStyle.setSummaryText(mNotifBottom);
//
//        notif.setStyle(textStyle);
//
//        if (we.getFetchingPartialUrl() != null)
//            Notifications.addRefreshButton(notif); //TODO consider restaurant that doesn need fecth in notification
//
////        myLog.logNotification(title, String.valueOf(msg), summary, false, refreshButton, true);
//
//        notif.setContentIntent(resultPendingIntent);
//
//        mBody = mNotifContent.toString();
//
//        return notif;
//    }


    // Notifications' messages
    @Override
    protected String msgRefreshing() {
        return mContext.getString(R.string.notif_restaurant_refreshing);
    }

    @Override
    protected String msgPressRefresh() {
        return mContext.getString(R.string.notif_restaurant_resfresh);
    }

    @Override
    protected String msgNoFetched() {
        return we.getDescription();
    }

    @Override
    protected SpannableString msgPressRefreshLong() {
        return SpannableString.valueOf(mContext.getString(R.string.refresh_menu_long));
    }

    class MealSection implements fetchableElement {

        private String title = "";
        private ArrayList<String> dishes;

        public MealSection(String section) {
            title = section;
            dishes = new ArrayList<>();
        }

        @Override
        public SpannableString oneLineSummary() {
            String name = shorten(title, 7);
            String gray = dishes.get(0);

            return StringUtils.getSpannableString(name + " " + gray, name.length());
        }

        @Override
        public String veryShortSummary() {
            return StringUtils.FirstWord(dishes.get(0));
        }

        public void addDish(String dish) {
            dishes.add(dish);
        }

        public boolean isValid() {
            return dishes.size() > 0 && !title.equals("");
        }

        public SpannableString getLongSpan() {
            String name = title;
            String gray = StringUtils.concatenate(dishes, "\n");
            return getSpannableString(name + "\n" + gray, name.length());
        }
    }
}
