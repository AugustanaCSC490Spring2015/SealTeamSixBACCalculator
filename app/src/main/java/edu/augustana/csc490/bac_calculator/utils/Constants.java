package edu.augustana.csc490.bac_calculator.utils;

/**
 * Created by Dan on 4/19/15.
 */
public class Constants {

    public static final String CLIENT_ID = "5BA3BD63DC25DE480F4C713D83F7A879233B660D";
    public static final String CLIENT_SECRET = "29923B02B40A4B327F7687495E36DAD8CAC1E989";
    public static final String REDIRECT_URL = "http://danshultz.com";
    public static final String UNTAPPD_API_URL = "https://api.untappd.com/v4/";
    public static final String UNTAPPD_AUTH_URL = "https://untappd.com/oauth/authenticate/?client_id=" + CLIENT_ID +"&response_type=token&redirect_url=" + REDIRECT_URL;

    // Preferences
    public static final String PREF_FILE = "SealTeamSixBACCalc";
    public static final String PREF_UNTAPPD_TOKEN = "token";

    // JSON TAGS
    public static final String TAG_RESPONSE = "response";
    public static final String TAG_USER = "user";
    public static final String TAG_USER_NAME = "user_name";
    public static final String TAG_FIRST_NAME = "first_name";
    public static final String TAG_LAST_NAME = "last_name";
    public static final String TAG_STATS = "stats";
    public static final String TAG_TOTAL_BADGES = "total_badges";
    public static final String TAG_TOTAL_FRIENDS = "total_friends";
    public static final String TAG_TOTAL_CHECKINS = "total_checkins";
    public static final String TAG_TOTAL_BEERS = "total_beers";
    public static final String TAG_BEERS = "beers";
    public static final String TAG_BEER = "beer";
    public static final String TAG_ITEMS = "items";
    public static final String TAG_BEER_NAME = "beer_name";
    public static final String TAG_BEER_LABEL = "beer_label";
    public static final String TAG_BEER_ABV = "beer_abv";
    public static final String TAG_BREWERY = "brewery";
    public static final String TAG_BREWERY_NAME = "brewery_name";



    /**
     * Endpoints
     * Format UNTAPPD_API_URL + Endpoint
      */
    //https://api.untappd.com/v4/beer/info/BID
    public static final String ENDPOINT_BEER_INFO = "beer/info/";
    //https://api.untappd.com/v4/user/info/USERNAME
    public static final String ENDPOINT_USER_INFO = "user/info/";
    //https://api.untappd.com/v4/search/beer/
    public static final String ENDPOINT_BEER_SEARCH = "search/beer";

}
