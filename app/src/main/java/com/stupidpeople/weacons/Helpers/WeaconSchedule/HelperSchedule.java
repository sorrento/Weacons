package com.stupidpeople.weacons.Helpers.WeaconSchedule;

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

/**
 * Created by Milenko on 31/05/2016.
 */
public class HelperSchedule extends HelperBaseFecthNotif {
    public HelperSchedule(WeaconParse we, Context ctx) {
        super(we, ctx);
    }

    @Override
    protected long fetchedDataIsValidDuringMinutes() {
        return 30;
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
    protected String getFetchingFinalUrl() {
        return we.getFetchingPartialUrl() + we.getParadaId();
    }

    @Override
    protected SpannableString msgPressRefreshLong() {
        return SpannableString.valueOf(mContext.getString(R.string.pulldown_refresh_Schedule));
    }

    @Override
    protected String msgRefreshing() {
        return mContext.getString(R.string.getting_schedule);
    }

    @Override
    protected String msgPressRefresh() {
        return mContext.getString(R.string.refresh_to_schedule);
    }

    @Override
    protected String typeString() {
        return "SCHEDULE";
    }

    private class Lesson implements fetchableElement {
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

        public String getHour() {
            return hora;
        }

        public String getTitle() {
            return title;
        }

        public String getAssig() {
            return assig;
        }

        //    Lesson{otro='(Edición:2)', aula='Aula:204N,203N', imgUrl='null', hora='18:45', title='Master in Finance Curso 1', assig='Spanish Language and Culture 2 (part 2) (Edición:2)'}

        @Override
        public SpannableString oneLineSummary() {
            String hour = getHour();
            String s = hour + " " + StringUtils.shorten(getAssig(), 15) + " | " + getAula();

            //  We want in one line: 18.45 Spanish La. Aula:204N,203N
            return StringUtils.getSpannableString(s, hour.length());

        }

        @Override
        public SpannableString getLongSpan() {
            String name = getHour();
            String extra = getOtro() == null ? "" : " | " + getOtro();

            String margin = "\n\t\t\t";
            String gray = getAssig() + margin + getTitle() + margin + getAula() + extra;

            return StringUtils.getSpannableString(name + " " + gray, name.length());
        }

        @Override
        public String veryShortSummary() {
            return getHour() + " " + getAssig();
        }
    }
}
