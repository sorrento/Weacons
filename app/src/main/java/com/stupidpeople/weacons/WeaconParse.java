package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.stupidpeople.weacons.BusWeacon.WeaconBusStop;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;

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
public abstract class WeaconParse extends ParseObject {

    protected ArrayList fetchedElements;
    private String[] cards;

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

    public String getOneLineSummary() {
        if (getType().equals("bus_stop")) {
            //TODO
//            NewBusStop busStop = (NewBusStop) fetchedElements.get(0);
//            return busStop.summarizeAllLines(true);
            return null;
        } else {
            return "no summary";
        }
//        StringBuilder sb = new StringBuilder(getName());
//        if (this.notificationRequiresFetching()) {
//            formatter form = new formatter(fetchedElements);
//            sb.append(": " + form.summarizeAllLines(true));
//        }
//        return sb.toString();
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

        //TODO implement types of weacons as a hasthabels
        if (type.equals("bus_stop")) {
            sol = parameters.typeOfWeacon.bus_station;
        }

        return sol;
    }

    public String getTypeString() {
        return getString("Type");
    }

    //SETTERS
    public void setName(String name) {
        put("Name", name);
    }

    public WeaconParse build() {
        WeaconParse weres = this;
        try {
            switch (getType()) {
                case accounting:
                    break;
                case airport:
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
                    WeaconBusStop web = (WeaconBusStop) this;
                    return web;
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
                    weres = this;
                    break;
            }
        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
        return weres;
    }


    public static HashSet<WeaconParse> build(HashSet<WeaconParse> weaconHashSet) {
        HashSet<WeaconParse> res = new HashSet<>();
        for (WeaconParse we : weaconHashSet) res.add(we.build());
        return res;
    }

    // Set of methods to be implemented by each subclass
    protected abstract boolean notificationRequiresFetching();

    protected abstract ArrayList processResponse(String response);

    protected abstract String getFetchingUrl();

    protected abstract String NotiSingleCompactTitle();

    protected abstract String NotiSingleCompactContent();

    protected abstract String NotiSingleExpandedTitle();

    protected abstract String NotiSingleExpandedContent();

    protected abstract String NotiOneLineSummary();

    protected abstract NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext);

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

    public Bitmap getLogoRounded() {
        Bitmap bm = getLogo();
        Bitmap logoRounded = stringUtils.drawableToBitmap(new RoundImage(bm));

        return logoRounded;
    }


    public String getMessage() {
        String message = getString("Description");
        return message;
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

    public String getImageParseUrl() {
        return getParseFile("Logo").getUrl();
    }

    public ParseGeoPoint getGPS() {
        return getParseGeoPoint("GPS");
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


//    public void setFetchingUrl(String fetchingUrl) {
//        this.fetchingUrl = fetchingUrl;
//    }
}
