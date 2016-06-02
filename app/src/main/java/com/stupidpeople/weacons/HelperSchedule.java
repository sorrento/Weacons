package com.stupidpeople.weacons;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.TextUtils;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import util.myLog;

/**
 * Created by Milenko on 31/05/2016.
 */
public class HelperSchedule extends HelperBaseFecthNotif {
    protected HelperSchedule(WeaconParse we, Context ctx) {
        super(we, ctx);
    }

    @Override
    protected ArrayList processResponse(Connection.Response response) {
        if (response == null) return null;
        ArrayList arr = new ArrayList();

        try {
            //see http://intranet.esade.edu/web1/pkg_pantalles.info_layer?ample=500&alt=901&segons=0&edifici=8
            Document doc = response.parse();
            Elements clases = doc.select("table[class=item]");
            for (Element clase : clases) arr.add(new Lesson(clase));
        } catch (Exception e) {
            myLog.error(e);
        }
        return arr;
    }

    @Override
    protected NotificationCompat.InboxStyle getInboxStyle() {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(NotiSingleExpandedTitle());
        if (LogInManagement.othersActive())
            inboxStyle.setSummaryText(Notifications.bottomMessage(mContext));

        StringBuilder sb = new StringBuilder();
        for (SpannableString s : summarizeByClass()) {
            inboxStyle.addLine(s);
            sb.append("   " + s + "\n");
        }
        sInbox = sb.toString();
        return inboxStyle;
    }

    private ArrayList<SpannableString> summarizeByClass() {
        ArrayList<SpannableString> arr = new ArrayList<>();

        if (we.refreshing) {
            arr.add(new SpannableString(mContext.getString(R.string.refreshing)));
        } else if (we.fetchedElements == null || we.fetchedElements.size() == 0) {
            arr.add(new SpannableString("No Info"));
        } else {

            for (Object o : we.fetchedElements) {
                Lesson lt = (Lesson) o;
                String name = lt.getTitle();

                StringBuilder sb = new StringBuilder(name + " ");

//                for (Bus bus : lt.buses) {
//                    sb.append(bus.arrivalTimeText + ", ");
//                }
                sb.append(lt.getHora() + " | " + lt.getAula());

                String s = sb.toString();
                String sub = s.substring(0, s.length() - 2);

                arr.add(StringUtils.getSpannableString(sub, name.length()));

            }
        }
        return arr;

    }

    @Override
    protected String getFetchingFinalUrl() {
        return we.getFetchingPartialUrl() + we.getParadaId();
    }

    @Override
    protected String typeString() {
        return "SCHEDULE";
    }

    @Override
    protected SpannableString NotiOneLineSummary() {
        String name;
        String greyPart;
        //TODo parte de esto debería estar en comun
        name = StringUtils.shorten(we.getName(), 15);

        if (we.refreshing) {
            greyPart = mContext.getString(R.string.refreshing);
        } else if (we.obsolete) {
            greyPart = we.getTypeString() + ". " + mContext.getString(R.string.press_refresh);
        } else {
            greyPart = summarizeFirstLessons();
        }
        return StringUtils.getSpannableString(name + " " + greyPart, name.length());

    }

    private String summarizeFirstLessons() {
        String substring = mContext.getString(R.string.press_refresh);
        //TODO esto se debería poner en general
        if (we.fetchedElements == null)
            substring = mContext.getString(R.string.press_refresh);

        else if (we.fetchedElements.size() == 0)
            substring = "No schedule available";

        else if (we.fetchedElements.size() > 0) {

            Lesson lesson = (Lesson) we.fetchedElements.get(0);
            substring = lesson.getHora() + "|" + lesson.getTitle() + "|" + lesson.getAula();
        }

        return substring;
    }

    @Override
    protected SpannableString NotiSingleExpandedContent() {
        SpannableString msg = new SpannableString("");
        try {
            if (we.refreshing)
                return SpannableString.valueOf(mContext.getString(R.string.refreshing));

            if (we.obsolete) {
                msg = SpannableString.valueOf(mContext.getString(R.string.press_refresh_bus_long));
            } else {
                for (SpannableString s : summarizeByClass()) {
                    msg = SpannableString.valueOf(TextUtils.concat(msg, "\n", s));
                }
            }
        } catch (Exception e) {
            myLog.error(e);
        }
        return msg;

    }

    private class Lesson {
        String otro = null, aula = null, imgUrl = null;
        String hora;
        String title;
        String assig;


        private Lesson(Element clase) {

            //        hora = clase.select("td[class=horatd]").first().text();
            hora = clase.select("font[class=hora]").first().text();
            title = clase.select("font[class=programa]").first().text();
            assig = clase.select("font[class=assig]").first().text();
            Elements otroEl = clase.select("font[class=ealsec]");
            Elements aulaEl = clase.select("td[class=aulas]");
            Elements imgUrlEl = clase.select("img");
            if (otroEl.size() > 0) {
                otro = otroEl.first().text();
            }
            if (aulaEl.size() > 0) {
                aula = aulaEl.first().text();

            }
            if (imgUrlEl.size() > 0) {
                imgUrl = imgUrlEl.first().attr("scr");
            }

            myLog.add(toString(), "NOTIF");

        }

        @Override
        public String toString() {
            return "Lesson{" +
                    "otro='" + otro + '\'' +
                    ", aula='" + aula + '\'' +
                    ", imgUrl='" + imgUrl + '\'' +
                    ", hora='" + hora + '\'' +
                    ", title='" + title + '\'' +
                    ", assig='" + assig + '\'' +
                    '}';
        }

        public String getOtro() {
            return otro;
        }

        public String getAula() {
            return aula;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public String getHora() {
            return hora;
        }

        public String getTitle() {
            return title;
        }

        public String getAssig() {
            return assig;
        }
    }
}
