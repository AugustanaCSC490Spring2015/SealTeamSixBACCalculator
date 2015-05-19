package edu.augustana.csc490.bac_calculator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * GetJSON is a utility class that is used to run JSON queries. It has various methods that
 * queries corresponding to different Untappd API calls. It then runs the queries and returns the
 * JSON returned by the Web API. Note: per Untappd API terms, there is a rate limit of 100 API
 * calls per continuous hour
 */
public class GetJSON {

    private SharedPreferences sharedPreferences;

    /**
     * The Constructor gets the shared preferences from the current context. The shared preferences
     * are used to store how many times the user has called the API in the last hour, which is
     * viewable in the Untappd Settings page
     *
     * @param context The current context.
     */
    public GetJSON(Context context) {
        // load preferences
        sharedPreferences = context.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Build a query to get the given user's account information
     *
     * @param username The user's Untappd username
     * @param token    The access_token for authorized calls
     * @param compact  You can pass "true" here only show the user infomation, and remove the "checkins", "media", "recent_brews", etc attributes
     * @return The JSON object returned from the web API containing the user's info
     */
    public JSONObject getUserInfo(String username, String token, Boolean compact) {
        // Endpoint: /v4/user/info/USERNAME
        StringBuilder url = new StringBuilder(Constants.UNTAPPD_API_URL);
        url.append(Constants.ENDPOINT_USER_INFO);

        // if no username is provided for an authorised call, it will return the authorised users info
        if (username != null) {
            url.append(username);
        }

        // append access token
        url.append("?access_token=").append(token);

        // append compact option
        if (compact) {
            url.append("&true");
        }

        return getJSON(url.toString());
    }

    /**
     * This method calls the overloaded getUserInfo mehtod with a null username to get the info of
     * the authorised user without having to specify their username
     *
     * @param token   The access_token for authorized calls
     * @param compact You can pass "true" here only show the user infomation, and remove the "checkins", "media", "recent_brews", etc attributes
     * @return The JSON object returned from the web API containing the user's info
     */
    public JSONObject getUserInfo(String token, Boolean compact) {
        return getUserInfo(null, token, compact);
    }

    /**
     * Build a query to search the beer database. It will return a maximum of 25 results as default.
     * Pagination is supported by the Untappd API but not implemented in this app
     * Note: The best way to search is always "Brewery Name + Beer Name", such as "Dogfish 60 Minute".
     *
     * @param token The access_token for authorized calls
     * @param query The search term that you want to search.
     * @return The JSON object containing the search results
     */
    public JSONObject getBeerSearch(String token, String query) {
        // Endpoint: /v4/search/beer
        StringBuilder url = new StringBuilder(Constants.UNTAPPD_API_URL);
        url.append(Constants.ENDPOINT_BEER_SEARCH);

        // append access token
        url.append("?access_token=").append(token);

        // append query
        url.append("&q=").append(query);

        return getJSON(url.toString());
    }

    /**
     * This method is used by the public methods to get the JSON from the Web API. It accepts a
     * pre-built url for an API query, opens a connection, reads in the JSON, and updates the rate
     * limit remaining in preferences
     *
     * @param urlString The pre-built API call url
     * @return The JSON returned from the web API
     */
    private JSONObject getJSON(String urlString) {
        InputStream is = null;
        JSONObject jsonObject = null;
        String jsonString = "";

        try {
            // Connect to the API
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            // Store rate limit remainging in prefs
            sharedPreferences.edit().putString(Constants.PREF_RATE_LIMIT_REMAINING, urlConnection.getHeaderField(Constants.HEADER_RATE_LIMIT_REMAINING)).commit();
            Log.i("RateLimit", "Rate Limit Remaining: " + urlConnection.getHeaderField(Constants.HEADER_RATE_LIMIT_REMAINING) + "/" + urlConnection.getHeaderField(Constants.HEADER_RATE_LIMIT));

            // Read the JSON into a String
            is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            // Append the response line-by-line
            while ((line = br.readLine()) != null) {
                sb.append(line + "/n");
            }
            is.close(); // close input stream

            // convert the String to JSONObject
            jsonString = sb.toString();
            jsonObject = new JSONObject(jsonString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
