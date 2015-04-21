package edu.augustana.csc490.bac_calculator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import edu.augustana.csc490.bac_calculator.utils.Constants;


public class MainActivity extends ActionBarActivity {

    Button addUntappdDrinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addUntappdDrinkButton = (Button) findViewById(R.id.addDrinkButton);

        // For Testing - Disable button if user is not signed in
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);
        if (!sharedPreferences.contains(Constants.PREF_UNTAPPD_TOKEN) || sharedPreferences.getString(Constants.PREF_UNTAPPD_TOKEN, null) == null) {
            addUntappdDrinkButton.setEnabled(false);
        }
        // Testing for Untappd Search
        addUntappdDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UntappdSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle clicks on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                // do something
                return true;
            case R.id.untappd_settings:
                Intent intent = new Intent(this, UntappdSettingsActivity.class);
                startActivity(intent);
                return true; // return true to close menu
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
