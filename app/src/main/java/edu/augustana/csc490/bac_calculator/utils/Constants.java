package edu.augustana.csc490.bac_calculator.utils;

/**
 * Created by Dan on 4/19/15.
 */
public class Constants {

    public static final String CLIENT_ID = "5BA3BD63DC25DE480F4C713D83F7A879233B660D";
    public static final String REDIRECT_URL = "http://danshultz.com";
    public static final String UNTAPPD_API_URL = "https://api.untappd.com/v4/";
    public static final String UNTAPPD_AUTH_URL = "https://untappd.com/oauth/authenticate/?client_id=" + CLIENT_ID +"&response_type=token&redirect_url=" + REDIRECT_URL;

    // Preferences
    public static final String PREF_FILE = "SealTeamSixBACCalc";
    public static final String PREF_UNTAPPD_TOKEN = "token";
    public static final String PREF_USER_FIRST_NAME = "firstName";
    public static final String PREF_USER_LAST_NAME = "lastName";
    public static final String PREF_USER_USERNAME = "username";
    public static final String PREF_TOTAL_ALCOHOL = "totalAlcohol";
    public static final String PREF_GENDER = "gender";
    public static final String PREF_WEIGHT = "weight";
    public static final String PREF_TOTAL_HOURS = "totalHours";
    public static final String PREF_AVG_ALC_ELIMINATION_RATE = "averageAlcoholEliminationRate";

    // JSON TAGS
    public static final String TAG_RESPONSE = "response";
    public static final String TAG_USER = "user";
    public static final String TAG_USER_NAME = "user_name";
    public static final String TAG_FIRST_NAME = "first_name";
    public static final String TAG_LAST_NAME = "last_name";
    public static final String TAG_BEERS = "beers";
    public static final String TAG_BEER = "beer";
    public static final String TAG_ITEMS = "items";
    public static final String TAG_BEER_NAME = "beer_name";
    public static final String TAG_BEER_LABEL = "beer_label";
    public static final String TAG_BEER_ABV = "beer_abv";
    public static final String TAG_BREWERY = "brewery";
    public static final String TAG_BREWERY_NAME = "brewery_name";

    // HTTP Headers
    public static final String HEADER_RATE_LIMIT = "X-Ratelimit-Limit";
    public static final String HEADER_RATE_LIMIT_REMAINING = "X-Ratelimit-Remaining";

    // BAC Constants
    public static final double WIDMARKS_CONSTANT = 0.8;
    public static final double OUNCES_IN_POUNDS = 16.0;
    public static final double GRAMS_IN_KILOGRAMS = 1000.0;
    // Body Water Constants
    public static final double MALE_CONSTANT = 0.68;     // (L/Kg)
    public static final double FEMALE_CONSTANT = 0.55;   // (L/Kg)

    /**
     * Endpoints
     * Format UNTAPPD_API_URL + Endpoint
      */
    //https://api.untappd.com/v4/user/info/USERNAME
    public static final String ENDPOINT_USER_INFO = "user/info/";
    //https://api.untappd.com/v4/search/beer/
    public static final String ENDPOINT_BEER_SEARCH = "search/beer";

}
