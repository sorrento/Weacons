package util;

import android.os.Build;

import com.parse.ParseGeoPoint;
import com.stupidpeople.weacons.Location.GPSCoordinates;

/**
 * Created by Milenko on 20/07/2015.
 */
public class parameters {
    //TODO remove useless parameters
    final public static int defaultThreshold = -100; //For weacon detection

    public static final String pinWeacons = "Weacons";
    public static final String pinFavorites = "Favoritos";
    public static final String pinParseLog = "Logs";
    public static final String pinLastTimeSeen = "spotsHits";
    public static final String pinWigle = "Wigle";

    public static final String COMPANY = "Company";
    public static final String RETAIL = "Retail";
    public static final String RESTAURANT = "Restaurant";
    public static final String FOOD_MENU = "FoodMenu";
    public static final String DAY_MENU = "DayMenu";
    public static final String CHEF = "Chef";
    public static final String SCHEDULE = "Schedule";
    public static final String JOB = "Job";
    public static final String AIRPORT = "Airport";
    public static final String LINKEDIN = "Linkedin";
    public static final String TWITTER = "Twitter";
    public static final String TRIP_ADVISOR = "TripAdvisor";
    public static final String PRODUCTS = "Products";
    public static final String COUPON = "Coupon";
    public static final String MAP = "Map";
    public static final String CHAT = "Chat";

    public static final int repetitionsTurnOffFetching = 3;
    public static final String updateInfo = "newWeaconData";

    //SAPO
    final static int LogFileSize = 100; //in kilobytes, after that, new is started
    public static Integer hitRepetitions = 20; //number of HitSapo in SAPO for considering a SSID important
    public static int minTimeForUpdates = 60 * 1; //in mins

    //WIFI
    public static int nHitsForLogIn = 3; //recommend 3. Number of hits for declaring the login in a spot
    public static long timeBetweenFlightQueries = (long) (2 * 60 * 1000);//in milliseconds, to verify if there are changes in gates, ets
    public static double radioSpotsQuery = 50; //Determines how many spots to load, (kilometers around user's position
    public static int spaceBetweenCards = 30; //in pixels

    //SAPO
//    public static boolean isSapoActive =;


    //Default values to repetition of weacon detection
    public static int repeatedOffRemoveFromNotification = 5;
    public static int repeatedOffToDisappear = 10;
    public static int repeatedOffToChatOff = 4;
    public static int repeatedOnToChatOn = 3;

    //Coords
    public static ParseGeoPoint stCugat = new ParseGeoPoint(41.474722, 2.086667);
    public static ParseGeoPoint santiago = new ParseGeoPoint(-33.45, -70.666667);
    public static ParseGeoPoint madrid = new ParseGeoPoint(40.418889, -3.691944);
    public static ParseGeoPoint barcelona = new ParseGeoPoint(41.3825, 2.176944);

    //Intents
    public static String refreshIntent = "REFRESH";
    public static String silenceIntentName = "SILENCE";
    public static String deleteIntentName = "DELETE_NOTIF";


    //////    TESTS

    public static boolean testWeacons = false;
    public static String[] weaconsTest = {"YouCAbDETK"};
    // "YouCAbDETK" ESADE MBA
    // "hAxGuSgJlJ" ALCALA-ALCALDE LOPEZ CASERO
    // "VGXy6yZLbY" Versió
    //  "Da2ICuTwev" Parada de pipe en santiago (PD107)

    public static boolean doFakePosition = false;
    //    static GPSCoordinates canVolpallerasVernet = new GPSCoordinates(41.4793687249, 2.07720251701);
//    public static com.stupidpeople.weacons.GPSCoordinates fakeCoords = new GPSCoordinates(41.47422028, 2.08041);//Pg. Sant Magí
    public static GPSCoordinates fakeCoords = new GPSCoordinates(41.477538087, 2.07244985754);//Av. Graells

    public static boolean ignoreScanning = false;

    public static boolean simulateWifi = false;
    public static String wifiToSimulateMac = "64:16:F0:54:E8:EE";//para graells
    public static boolean useWigle = false;
//    public static String wifiToSimulateMac = "00:19:15:86:d0:fb";//TELETRIX, wigle para sant magi
//    public static String wifiToSimulateMac= "f8:8e:85:16:28:41";//piripiri

    ////////////////////////////


    public static boolean isMilenkosPhone() {
        String model = Build.MODEL;
        String id = Build.ID;
        String serial = Build.SERIAL;

        //myLog.add("Serial = " + serial + " ID= " + id, "aut");
        //Serial = 3111e121 ID = LRX22C
        //return serial.equals("GT-I9505");//Samsung S4

        return serial.equals("3111e121");//my serial
    }

    public enum CardType {COMPANY, SCHEDULE, LINKEDIN, NEWS, FLIGHTS}

    public enum typeOfWeacon {
        accounting, airport, amusement_park, aquarium, art_gallery, atm, bakery,
        bank, bar, beauty_salon, bicycle_store, book_store, bowling_alley, bus_station, cafe, campground,
        car_dealer, car_rental, car_repair, car_wash, casino, cemetery, church, city_hall, clothing_store,
        convenience_store, courthouse, dentist, department_store, doctor, electrician, electronics_store,
        embassy, establishment, finance, fire_station, florist, food, funeral_home, furniture_store,
        gas_station, general_contractor, grocery_or_supermarket, gym, hair_care, hardware_store, health,
        hindu_temple, home_goods_store, hospital, insurance_agency, jewelry_store, laundry, lawyer, library,
        liquor_store, local_government_office, locksmith, lodging, meal_delivery, meal_takeaway, mosque,
        movie_rental, movie_theater, moving_company, museum, night_club, painter, park, parking, pet_store,
        pharmacy, physiotherapist, place_of_worship, plumber, police, post_office, real_estate_agency, restaurant,
        roofing_contractor, rv_park, school, shoe_store, shopping_mall, spa, stadium, storage, store, subway_station,
        synagogue, taxi_stand, train_station, travel_agency, university, veterinary_care, zoo, nothing
    }

}
