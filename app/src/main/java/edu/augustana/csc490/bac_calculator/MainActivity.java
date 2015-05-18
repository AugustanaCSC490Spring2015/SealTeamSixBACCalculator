package edu.augustana.csc490.bac_calculator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            drinkAdapter.notifyDataSetChanged();
                        }
                    });

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

        drinkAdapter = new DrinkListArrayAdapter(this, R.layout.dashboard_list_item, CalculatorManager.getDrinkLog());
        drinkListView.setAdapter(drinkAdapter);
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


                        //graph view
                        // example data for testing
                        /** Graph View
                         * example data for testing
                         * @TODO: integrate real data
                         */

                        Calendar calendar = Calendar.getInstance();
                        GraphView graph = (GraphView) findViewById(R.id.graph);
                        graph.removeAllSeries();
                        LineGraphSeries<DataPoint> series;
                        DataPoint[] dataPoints = new DataPoint[CalculatorManager.getDrinkLogSize() + 3];

                        if (CalculatorManager.getDrinkLogSize()>0) {
                            dataPoints[0] = new DataPoint(CalculatorManager.getDrink(0).getDrinkStartedCalendar().getTime(), 0.00); // first Drink Start time

                            //add drink end times
                            for (int i = 0; i < CalculatorManager.getDrinkLogSize() - 1; i++) { // all drinks execpt the last one
                                dataPoints[i + 1] = new DataPoint(CalculatorManager.getDrink(i + 1).getDrinkStartedCalendar().getTime(), CalculatorManager.caluclateASimpleFutureBAC(0, i + 1));
                                Log.e("BAC", "SIMPLEFUTBAC:" + CalculatorManager.caluclateASimpleFutureBAC(0, i + 1));
                            }
                            Drink latestDrink = CalculatorManager.getDrink(CalculatorManager.getDrinkLogSize() - 1);
                            if (!latestDrink.isDrinkFinished()) {
                                dataPoints[CalculatorManager.getDrinkLogSize()] = new DataPoint(latestDrink.getDrinkStartedCalendar().getTime(), CalculatorManager.caluclateASimpleFutureBAC(0, CalculatorManager.getDrinkLogSize() - 1));
                            } else {
                                dataPoints[CalculatorManager.getDrinkLogSize()] = new DataPoint(calendar.getTime(), CalculatorManager.getCurrentBAC());
                            }

                            long futureBACLong = calendar.getTimeInMillis() + 1200000; // adds 20 minutes to current time
                            Calendar futureBACMaxTime = Calendar.getInstance();
                            futureBACMaxTime.setTimeInMillis(futureBACLong);
                            dataPoints[CalculatorManager.getDrinkLogSize() + 1] = new DataPoint(futureBACMaxTime.getTime(), CalculatorManager.getFutureBAC());

                            // Last entry is future sober time
                            long futureSoberLong = calendar.getTimeInMillis() + (long) (CalculatorManager.getFutureSoberTime() * 3600000.0);
                            Calendar soberCalendarTime = Calendar.getInstance();
                            soberCalendarTime.setTimeInMillis(futureSoberLong);

                            dataPoints[CalculatorManager.getDrinkLogSize() + 2] = new DataPoint(soberCalendarTime.getTime(), 0.00);

                            series = new LineGraphSeries<>(dataPoints);

                            series.setColor(getResources().getColor(R.color.graph_line_color));
                            graph.addSeries(series);

                            // set date x-axis label formatter
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext(), dateFormat));
                            graph.getGridLabelRenderer().setNumHorizontalLabels(3);

                            // graph viewport settings
                            graph.getViewport().setMinX(dataPoints[0].getX());
                            graph.getViewport().setMaxX(dataPoints[dataPoints.length - 1].getX());
                            graph.getViewport().setXAxisBoundsManual(true);
                            graph.getViewport().setMinY(0.0);
                            double largestBAC = 0;
                            for (int i = 0; i < dataPoints.length; i++) {
                                if (dataPoints[i].getY() > largestBAC) {
                                    largestBAC = dataPoints[i].getY();
                                }
                            }
                            largestBAC += 0.01;
                            BigDecimal bd = new BigDecimal(largestBAC);
                            BigDecimal roundedDouble = bd.setScale(2, BigDecimal.ROUND_UP);
                            graph.getViewport().setMaxY(roundedDouble.doubleValue());
                            graph.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph.getViewport().setYAxisBoundsManual(true);
                            graph.getViewport().setScalable(true);
                        }

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
