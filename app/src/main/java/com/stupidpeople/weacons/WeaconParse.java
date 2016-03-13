package com.stupidpeople.weacons;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.stupidpeople.weacons.WeaconBus.Bus;
import com.stupidpeople.weacons.WeaconBus.BusLine;
import com.stupidpeople.weacons.WeaconBus.SantCugat.BusLineStCugat;
import com.stupidpeople.weacons.WeaconBus.SantCugat.BusStCugat;
import com.stupidpeople.weacons.WeaconBus.Santiago.BusLineSantiago;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import util.RoundImage;
import util.myLog;
import util.parameters;
import util.stringUtils;

/**
 * Created by Milenko on 30/07/2015.
 */
@ParseClassName("Weacon")
public class WeaconParse extends ParseObject {
    private WeaconHelper mHelper;

    protected ArrayList fetchedElements;
    private String[] cards;
    protected boolean obsolete = false;

    public WeaconParse() {
    }

    //STRINGS
    public static String Listar(HashSet<WeaconParse> weacons) {
        StringBuilder sb = new StringBuilder();
        for (WeaconParse we : weacons) {
            sb.append(we.getName() + " | ");
        }
        return sb.toString();
    }

    public static String Listar(HashMap<WeaconParse, Integer> hash) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<WeaconParse, Integer> entry : hash.entrySet()) {
            sb.append(entry.getKey().getName() + ":" + entry.getValue() + " | ");
        }
        return sb.toString();
    }

    public static String Listar(ArrayList<WeaconParse> weacons) {
        StringBuilder sb = new StringBuilder();
        for (WeaconParse we : weacons) {
            sb.append(we.getName() + " | ");
        }
        return sb.toString();
    }

    public static String Listar(List<WeaconParse> list) {
        StringBuilder sb = new StringBuilder();
        for (WeaconParse we :
                list) {
            sb.append(we.getName() + " | ");
        }
        return sb.toString();
    }


    // Comparison of WeaconParse
    @Override
    public int hashCode() {
        return getObjectId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof WeaconParse)) return false;

        WeaconParse other = (WeaconParse) o;
        return this.getObjectId() == other.getObjectId();
    }

    //GETTERS

    public String getName() {
        return getString("Name");
    }

    public String getUrl() {
        return getString("MainUrl");
    }

    public parameters.typeOfWeacon getType() {
        String type = getString("Type");
        parameters.typeOfWeacon sol = parameters.typeOfWeacon.nothing;
        myLog.add("====" + getName() + "|" + type, "aut");
        //TODO implement types of weacons as a hasthabels
        if (type.equals("bus_stop")) {
            sol = parameters.typeOfWeacon.bus_station;
        } else if (type.equals("AIRPORT")) {
            sol = parameters.typeOfWeacon.airport;
        }

        return sol;
    }

    public String getTypeString() {
        return getString("Type");
    }

    public Bitmap getLogo() {
        Bitmap bm = null;
        try {
            ParseFile parseFile = getParseFile("Logo");
            byte[] bitmapdata = new byte[0];
            bitmapdata = parseFile.getData();
            bm = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bm;
    }

    public Bitmap getLogoRounded() {
        Bitmap bm = getLogo();
        Bitmap logoRounded = stringUtils.drawableToBitmap(new RoundImage(bm));

        return logoRounded;
    }

    public ParseGeoPoint getGPS() {
        return getParseGeoPoint("GPS");
    }


    //SETTERS
    public void setName(String name) {
        put("Name", name);
    }

    public WeaconParse build() {
        try {
            switch (getType()) {
                case accounting:
                    break;
                case airport:
                    this.setHelper(new HelperAirport());
                    break;
                case amusement_park:
                    break;
                case aquarium:
                    break;
                case art_gallery:
                    break;
                case atm:
                    break;
                case bakery:
                    break;
                case bank:
                    break;
                case bar:
                    break;
                case beauty_salon:
                    break;
                case bicycle_store:
                    break;
                case book_store:
                    break;
                case bowling_alley:
                    break;
                case bus_station:
                    this.setHelper(new HelperBus());
                    break;
                //            case cafe:
                //                break;
                //            case campground:
                //                break;
                //            case car_dealer:
                //                break;
                //            case car_rental:
                //                break;
                //            case car_repair:
                //                break;
                //            case car_wash:
                //                break;
                //            case casino:
                //                break;
                //            case cemetery:
                //                break;
                //            case church:
                //                break;
                //            case city_hall:
                //                break;
                //            case clothing_store:
                //                break;
                //            case convenience_store:
                //                break;
                //            case courthouse:
                //                break;
                //            case dentist:
                //                break;
                //            case department_store:
                //                break;
                //            case doctor:
                //                break;
                //            case electrician:
                //                break;
                //            case electronics_store:
                //                break;
                //            case embassy:
                //                break;
                //            case establishment:
                //                break;
                //            case finance:
                //                break;
                //            case fire_station:
                //                break;
                //            case florist:
                //                break;
                //            case food:
                //                break;
                //            case funeral_home:
                //                break;
                //            case furniture_store:
                //                break;
                //            case gas_station:
                //                break;
                //            case general_contractor:
                //                break;
                //            case grocery_or_supermarket:
                //                break;
                //            case gym:
                //                break;
                //            case hair_care:
                //                break;
                //            case hardware_store:
                //                break;
                //            case health:
                //                break;
                //            case hindu_temple:
                //                break;
                //            case home_goods_store:
                //                break;
                //            case hospital:
                //                break;
                //            case insurance_agency:
                //                break;
                //            case jewelry_store:
                //                break;
                //            case laundry:
                //                break;
                //            case lawyer:
                //                break;
                //            case library:
                //                break;
                //            case liquor_store:
                //                break;
                //            case local_government_office:
                //                break;
                //            case locksmith:
                //                break;
                //            case lodging:
                //                break;
                //            case meal_delivery:
                //                break;
                //            case meal_takeaway:
                //                break;
                //            case mosque:
                //                break;
                //            case movie_rental:
                //                break;
                //            case movie_theater:
                //                break;
                //            case moving_company:
                //                break;
                //            case museum:
                //                break;
                //            case night_club:
                //                break;
                //            case painter:
                //                break;
                //            case park:
                //                break;
                //            case parking:
                //                break;
                //            case pet_store:
                //                break;
                //            case pharmacy:
                //                break;
                //            case physiotherapist:
                //                break;
                //            case place_of_worship:
                //                break;
                //            case plumber:
                //                break;
                //            case police:
                //                break;
                //            case post_office:
                //                break;
                //            case real_estate_agency:
                //                break;
                //            case restaurant:
                //                break;
                //            case roofing_contractor:
                //                break;
                //            case rv_park:
                //                break;
                //            case school:
                //                break;
                //            case shoe_store:
                //                break;
                //            case shopping_mall:
                //                break;
                //            case spa:
                //                break;
                //            case stadium:
                //                break;
                //            case storage:
                //                break;
                //            case store:
                //                break;
                //            case subway_station:
                //                break;
                //            case synagogue:
                //                break;
                //            case taxi_stand:
                //                break;
                //            case train_station:
                //                break;
                //            case travel_agency:
                //                break;
                //            case university:
                //                break;
                //            case veterinary_care:
                //                break;
                //            case zoo:
                //                break;
                case nothing:
                    break;
            }
        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
        return this;
    }

    private void setHelper(WeaconHelper helper) {
        myLog.add("Setting helper", "aut");
        mHelper = helper;
    }

    public String whatIam() {
        return mHelper.whatImI();
    }

    public static HashSet<WeaconParse> build(HashSet<WeaconParse> weaconHashSet) {
        HashSet<WeaconParse> weaconHashSet2 = new HashSet<>();
        try {
            for (WeaconParse we : weaconHashSet) {
                WeaconParse we2 = we.build();
                weaconHashSet2.add(we2);
                myLog.add("****el wwacon que estamos procesando es " + we2.getName() + " y soy " + we2.whatIam(), "aut");
            }
        } catch (Exception e) {
            myLog.error(e);
        }
        return weaconHashSet2;//TODO be sure that modifyin the object tehe element in the hash are modified
    }

    /////////////////////////////////////////////////////

    public boolean notificationRequiresFetching() {
        return mHelper.notificationRequiresFetching();
    }

    public ArrayList processResponse(String response) {
        return mHelper.processResponse(response);
    }

    public String getFetchingUrl() {
        return mHelper.getFetchingUrl();
    }

    public String NotiSingleCompactTitle() {
        return mHelper.NotiSingleCompactTitle();
    }

    public String NotiSingleCompactContent() {
        return mHelper.NotiSingleCompactContent();
    }

    public String NotiSingleExpandedTitle() {
        return mHelper.NotiSingleExpandedTitle();
    }

    public String NotiSingleExpandedContent() {
        return mHelper.NotiSingleExpandedContent();
    }

    public SpannableString NotiOneLineSummary() {
        return mHelper.NotiOneLineSummary();
    }

    public SpannableString getOneLineSummary() {
        return mHelper.getOneLineSummary();
    }

    public Class getActivityClass() {
        return mHelper.getActivityClass();
    }

    public Intent getResultIntent(Context mContext) {
        return mHelper.getResultIntent(mContext);
    }

    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
        return mHelper.buildSingleNotification(resultPendingIntent, sound, mContext);
    }

    /////////////////////////////////////////////////////

    public void fetchForNotification(final MultiTaskCompleted fetchedElementListener) {

        fetchingResults elementsListener = new fetchingResults() {
            @Override
            public void onReceive(Connection.Response response) {
                fetchedElements = processResponse(response.body());
                fetchedElementListener.OneTaskCompleted();
            }

            @Override
            public void onError(Exception e) {
                myLog.error(e);
            }
        };

        (new fetchNotificationWeacon(getFetchingUrl(), elementsListener)).execute();

    }

    /**
     * Put a new message for notification, without data
     */
    protected void setObsolete() {
        obsolete = true;
    }

    /*
    TODO clean from here
     */


    public String[] getCards() {

        try {
            List<Object> al = getList("cards");
            cards = new String[al.size()];
            al.toArray(cards);
        } catch (Exception e) {
            myLog.add("--error: ther is no definition of cards in parse: " + e.getLocalizedMessage(), "aut");
        }
        return cards;
    }

    public String getMessage() {
        String message = getString("Description");
        return message;
    }


    public String getImageParseUrl() {
        return getParseFile("Logo").getUrl();
    }

    // OTHER

    public int getRepeatedOffRemoveFromNotification() {
        int res;
        if (getType().equals("bus_stop")) {
            res = 1;
        } else {
            res = parameters.repeatedOffRemoveFromNotification;
        }
        return res;
    }

    public boolean near(ParseGeoPoint point, int kms) {
        return getGPS().distanceInKilometersTo(point) < kms;
    }

    public static String ListarSR(List<ScanResult> sr) {
        StringBuilder sb = new StringBuilder();
        for (ScanResult s : sr) {
            sb.append(s.SSID + " | " + s.BSSID + "\n");
        }
        return sb.toString();
    }

    // Specific Helpers for specific weacons

    private class HelperBus implements WeaconHelper {
        //TODO sort methods
        String updateTime;
        String stopCode;
        private String description;
        private String sInbox;

        @Override
        public String whatImI() {
            return "SOY UN BUS";
        }

        @Override
        public boolean notificationRequiresFetching() {
            return true;
        }

        @Override
        public ArrayList processResponse(String response) {
            ArrayList arr = new ArrayList();

            if (near(parameters.stCugat, 20)) {
                arr = processStCugat(response);
            } else if (near(parameters.santiago, 20)) {
                arr = processSantiago(response);
            }
            return arr;

        }

        @Override
        public String getFetchingUrl() {
            return getString("FetchingUrl") + getBusStopId();
        }

        @Override
        public String NotiSingleCompactTitle() {
            return getName();
        }

        @Override
        public String NotiSingleCompactContent() {
            String s;
            if (obsolete) {
                s = "Press REFRESH Button";
            } else {
                s = "BUS STOP. " + summarizeAllLines();
            }
            return s;
        }

        @Override
        public String NotiSingleExpandedTitle() {
            return getName();
        }

        @Override
        public String NotiSingleExpandedContent() {
            String msg = "Please press REFRESH \nto have updated information about the estimated " +
                    "arrival times of buses at this stop.";
            return msg;
        }

        @Override
        public SpannableString NotiOneLineSummary() {
            return getOneLineSummary();
        }

        @Override
        public SpannableString getOneLineSummary() {
            String name;

            if (getName().length() > 10) {
                name = getName().substring(0, 10) + ".";
            } else {
                name = getName();
            }

            return getSpannableString(name + " " + summarizeAllLines(), name.length());
        }

        @Override
        public Class getActivityClass() {
            //        Class<?> cls = CardActivity.class;//TODO poner la class correcto
            return myLog.class;
        }

        @Override
        public Intent getResultIntent(Context mContext) {
            Intent intent = new Intent(mContext, getActivityClass())//TODO Asegurarse que todos los weacon mandan mismos nombrees de extras
                    .putExtra("wName", getName())
                    .putExtra("wWeaconObId", getObjectId())
                    .putExtra("wLogo", getLogoRounded())
                    .putExtra("wFetchingUrl", getFetchingUrl());

            return intent;
        }

        @Override
        public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext) {
            NotificationCompat.Builder notif;
            String title = NotiSingleCompactTitle();

            Intent refreshIntent = new Intent("popo"); //TODO poner el reciever de esto, para que refresque la notif
            PendingIntent resultPendingIntentRefresh = PendingIntent.getBroadcast(mContext, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action actionRefresh = new NotificationCompat.Action(R.drawable.ic_refresh_white_24dp, "Refresh", resultPendingIntentRefresh);
            NotificationCompat.Action actionSilence = new NotificationCompat.Action(R.drawable.ic_volume_off_white_24dp, "Turn Off", resultPendingIntent);//TODO to create the silence intent

            notif = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_notif_we)
                    .setLargeIcon(getLogoRounded())
                    .setContentTitle(title)
                    .setContentText(NotiSingleCompactContent())
                    .setAutoCancel(true)
                    .addAction(actionSilence)
                    .addAction(actionRefresh);


            if (obsolete) {
                //Bigtext style

                String msg = NotiSingleExpandedContent();
                NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
                textStyle.setBigContentTitle(title);
                textStyle.bigText(msg);
                notif.setStyle(textStyle);

                myLog.notificationMultiple(title, msg, "Currently " + LogInManagement.getActiveWeacons().size()
                        + " weacons active", String.valueOf(false));
            } else {
                //InboxStyle
                notif.setTicker("Weacon detected\n" + getName());
                if (sound) {
                    notif.setLights(0xE6D820, 300, 100)
                            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);
                }
                notif.setStyle(getInboxStyle());

                myLog.notificationMultiple(title, sInbox, "Currently " + LogInManagement.getActiveWeacons().size()
                        + " weacons active", String.valueOf(sound));
            }

            notif.setContentIntent(resultPendingIntent);//TODO whath to do when they click on in the notification

            return notif;

        }


        private ArrayList processSantiago(String response) {
            ArrayList<BusLine> arr = new ArrayList<>();

            try {
                JSONObject json = new JSONObject(response);
                stopCode = json.getString("paradero");
                description = json.getString("nomett");
                updateTime = json.getString("fechaprediccion") + "|" + json.getString("horaprediccion");

                JSONObject services = json.getJSONObject("servicios");
                JSONArray items = services.getJSONArray("item");
                myLog.add("tenemos algunso servicios de bus:" + items.length(), "aut");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    if (!(item.getString("codigorespuesta").equals("00") || item.getString("codigorespuesta").equals("01")))
                        continue;
                    myLog.add("oneitem: " + item.toString(), "aut");
                    BusLineSantiago line = new BusLineSantiago(item);
                    arr.add(line);
                }

            } catch (JSONException e) {
                myLog.error(e);
            }
            return arr;

        }

        private ArrayList processStCugat(String response) {
            HashMap<String, BusLine> tableLines = new HashMap<>();

            try {
                JSONArray mJsonArray = new JSONArray(response);

                for (int i = 0; i < mJsonArray.length(); i++) {
                    JSONObject json = mJsonArray.getJSONObject(i);
                    BusStCugat bus = new BusStCugat(json);

                    if (stopCode == null) { //put this info in the stop, only once
                        stopCode = bus.getStopCode();
                        updateTime = bus.getUpdateTime();
                    }

                    String lineCode = bus.getLineCode();
                    if (tableLines.containsKey(lineCode)) {
                        BusLine busLine = tableLines.get(lineCode);
                        busLine.addBus(bus);
                    } else {
                        tableLines.put(lineCode, new BusLineStCugat(lineCode, bus));
                    }
                }
            } catch (Exception e) {
                myLog.error(e);
            }

            ArrayList<BusLine> arr = new ArrayList<>();
            for (BusLine line : tableLines.values()) {
                arr.add(line);
            }
            return arr;
        }

        private NotificationCompat.InboxStyle getInboxStyle() {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(NotiSingleExpandedTitle());
            inboxStyle.setSummaryText("Currently " + LogInManagement.getActiveWeacons().size() + " weacons active");


            StringBuilder sb = new StringBuilder();
            for (SpannableString s : summarizeByOneLine()) {
                inboxStyle.addLine(s);
                sb.append("   " + s + "\n");
            }
            sInbox = sb.toString();
            return inboxStyle;
        }

        /**
         * Array with strings that summarizes each line: L1: 12 min, 18 min, 35 min
         * ideal for single notification (inbox format)
         *
         * @return
         */
        public ArrayList<SpannableString> summarizeByOneLine() {
            ArrayList<SpannableString> arr = new ArrayList<>();

            if (fetchedElements == null || fetchedElements.size() == 0) {
                arr.add(new SpannableString("No info for this stop by now."));
            } else {

                for (Object o : fetchedElements) {
                    BusLine lt = (BusLine) o;
                    String name = lt.lineCode;

                    StringBuilder sb = new StringBuilder(name + " ");

                    for (Bus bus : lt.buses) {
                        sb.append(bus.arrivalTimeText + ", ");
                    }

                    String s = sb.toString();
                    String sub = s.substring(0, s.length() - 2);

                    arr.add(getSpannableString(sub, name.length()));

                }
            }
            return arr;
        }

        /**
         * The first m characters are BOLD and slighty bigger
         *
         * @param text
         * @param m
         * @return
         */
        @NonNull
        private SpannableString getSpannableString(String text, int m) {
            //TODO move to string utils
            SpannableString span = new SpannableString(text);

            span.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new RelativeSizeSpan(1.1f), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            return span;
        }

        public String getBusStopId() {
            return getString("paradaId");
        }

        /**
         * Shows only the first arrival by line:  L1:10m | B3: 5m | R4:18m
         *
         * @param compact for having L1:10|B3:5|R4:18
         * @return
         */
        public String summarizeAllLines(boolean compact) {
            String substring = "No info Available";

            int del = 0;

            if (fetchedElements == null) return "No lines available";

            if (fetchedElements.size() > 0) {
                StringBuilder sb = new StringBuilder();

                for (Object o : fetchedElements) {
                    BusLine line = (BusLine) o;
                    String name = line.lineCode;

                    if (compact) {
                        sb.append(name + ":" + line.getShortestTime() + "|");
                        del = 1;
                    } else {
                        sb.append(name + ": " + line.getShortestTime() + "m | ");
                        del = 2;
                    }
                }
                String s = sb.toString();
                substring = s.substring(0, s.length() - del);
            }

            return substring;
        }

        public String summarizeAllLines() {
            return summarizeAllLines(false);
        }


    }

    private class HelperAirport implements WeaconHelper {
        @Override
        public String whatImI() {
            return "SOY UN AEROPUERTO";
        }

        @Override
        public boolean notificationRequiresFetching() {
            return false;
        }

        @Override
        public ArrayList processResponse(String response) {
            return null;
        }

        @Override
        public String getFetchingUrl() {
            return null;
        }

        @Override
        public String NotiSingleCompactTitle() {
            return null;
        }

        @Override
        public String NotiSingleCompactContent() {
            return null;
        }

        @Override
        public String NotiSingleExpandedTitle() {
            return null;
        }

        @Override
        public String NotiSingleExpandedContent() {
            return null;
        }

        @Override
        public SpannableString NotiOneLineSummary() {
            return null;
        }

        @Override
        public SpannableString getOneLineSummary() {
            return null;
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
            return null;
        }
    }
}

