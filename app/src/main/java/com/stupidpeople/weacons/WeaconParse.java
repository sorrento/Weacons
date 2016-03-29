package com.stupidpeople.weacons;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.stupidpeople.weacons.WeaconAirport.HelperAiport2;
import com.stupidpeople.weacons.WeaconBus.HelperBus;
import com.stupidpeople.weacons.WeaconRestaurant.HelperRestaurant;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;

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
    static boolean forceFetching = false;
    public ArrayList fetchedElements;
    public boolean obsolete = false;
    private HelperBase mHelper;
    private String[] cards;
    private boolean isInteresting;

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

    public static void build(HashSet<WeaconParse> weaconHashSet, Context ctx) {
        HashSet<WeaconParse> weaconHashSet2 = new HashSet<>();
        try {
            for (WeaconParse we : weaconHashSet) {
                we.build(ctx);
            }
        } catch (Exception e) {
            myLog.error(e);
        }
    }

    public static String ListarSR(List<ScanResult> sr) {
        StringBuilder sb = new StringBuilder();
        for (ScanResult s : sr) {
            sb.append(s.SSID + " | " + s.BSSID + " | " + s.level + "\n");
        }
        return sb.toString();
    }

    //GETTERS

    public static void SetObsolete(ArrayList<WeaconParse> weaconsList, boolean b) {
        for (WeaconParse we : weaconsList) {
            we.setObsolete(b);
        }
    }

    public static void ForceFetchingNextTime(ArrayList<WeaconParse> weaconsToNotify) {
        forceFetching = true;
    }

    public String getDescription() {
        String message = getString("Description");
        return message;
    }

    public String getImageParseUrl() {
        return getParseFile("Logo").getUrl();
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

    public String getName() {
        return getString("Name");
    }

    //SETTERS
    public void setName(String name) {
        put("Name", name);
    }

    public String getUrl() {
        return getString("MainUrl");
    }

    public parameters.typeOfWeacon getType() {
        String type = getString("Type");
        parameters.typeOfWeacon sol = parameters.typeOfWeacon.nothing;
        if (type.equals("bus_stop")) {
            sol = parameters.typeOfWeacon.bus_station;
        } else if (type.equals("AIRPORT")) {
            sol = parameters.typeOfWeacon.airport;
        } else if (type.equals("restaurant")) {
            sol = parameters.typeOfWeacon.restaurant;
        }

        return sol;
    }

    public String getTypeString() {
        return mHelper.typeString();
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

    ///////////////////////////////////////////////////// DELEGATES

    public WeaconParse build(Context ctx) {
        try {
            switch (getType()) {
                case accounting:
                    break;
                case airport:
                    mHelper = new HelperAiport2(this, ctx);
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
                    mHelper = new HelperBus(this, ctx);
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
                case restaurant:
                    if (getFetchingPartialUrl() == null) {
                        //TODO solve n parese: separar el feching de notifiacacion por el de cartas
                        mHelper = new HelperDefault(this, ctx);
                    } else {
                        mHelper = new HelperRestaurant(this, ctx);
                    }
                    break;
//                            case roofing_contractor:
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
                    mHelper = new HelperDefault(this, ctx);
                    break;
            }

            isInteresting = ParseActions.isInteresting(getObjectId());

        } catch (Exception e) {
            myLog.add(Log.getStackTraceString(e), "err");
        }
        return this;
    }

    public boolean notificationRequiresFetching() {
        return mHelper.notificationRequiresFetching();
    }

    public ArrayList processResponse(Connection.Response response) {
        if (mHelper.notificationRequiresFetching()) {
            return ((HelperBaseFecthNotif) mHelper).processResponse(response);
        } else {
            myLog.add("Este weacon no tiene fetching" + getName(), "WARN");
            return null;
        }
    }

    public String getFetchingFinalUrl() {
        if (mHelper.notificationRequiresFetching()) {
            return ((HelperBaseFecthNotif) mHelper).getFetchingFinalUrl();
        } else {
            myLog.add("Este weacon no tiene fetching" + getName(), "WARN");
            return null;
        }
    }

    public String getFetchingPartialUrl() {
        return getString("FetchingUrl");
    }

    public SpannableString NotiSingleExpandedContent() {

        if (mHelper == null) {
            String s = "Sorry, mhelper=null";
            myLog.add(s, "WARN");
            return SpannableString.valueOf(s);
        } else {
            return mHelper.NotiSingleExpandedContent();
        }
    }

    public SpannableString NotiOneLineSummary() {
        return mHelper.NotiOneLineSummary();
    }


    /////////////////////////////////////////////////////

    /**
     * the activity should be open for this Weacon. can be Cards o Browser
     *
     * @return
     */
    public Class getActivityClass() {
        return mHelper.getActivityClass();
    }

    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, boolean sound, Context mContext, boolean isInteresting) {
        return mHelper.buildSingleNotification(resultPendingIntent, sound, mContext, isInteresting);
    }

    public void fetchForNotification(final MultiTaskCompleted fetchedElementListener) {
        forceFetching = false;
        obsolete = false;

        fetchingResults elementsListener = new fetchingResults() {
            @Override
            public void onReceive(Connection.Response response) {
                fetchedElements = processResponse(response);
                fetchedElementListener.OneTaskCompleted();
            }

            @Override
            public void onError(Exception e) {
                myLog.error(e);
            }

            @Override
            public void OnEmptyAnswer() {
                fetchedElements = new ArrayList();
//                processResponse("");
                myLog.add("Tenesmos un feching fallido (emtpy answer) en " + getName(), "WARN");
                fetchedElementListener.OneTaskCompleted();
            }
        };

        (new fetchNotificationWeacon(getFetchingFinalUrl(), elementsListener)).execute();

    }

    /**
     * Put a new message for notification, without data
     *
     * @param b
     */
    protected void setObsolete(boolean b) {
        obsolete = b;
    }

    // OTHER
   /*
    TODO clean from here
     */


    public int getRepeatedOffRemoveFromNotification() {
        return mHelper.getRepeatedOffRemoveFromNotification();
    }

    public boolean near(ParseGeoPoint point, int kms) {
        return getGPS().distanceInKilometersTo(point) < kms;
    }

    public boolean isInteresting() {
        return this.isInteresting;
    }

    public String getParadaId() {
        return getString("paradaId");
    }

}

