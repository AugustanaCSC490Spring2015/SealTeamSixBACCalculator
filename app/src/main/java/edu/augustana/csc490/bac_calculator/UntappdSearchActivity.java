package edu.augustana.csc490.bac_calculator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.augustana.csc490.bac_calculator.utils.CalculatorManager;
import edu.augustana.csc490.bac_calculator.utils.Constants;
import edu.augustana.csc490.bac_calculator.utils.GetJSON;
import edu.augustana.csc490.bac_calculator.utils.UntappdSearchListAdapter;

/**
 * The UntappdSearchActivity is a child Activity of MainActivity. It has search bar for the user to
 * enter the beer they are looking for, then displays the search results in a custom ArrayAdapter
 */
public class UntappdSearchActivity extends Activity {

    private ListView beersListView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private List<UntappdBeer> beers;
    private SharedPreferences sharedPreferences;
    private UntappdSearchListAdapter adapter;

    /**
     * onCreate sets up the search bar, listeners, image cache, and beer list.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_untappd_search);

        // get Shared Preferences
        sharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);

        // Get InputMethodManager that is used to hide/show keyboard after searches
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Get references to layout components
        beersListView = (ListView) findViewById(R.id.untappdSearchListView);
        searchButton = (ImageButton) findViewById(R.id.searchUntappdButton);
        searchEditText = (EditText) findViewById(R.id.searchEditText);

        // Initialize the list that will hold search results
        beers = new ArrayList<UntappdBeer>();

        // create and set the custom ArrayAdapter
        adapter = new UntappdSearchListAdapter(this, R.layout.item_untappd_search_list, beers);
        beersListView.setAdapter(adapter);
        beersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // Listener for when the user clicks on a beer in the list
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the UntappedBeer object of the selected beer
                UntappdBeer selectedBeer = beers.get(i);

                // Create an add drink dialog with the beers info
                AddDrinkDialog addDrinkDialog = new AddDrinkDialog(UntappdSearchActivity.this, selectedBeer.getBeerName(), selectedBeer.getBeerABV());

                // The number of drinks before the user adds the selected drink.
                // Used to determine if a drink was added and should return to dashboard
                final int numDrinks = CalculatorManager.getDrinkLogSize();

                // Listener for when the add drink dialog closes
                addDrinkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // If a new drink was added, return to dashboard
                        if (CalculatorManager.getDrinkLogSize() > numDrinks) {
                            finish();
                        }
                    }
                });
                addDrinkDialog.show(); // show the add drink dialog
            }
        });

        // Search Button Click Listener
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get query input
                String query = searchEditText.getText().toString();

                // If valid query, run search
                if (query != null && !query.equals("")) {
                    new GetBeerSearch().execute();
                    // hide soft keyboard
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        // Enter key listener. Allow user to run search with Enter key
        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            new GetBeerSearch().execute();
                            // hide soft keyboard
                            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            return true;
                        default:
                            break;
                    }
                }

                return false;
            }
        });

        // Universal Image Loader Setup
        File cacheDir = StorageUtils.getCacheDirectory(this); // set directory for file cache
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true) // Memory cache
                .cacheOnDisk(true) // Disk Cache
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .diskCacheSize(25 * 1024 * 1024) // 25MB in bytes
                .diskCache(new LimitedAgeDiscCache(cacheDir, 60 * 60 * 24)) // only cache images for 24 hrs per Untappd API term #13
                .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * GetBeerSearch runs the API call on a new thread in the background as networking should not
     * (and can not) be run on the UI thread
     */
    private class GetBeerSearch extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog progressDialog;
        private String token;
        private String query;

        // Before the API call
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // get the search query and access token
            query = searchEditText.getText().toString();
            token = sharedPreferences.getString(Constants.PREF_UNTAPPD_TOKEN, "");

            // display a progress dialog durring search
            progressDialog = new ProgressDialog(UntappdSearchActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.searching_beers));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        // run in the background of the UI thread
        @Override
        protected JSONObject doInBackground(String... args) {
            // Make the call using the GetJSON class
            GetJSON jsonParser = new GetJSON(UntappdSearchActivity.this);
            JSONObject jsonObject = jsonParser.getBeerSearch(token, query);

            return jsonObject;
        }

        // Run after the API call is made
        // Refer to Untappd API to see returned JSON structure
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            // dismiss the progress dialog
            progressDialog.dismiss();

            // Check if something was returned
            if (jsonObject != null) {
                // Try to parse the JSON
                try {
                    // Contains the number of beers returned
                    JSONObject beersJSONObject = jsonObject.getJSONObject(Constants.TAG_RESPONSE).getJSONObject(Constants.TAG_BEERS);
                    // Contains the beer objects returned
                    JSONArray itemsJSONArray = beersJSONObject.getJSONArray(Constants.TAG_ITEMS);

                    // clear old search
                    beers.clear();

                    // Loop through the search results to make UntappdBeer objects from data
                    // See Untappd API documentation for JSON structure
                    for (int i = 0; i < itemsJSONArray.length(); i++) {
                        // Get the current beer
                        JSONObject itemJSONObject = itemsJSONArray.getJSONObject(i);
                        JSONObject beerJSONObject = itemJSONObject.getJSONObject(Constants.TAG_BEER);

                        // create new UntappdBeer
                        String beerName = beerJSONObject.getString(Constants.TAG_BEER_NAME);
                        String beerABV = beerJSONObject.getString(Constants.TAG_BEER_ABV);
                        String beerLabel = beerJSONObject.getString(Constants.TAG_BEER_LABEL);
                        String brewery = itemJSONObject.getJSONObject(Constants.TAG_BREWERY).getString(Constants.TAG_BREWERY_NAME);
                        UntappdBeer untappdBeer = new UntappdBeer(beerName, brewery, beerLabel, beerABV);

                        beers.add(untappdBeer);
                    }
                    // Update the list to show the beers
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // No JSON returned
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT);
            }
        }
    }
}
