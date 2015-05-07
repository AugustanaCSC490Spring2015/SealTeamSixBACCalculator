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
 * Created by Dan on 4/19/15.
 */
public class GetJSON {

    SharedPreferences sharedPreferences;

    public GetJSON(Context context){
        // load preferences
        sharedPreferences = context.getSharedPreferences(Constants.PREF_FILE, context.MODE_PRIVATE);
    }

    public JSONObject getUserInfo(String username, String token, Boolean compact) {
        StringBuilder url = new StringBuilder(Constants.UNTAPPD_API_URL);
        url.append(Constants.ENDPOINT_USER_INFO);

        if (username != null) {
            url.append(username);
        }

        url.append("?access_token=").append(token);

        if (compact) {
            url.append("&true");
        }

        return getJSON(url.toString());

    }

    public JSONObject getUserInfo(String token, Boolean compact) {
        return getUserInfo(null, token, compact);
    }

    public JSONObject getUserInfo(String token) {
        return getUserInfo(null, token, false);
    }

    public JSONObject getBeerSearch(String token, String query) {
        StringBuilder url = new StringBuilder(Constants.UNTAPPD_API_URL);
        url.append(Constants.ENDPOINT_BEER_SEARCH);
        url.append("?access_token=").append(token);
        url.append("&q=").append(query);

        return getJSON(url.toString());
    }

    private JSONObject getJSON(String urlString) {
        InputStream is = null;
        JSONObject jsonObject = null;
        String jsonString = "";

        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            // Store rate limit remainging in prefs
            sharedPreferences.edit().putString(Constants.PREF_RATE_LIMIT_REMAINING, urlConnection.getHeaderField(Constants.HEADER_RATE_LIMIT_REMAINING)).commit();
            Log.i("RateLimit", "Rate Limit Remaining: "+ urlConnection.getHeaderField(Constants.HEADER_RATE_LIMIT_REMAINING) + "/" + urlConnection.getHeaderField(Constants.HEADER_RATE_LIMIT));
            is = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "/n");
            }
            is.close();
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
