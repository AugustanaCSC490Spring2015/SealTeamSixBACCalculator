package edu.augustana.csc490.bac_calculator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.augustana.csc490.bac_calculator.utils.CalculatorManager;
import edu.augustana.csc490.bac_calculator.utils.Constants;
import edu.augustana.csc490.bac_calculator.utils.DrinkListArrayAdapter;


public class MainActivity extends ActionBarActivity {

    Button addDrinkButton, finishDrinkButton;

    TextView currentBAC, futureBAC;

    ListView drinkListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalculatorManager.savedPreferences = getSharedPreferences("BAC_CALCULATOR", MODE_PRIVATE);
        CalculatorManager.loadBACPreferences();
        CalculatorManager.weightInPounds = 220;

        currentBAC = (TextView) findViewById(R.id.currentBACView);
        futureBAC = (TextView) findViewById(R.id.futureBACView);
        drinkListView = (ListView) findViewById(R.id.drinkListView);

        /**@TODO: code finish drink button
         */
        finishDrinkButton = (Button) findViewById(R.id.finishDrinkButton);

        addDrinkButton = (Button) findViewById(R.id.addDrinkButton);

        addDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if user is not logged into Untappd
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);
                if (!sharedPreferences.contains(Constants.PREF_UNTAPPD_TOKEN) || sharedPreferences.getString(Constants.PREF_UNTAPPD_TOKEN, null) == null) {
                    // Show manual add drink dialog
                    new AddDrinkDialog(MainActivity.this).show();
                } else {
                    // the user is logged into Untappd
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.choose_option);
                    builder.setItems(R.array.add_drink_options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: // Manually add drink
                                    // Show manual add drink dialog
                                    new AddDrinkDialog(MainActivity.this).show();
                                    break;
                                case 1: // Search Untappd
                                    Intent intent = new Intent(MainActivity.this, UntappdSearchActivity.class);
                                    startActivity(intent);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
            }
        });

        finishDrinkButton.setOnClickListener(new View.OnClickListener() {  // TODO: Change This; Right now it updates the BAC Calculation
            @Override
            public void onClick(View v) {

               CalculatorManager.finishDrink();
            }
        });

        final DrinkListArrayAdapter adapter = new DrinkListArrayAdapter(this, R.layout.dashboard_list_item, CalculatorManager.drinkLog);
        drinkListView.setAdapter(adapter);

        //graph view
        // example data for testing
        /** Graph View
         * example data for testing
         * @TODO: integrate real data
         */

        ArrayList<Date> exampleDates = new ArrayList<Date>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 6; i++) {
            exampleDates.add(calendar.getTime());
            calendar.add(Calendar.MINUTE, 30);
        }


        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(exampleDates.get(0), 0.02),
                new DataPoint(exampleDates.get(1), 0.04),
                new DataPoint(exampleDates.get(2), 0.07),
                new DataPoint(exampleDates.get(3), 0.09),
                new DataPoint(exampleDates.get(4), 0.10),
                new DataPoint(exampleDates.get(5), 0.11)
        });
        graph.addSeries(series);

        /**
         * formattedDates is an ArrayList of the dates formatted to show just the
         * month, day, and time
         */
        ArrayList<String> formattedDates = new ArrayList<String>();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd hh:mm a");
        for (int i = 0; i < 6; i++) {
            formattedDates.add(formatter.format(exampleDates.get(i)));
        }

        /** ArrayList RecentList contains the # most recent entries as strings,
         * including the date/time and the BAC value
         */
        ArrayList<String> RecentList = new ArrayList<String>();
        RecentList.add(formattedDates.get(0) + "  -  " + "0.11");
        RecentList.add(formattedDates.get(1) + "  -  " + "0.10");
        RecentList.add(formattedDates.get(2) + "  -  " + "0.09");
        RecentList.add(formattedDates.get(3) + "  -  " + "0.07");
        RecentList.add(formattedDates.get(4) + "  -  " + "0.04");
        RecentList.add(formattedDates.get(5) + "  -  " + "0.02");


        /** arrayAdapter adapts RecentList to the dashboard list view lv

        ListView lv = (ListView) findViewById(R.id.drinkListView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.dashboard_list_item,
                RecentList);

        lv.setAdapter(arrayAdapter);
        */
        
        // set date x-axis label formatter
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, dateFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);

        // graph viewport settings
        graph.getViewport().setMinX(exampleDates.get(1).getTime());
        graph.getViewport().setMaxX(exampleDates.get(4).getTime());
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0.0);
        graph.getViewport().setMaxY(0.3);
        graph.getGridLabelRenderer().setNumVerticalLabels(4);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScalable(true);

        TimerTask updateBAC = new TimerTask() {
            @Override
            public void run() {
                CalculatorManager.calculateCurrentAndFutureBAC();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // http://stackoverflow.com/questions/12806278/double-decimal-formatting-in-java
                        NumberFormat formatter = new DecimalFormat("#0.0000");
                        currentBAC.setText(formatter.format(CalculatorManager.getCurrentBAC()));
                        futureBAC.setText(formatter.format(CalculatorManager.getFutureBAC()));
                    }
                });

            }
        };

        Timer timer = new Timer("Update BAC");
        timer.scheduleAtFixedRate(updateBAC, 30, 5000);

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
                Intent userSettingsIntent = new Intent(this, UserSettingsActivity.class);
                startActivity(userSettingsIntent);
                return true; // return true to close menu
            case R.id.untappd_settings:
                Intent untappdSettingsIntent = new Intent(this, UntappdSettingsActivity.class);
                startActivity(untappdSettingsIntent);
                return true; // return true to close menu
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true; // return true to close menu
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
