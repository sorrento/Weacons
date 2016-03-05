package com.stupidpeople.weacons;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
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

    private String[] cards;
    private ArrayList fetchedElements;
    private String fetchingUrl;

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




    /*
    TODO clean from here
     */


    public String getCompanyDataObjectId() {

        ParseObject po = getParseObject("CardCompany");
        if (po == null) {
            return null;
        } else {
            return po.getObjectId();
        }
    }

    public void setCompanyDataObjectId(String value) {
        put("CardCompany", value);
    }

    /**
     * If true, it shows a separated notification if has been fetched
     *
     * @return
     */
    public boolean getOwnNotif() {
        return (getBoolean("OwnNotif") && fetchedElements.size() > 0);
    }

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

    public String getParadaId() {
        return getString("paradaId");
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

    public void setLogo(ParseFile fileLogo) {
        put("Logo", fileLogo);
    }


    public String getImageParseUrl() {
        return getParseFile("Logo").getUrl();
    }

    public ParseGeoPoint getGPS() {
        return getParseGeoPoint("GPS");
    }

    public void setGPS(ParseGeoPoint GPS) {
        put("GPS", GPS);
    }


    public void setType(String type) {
        put("Type", type);
    }

    public void setMainUrl(String mainUrl) {
        put("MainUrl", mainUrl);
    }

    public void setPhone(String phone) {
        put("Phone", phone);
    }

    public void setUrl2(String url2) {
        put("Url2", url2);

    }

    public void setUrl3(String url3) {
        put("Url3", url3);
    }

    public void setDescription(String description) {
        put("Description", description);
    }

    public void setAirportCode(String airportCode) {
        put("AirportCode", airportCode);
    }

    public void setRating(int rating) {
        put("Rating", rating);
    }

    public void setAutomatic(boolean automatic) {
        put("Automatic", automatic);

    }

    public void setOwner(ParseUser owner) {
        put("Owner", owner);

    }

    // OTHER
    public boolean isAirport() {
        return getType().equals("AIRPORT");
    }


    public boolean isBrowser() {
        boolean b = false;
        try {
            String first = getCards()[0];
            b = first.equals("Browser");
        } catch (Exception e) {
            myLog.add("no tinene card definida en parse", "aut");
        }
        return b;
    }


    //Fetching for notification
    public boolean notificationRequiresFetching() {
        if (getType().equals("bus_stop") || getName().equals("ESADECREAPOLIS")) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList getFetchedElements() {
        return fetchedElements;
    }

    public void setFetchingResults(ArrayList elements) {
        this.fetchedElements = elements;
    }

    public void resetFetchingResults() {
        for (Object o : fetchedElements) {
            //TODO solve
//            LineTimeStCgOld lineTime = (LineTimeStCgOld) o;
//            lineTime.setRoundedTime("-");
        }
    }

    public int getRepeatedOffRemoveFromNotification() {
        int res;
        if (getType().equals("bus_stop")) {
            res = 1;
        } else {
            res = parameters.repeatedOffRemoveFromNotification;
        }
        return res;
    }

    public String getFetchingUrl() {
        fetchingUrl = getString("FetchingUrl") + getParadaId();
//        if (getName().startsWith("ESADE F")) {
//            fetchingUrl = "http://intranet.esade.edu/web1/pkg_pantalles.info_layer?ample=500&alt=901&segons=0&edifici=2";
//        } else if (getType().equals("bus_stop")) {
//            fetchingUrl = "http://www.santqbus.santcugat.cat/consultatr.php?idparada=" + getParadaId() + "&idliniasae=-1&codlinea=-1";
//        }
        return fetchingUrl;
    }

    public boolean near(ParseGeoPoint point, int kms) {
        return getGPS().distanceInKilometersTo(point) < kms;
    }


//    public void setFetchingUrl(String fetchingUrl) {
//        this.fetchingUrl = fetchingUrl;
//    }
}
