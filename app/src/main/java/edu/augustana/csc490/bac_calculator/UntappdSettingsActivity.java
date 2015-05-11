package edu.augustana.csc490.bac_calculator;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.augustana.csc490.bac_calculator.utils.Constants;
import edu.augustana.csc490.bac_calculator.utils.GetJSON;

/**
 * Created by Dan on 4/20/15.
 */
public class UntappdSettingsActivity extends ActionBarActivity {

    SharedPreferences sharedPreferences;
    Button authAppButton;
    TextView untappdAuthDirections;
    TextView untappdUserInfo; // Displays info about user logged in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_untappd_settings);

        // Set the up arrow in the ActionBar to the home screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Load shared preferences
        sharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);

        // Get references to UI components
        authAppButton = (Button) findViewById(R.id.authAppButton);
        untappdAuthDirections = (TextView) findViewById(R.id.untappdAuthDirections);
        untappdUserInfo = (TextView) findViewById(R.id.untappdUserInfo);

        // Check if user has already authorised the app
        if (sharedPreferences.contains(Constants.PREF_UNTAPPD_TOKEN)) {
            updateUserInfo();
        }

        authAppButton.setOnClickListener(new View.OnClickListener() {
            Dialog dialog;
            WebView webView;
            @Override
            public void onClick(View view) {
                // Check if the user is logging in out out
                if (sharedPreferences.getString(Constants.PREF_UNTAPPD_TOKEN, null) != null) {
                    // Log out - Delete token,  first name, last name, and username in prefs
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.PREF_UNTAPPD_TOKEN, null);
                    editor.putString(Constants.PREF_USER_FIRST_NAME, null);
                    editor.putString(Constants.PREF_USER_LAST_NAME, null);
                    editor.putString(Constants.PREF_USER_USERNAME, null);
                    editor.commit();

                    authAppButton.setText(R.string.auth_app_btn);
                    untappdUserInfo.setText("");
                } else {
                    // User is logging in
                    dialog = new Dialog(UntappdSettingsActivity.this);
                    dialog.setContentView(R.layout.auth_dialog);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    webView = (WebView) dialog.findViewById(R.id.webView);
                    webView.getSettings().setJavaScriptEnabled(true); // enable javascript in the WebView

                    // Load the page for the user to authenticate the app. Link in form of
                    // https://untappd.com/oauth/authenticate/?client_id=CLIENTID&response_type=token&redirect_url=REDIRECT_URL
                    webView.loadUrl(Constants.UNTAPPD_AUTH_URL);

                    // Set the custom WebViewClient
                    webView.setWebViewClient(new WebViewClient() {
                        boolean haveToken = false;
                        String token;

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);

                            // Check if the url contains the token
                            if (url.contains("token=") && !haveToken) {
                                String removeFromUrl = Constants.REDIRECT_URL + "/#access_token=";
                                token = url.substring(removeFromUrl.length());
                                haveToken = true;

                                // Save token in preferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(Constants.PREF_UNTAPPD_TOKEN, token);
                                editor.commit();

                                // update user info on screen
                                new GetUserInfo().execute();

                                // close dialog
                                dialog.dismiss();
                            } else if (url.toLowerCase().contains(Constants.REDIRECT_URL) && sharedPreferences.getString(Constants.PREF_UNTAPPD_TOKEN, null) == null && !url.toLowerCase().contains("client_id=")) {
                                // Redirected to redirect url without token
                                Log.wtf("Bad Redirect from Untappd: ", url);
                                // Ask user to try again and close the webview dialog
                                dialog.dismiss();
                                Toast.makeText(UntappdSettingsActivity.this, R.string.try_again, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                        }
                    });

                    // Set the title and show the dialog
                    dialog.setTitle(getResources().getString(R.string.auth_untappd_dialog_title));
                    dialog.show();
                }
            }
        });

    } // end method onCreate

    private void updateUserInfo() {
        authAppButton.setText(R.string.sign_out);

        String first_name = sharedPreferences.getString(Constants.PREF_USER_FIRST_NAME, "");
        String last_name = sharedPreferences.getString(Constants.PREF_USER_LAST_NAME, "");
        String user_name = sharedPreferences.getString(Constants.PREF_USER_USERNAME, "");

        String userInfo = "Logged in as " + first_name + " " + last_name + " (" + user_name +") " +
                "\nUntappd API Hourly Call Rate Limit Remaining: " + sharedPreferences.getString(Constants.PREF_RATE_LIMIT_REMAINING,"?") + "/100";

        untappdUserInfo.setText(userInfo);
    }

    // Note: AsyncTask must be subclassed to be used.
    private class GetUserInfo extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog progressDialog;
        private String token;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(UntappdSettingsActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading_user_data));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            token = sharedPreferences.getString(Constants.PREF_UNTAPPD_TOKEN, "");
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetJSON jsonParser = new GetJSON(UntappdSettingsActivity.this);
            JSONObject jsonObject = jsonParser.getUserInfo(token, true);
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            progressDialog.dismiss();
            if (jsonObject != null) {
                try {
                    JSONObject userJSONObject = jsonObject.getJSONObject(Constants.TAG_RESPONSE).getJSONObject(Constants.TAG_USER);

                    // Get user info
                    String first_name = userJSONObject.getString(Constants.TAG_FIRST_NAME);
                    String last_name = userJSONObject.getString(Constants.TAG_LAST_NAME);
                    String user_name = userJSONObject.getString(Constants.TAG_USER_NAME);

                    // Save first name, last name, and username in prefs
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.PREF_USER_FIRST_NAME, first_name);
                    editor.putString(Constants.PREF_USER_LAST_NAME, last_name);
                    editor.putString(Constants.PREF_USER_USERNAME, user_name);
                    editor.commit();

                    updateUserInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT);
            }
        }
    }
}
