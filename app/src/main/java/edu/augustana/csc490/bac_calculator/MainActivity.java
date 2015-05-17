package edu.augustana.csc490.bac_calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
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

    TextView currentBAC, futureBAC, soberIn;

    ListView drinkListView;

    DrinkListArrayAdapter drinkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalculatorManager.savedPreferences = getSharedPreferences("BAC_CALCULATOR", MODE_PRIVATE);
        CalculatorManager.loadBACPreferences();

        currentBAC = (TextView) findViewById(R.id.current_BAC_value);
        futureBAC = (TextView) findViewById(R.id.future_BAC_value);
        soberIn = (TextView) findViewById(R.id.sober_in_value);
        drinkListView = (ListView) findViewById(R.id.drinkListView);

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
                                    AddDrinkDialog addDrinkDialog = new AddDrinkDialog(MainActivity.this);
                                    addDrinkDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            drinkAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    addDrinkDialog.show();

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


                // add dialog to confirm that they finished the drink
                DialogFragment finishedDrinkDialog = new DialogFragment() {
                    // create an AlertDialog and return it
                    @Override
                    public Dialog onCreateDialog(Bundle bundle){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(false);

                        builder.setMessage("Did you finish your drink?");

                        // "Reset Quiz" Button
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                CalculatorManager.finishDrink();
                            }
                        } // end anonymous inner class
                        ); // end call to setPositiveButton
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                dismiss();
                            }

                        }
                        );

                        return builder.create(); // return the AlertDialog
                    } // end method onCreateDialog
                }; // end DialogFragment anonymous inner class

                // use FragmentManager to display the DialogFragment
                finishedDrinkDialog.show(getFragmentManager(), "Confirm Finished Drink");

            }
        });

        drinkAdapter = new DrinkListArrayAdapter(this, R.layout.dashboard_list_item, CalculatorManager.drinkLog);
        drinkListView.setAdapter(drinkAdapter);
        drinkAdapter.notifyDataSetChanged();
        drinkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CalculatorManager.removeDrink(position);
                        drinkAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                builder.setMessage("Delete drink?");
                builder.create().show();
            }
        });

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
        series.setColor(getResources().getColor(R.color.graph_line_color));


       /* *//**
         * formattedDates is an ArrayList of the dates formatted to show just the
         * month, day, and time
         *//*
        ArrayList<String> formattedDates = new ArrayList<String>();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd hh:mm a");
        for (int i = 0; i < 6; i++) {
            formattedDates.add(formatter.format(exampleDates.get(i)));
        }

        *//** ArrayList RecentList contains the # most recent entries as strings,
         * including the date/time and the BAC value
         *//*
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
                //CalculatorManager.calculateFutureSoberTime();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // http://stackoverflow.com/questions/12806278/double-decimal-formatting-in-java
                        NumberFormat formatter = new DecimalFormat("#0.0000");
                        currentBAC.setText(formatter.format(CalculatorManager.getCurrentBAC()));
                        futureBAC.setText(formatter.format(CalculatorManager.getFutureBAC()));
                        Log.e("BAC", "SOBER:" + CalculatorManager.getFutureSoberTime());
                        soberIn.setText((int)Math.floor(CalculatorManager.getFutureSoberTime()) + ":" + (int)((CalculatorManager.getFutureSoberTime() % 1) * 100));
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
            case R.id.delete_all_drinks:
                CalculatorManager.deleteAllDrinks();
                drinkAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Creates and shows the about dialog
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing, close dialog
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
