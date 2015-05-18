package edu.augustana.csc490.bac_calculator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
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
 * Created by Dan on 4/20/15.
 */
public class UntappdSearchActivity extends Activity {

    private ListView beersListView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private List<UntappdBeer> beers;
    private SharedPreferences sharedPreferences;
    private UntappdSearchListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_untappd_search);

        sharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);

        beersListView = (ListView) findViewById(R.id.untappdSearchListView);
        searchButton = (ImageButton) findViewById(R.id.searchUntappdButton);
        searchEditText = (EditText) findViewById(R.id.searchEditText);

        beers = new ArrayList<UntappdBeer>();

        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // create and set the custom ArrayAdapter
        adapter = new UntappdSearchListAdapter(this, R.layout.item_untappd_search_list, beers);
        beersListView.setAdapter(adapter);
        beersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UntappdBeer selectedBeer = beers.get(i);
                AddDrinkDialog addDrinkDialog = new AddDrinkDialog(UntappdSearchActivity.this, selectedBeer.getBeerName(), selectedBeer.getBeerABV());
                final int numDrinks = CalculatorManager.drinkLog.size();
                addDrinkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // If a new drink was added, return to dashboard
                        if (CalculatorManager.drinkLog.size()>numDrinks) {
                            finish();
                        }
                    }
                });
                addDrinkDialog.show();
            }
        });

        // Search Button Click Listener
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchEditText.getText().toString();
                if (query != null && !query.equals("")) {
                    new GetBeerSearch().execute();
                    // hide soft keyboard
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        // Enter key listener
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
        File cacheDir = StorageUtils.getCacheDirectory(this);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .diskCacheSize(25 *1024 * 1024) // 25MB in bytes
                .diskCache(new LimitedAgeDiscCache(cacheDir, 60 * 60 * 24)) // per Untappd API term #13
                .build();
        ImageLoader.getInstance().init(config);
    }

    // Note: AsyncTask must be subclassed to be used.
    private class GetBeerSearch extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog progressDialog;
        private String token;
        private String query;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            query = searchEditText.getText().toString();
            progressDialog = new ProgressDialog(UntappdSearchActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.searching_beers));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            token = sharedPreferences.getString(Constants.PREF_UNTAPPD_TOKEN, "");
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetJSON jsonParser = new GetJSON(UntappdSearchActivity.this);
            JSONObject jsonObject = jsonParser.getBeerSearch(token, query);
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            progressDialog.dismiss();
            if (jsonObject != null) {
                try {
                    JSONObject beersJSONObject = jsonObject.getJSONObject(Constants.TAG_RESPONSE).getJSONObject(Constants.TAG_BEERS);
                    JSONArray itemsJSONArray = beersJSONObject.getJSONArray(Constants.TAG_ITEMS);

                    // clear old search
                    beers.clear();

                    for (int i = 0; i < itemsJSONArray.length(); i++) {
                        JSONObject itemJSONObject = itemsJSONArray.getJSONObject(i);
                        JSONObject beerJSONObject = itemJSONObject.getJSONObject(Constants.TAG_BEER);

                        String beerName = beerJSONObject.getString(Constants.TAG_BEER_NAME);
                        String beerABV = beerJSONObject.getString(Constants.TAG_BEER_ABV);
                        String beerLabel = beerJSONObject.getString(Constants.TAG_BEER_LABEL);
                        String brewery = itemJSONObject.getJSONObject(Constants.TAG_BREWERY).getString(Constants.TAG_BREWERY_NAME);
                        UntappdBeer untappdBeer = new UntappdBeer(beerName, brewery, beerLabel, beerABV);

                        beers.add(untappdBeer);
                    }
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT);
            }
        }
    }

}
