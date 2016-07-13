package com.stupidpeople.weacons.Helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.stupidpeople.weacons.Helpers.WeaconAirport.HelperAiport2;
import com.stupidpeople.weacons.Helpers.WeaconBus.HelperBusStop;
import com.stupidpeople.weacons.Helpers.WeaconRestaurant.HelperRestaurant;
import com.stupidpeople.weacons.Helpers.WeaconSchedule.HelperSchedule;
import com.stupidpeople.weacons.ready.MultiTaskCompleted;
import com.stupidpeople.weacons.ready.ParseActions;

import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import util.RoundImage;
import util.StringUtils;
import util.imageUtils;
import util.myLog;
import util.parameters;

/**
 * Created by Milenko on 30/07/2015.
 */
@ParseClassName("Weacon")
public class WeaconParse extends ParseObject {
    public ArrayList<fetchableElement> fetchedElements = new ArrayList<>();
    public boolean refreshing = false;
    private boolean obsolete = false;
    private HelperBase mHelper;
    private String[] cards;
    private boolean isInteresting;
    private boolean inHome;
    private Date timeFirstApperaringInThisRow;
    private boolean emptyOrError;//in the fetching answer

    public WeaconParse() {
    }

    public static void build(HashMap<WeaconParse, ArrayList<String>> weaconHash, Context ctx) {
        try {
            for (WeaconParse we : weaconHash.keySet()) we.build(ctx);
        } catch (Exception e) {
            myLog.error(e);
        }
    }

    //GETTERS

    public String getDescription() {
        return getString("Description");
    }

    public void setDescription(String x) {
        put("Description", x);
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

        return this.getObjectId().equals(((WeaconParse) o).getObjectId());
    }

    public String getName() {

        String extra = "";
        if (parameters.isMilenkosPhone()) {
            boolean c1 = isInteresting();
            boolean c3 = inHome();
            ArrayList<String> arr = new ArrayList<>();

            if (c1) arr.add("<3");
            //        if (c2) arr.add("N");
            if (c3) arr.add("H");
            if (c1 || c3) extra = "[" + StringUtils.concatenate(arr, " ") + "]";
        }


        String name = getString("Name");

        return extra + name;
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
        switch (type) {
            case "bus_stop":
                sol = parameters.typeOfWeacon.bus_station;
                break;
            case "AIRPORT":
                sol = parameters.typeOfWeacon.airport;
                break;
            case "restaurant":
                sol = parameters.typeOfWeacon.restaurant;
                break;
            case "University":  //es ESADE, cambiar, asumo que todas tienen schedule
                sol = parameters.typeOfWeacon.university;
                break;
        }

        return sol;
    }

    public void setType(String type) {
        put("Type", type);
    }

    public String getTypeString() {
        return mHelper.typeString();
    }

    private Bitmap getLogo() {
        Bitmap bm = null;
        try {
            byte[] bitmapdata = getParseFile("Logo").getData();
            bm = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bm;
    }

    public String getLogoFileName() {
        return getParseFile("Logo").getName();
    }

    ///////////////////////////////////////////////////// DELEGATES

    public Bitmap getLogoRounded() {
        Bitmap bm = getLogo();

        return imageUtils.drawableToBitmap(new RoundImage(bm));
    }

    public ParseGeoPoint getGPS() {
        return getParseGeoPoint("GPS");
    }

    public void setGPS(ParseGeoPoint gps) {
        put("GPS", gps);
    }

    public void build(Context ctx) {
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
                    mHelper = new HelperBusStop(this, ctx);
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
                case university:
                    //TODO change since it applies ony to esade
                    mHelper = new HelperSchedule(this, ctx);
                    break;
                //            case veterinary_care:
                //                break;
                //            case zoo:
                //                break;
                case nothing:
                    mHelper = new HelperDefault(this, ctx);
                    break;
            }

            isInteresting = ParseActions.isInteresting(getObjectId());
            inHome = ParseActions.IsHome(getObjectId());

        } catch (Exception e) {
            myLog.error(e);
        }
    }

    public boolean notificationRequiresFetching() {
        return mHelper.notificationRequiresFetching();
    }

    private ArrayList processResponse(Connection.Response response) {
        if (mHelper.notificationRequiresFetching()) {
            return ((HelperBaseFecthNotif) mHelper).processResponse(response);
        } else {
            myLog.add("Este weacon no tiene fetching" + getName(), "WARN");
            return null;
        }
    }

    private String getFetchingFinalUrl() {
        if (mHelper.notificationRequiresFetching()) {
            return ((HelperBaseFecthNotif) mHelper).getFetchingFinalUrl();
        } else {
            myLog.add("Este weacon no tiene fetching" + getName(), "WARN");
            return null;
        }
    }


    /////////////////////////////////////////////////////

    public String getFetchingPartialUrl() {
        return getString("FetchingUrl");
    }

    public SpannableString inboxSummary() {
        return mHelper.inboxSummaryText();
    }

    /**
     * the activity should be open for this Weacon. can be Cards o Browser
     *
     * @return
     */
    public Class getActivityClass() {
        return mHelper.getActivityClass();
    }

    public NotificationCompat.Builder buildSingleNotification(PendingIntent resultPendingIntent, Context ctx) {
        return mHelper.buildSingleNotification(resultPendingIntent, ctx);
    }

    public void fetchForNotification(final MultiTaskCompleted fetchedElementListener) {

        if (((HelperBaseFecthNotif) this.mHelper).doFetchingAEstaHora()) {

            fetchingResults elementsListener = new fetchingResults() {
                @Override
                public void onReceive(Connection.Response response) {
                    setObsolete(false);
                    refreshing = false;

                    ((HelperBaseFecthNotif) mHelper).lastUpdateTime = new Date();
                    fetchedElements = processResponse(response);
                    emptyOrError = fetchedElements.size() == 0;
                    fetchedElementListener.OneTaskCompleted();
                }

                @Override
                public void onError(Exception e) {
                    emptyOrError = true;
                    setObsolete(false);
                    refreshing = false;
                    myLog.add("ERROR:\n" + e, "FET");
                }

                @Override
                public void OnEmptyAnswer() {
                    emptyOrError = true;
                    setObsolete(false);
                    refreshing = false;

                    fetchedElements = new ArrayList();
//                processResponse("");
                    myLog.add("Tenesmos un feching fallido (emtpy answer) en " + getName(), "WARN");
                    fetchedElementListener.OneTaskCompleted();
                }
            };

            (new fetchNotificationWeacon(getFetchingFinalUrl(), elementsListener)).execute();

        } else {
            // Not fetching at this time
            fetchedElementListener.OneTaskCompleted();
        }
    }

    // OTHER
   /*
    TODO clean from here
     */

    public boolean isObsolete() {
        return obsolete;
    }

    /**
     * Put a new message for notification, without data
     *
     * @param b
     */
    public void setObsolete(boolean b) {
        obsolete = b;
        fetchedElements = new ArrayList();
    }

    public int getRepeatedOffRemoveFromNotification() {
        return mHelper.getRepeatedOffRemoveFromNotification();
    }

    public boolean near(ParseGeoPoint point, int kms) {
        return getGPS().distanceInKilometersTo(point) < kms;
    }

    public boolean isInteresting() {
        return this.isInteresting;
    }

    public void setInteresting(boolean b) {
        this.isInteresting = b;
    }

    public String getParadaId() {
        return getString("paradaId");
    }

    public void setParadaId(String stopCode) {
        put("paradaId", stopCode);
    }

    /**
     * Indicates if this weacon is detectable from a place where the person spends more tha half a
     * an hour
     *
     * @return
     */
    public boolean inHome() {
        return inHome;
    }

    public Date getTimeFirstApperaringInThisRow() {
        return timeFirstApperaringInThisRow;
    }

    public void setTimeFirstApperaringInThisRow(Date timeFirstApperaringInThisRow) {
        this.timeFirstApperaringInThisRow = timeFirstApperaringInThisRow;
    }

    public void setInHome(boolean inHome) {
        myLog.add("Se ha considerado home este weacon:" + getName(), "OJO");
        ParseActions.setHome(this);
        this.inHome = inHome;
    }

    public boolean emptyAnswerOrErrorFetching() {
        return emptyOrError;
    }

    public SpannableString textForListActivity() {
        return mHelper.textForListActivity();
    }

    public String getNameWithCode() {
        return mHelper.getNameWithCode();
    }

    public void setOwner() {
        ParseUser owner = ParseUser.getCurrentUser();
        if (owner != null) put("Owner", owner);
    }

    public void setFetchingUrl(String url) {
        put("FetchingUrl", url);
    }
}

