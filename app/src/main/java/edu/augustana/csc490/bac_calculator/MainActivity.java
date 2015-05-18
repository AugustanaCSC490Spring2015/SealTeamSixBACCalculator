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
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.augustana.csc490.bac_calculator.utils.CalculatorManager;
import edu.augustana.csc490.bac_calculator.utils.Constants;
import edu.augustana.csc490.bac_calculator.utils.DrinkListArrayAdapter;

/**
 * MainActivity acts like a "dashboard" for the app. It displays the users current BAC, future BAC,
 * time they will be sober, a graph of alcohol intake, list of previous drinks as well as a way to
 * add new drinks
 */
public class MainActivity extends ActionBarActivity {

    Button addDrinkButton, finishDrinkButton;
    TextView currentBAC, futureBAC, soberIn;
    ListView drinkListView;
    DrinkListArrayAdapter drinkAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load preferences
        sharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);

        // Calculator Manager Preferences
        CalculatorManager.savedPreferences = getSharedPreferences("BAC_CALCULATOR", MODE_PRIVATE);
        CalculatorManager.loadBACPreferences();

        // show disclaimer
        showDisclaimer();

        // get references to layout components
        currentBAC = (TextView) findViewById(R.id.current_BAC_value);
        futureBAC = (TextView) findViewById(R.id.future_BAC_value);
        soberIn = (TextView) findViewById(R.id.sober_in_value);
        drinkListView = (ListView) findViewById(R.id.drinkListView);
        finishDrinkButton = (Button) findViewById(R.id.finishDrinkButton);
        addDrinkButton = (Button) findViewById(R.id.addDrinkButton);

        // Add Drink Listener
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

        finishDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CalculatorManager.isDrinkUnfinished()) {
                    Calendar tempCal = Calendar.getInstance();
                    if (tempCal.before(CalculatorManager.getDrink(CalculatorManager.getDrinkLogSize()-1).getDrinkStartedCalendar())) {
                        Toast.makeText(MainActivity.this, "You cannot finish a drink before the set start time", Toast.LENGTH_SHORT).show();
                    } else {
                        // add dialog to confirm that they finished the drink
                        DialogFragment finishedDrinkDialog = new DialogFragment() {
                            // create an AlertDialog and return it
                            @Override
                            public Dialog onCreateDialog(Bundle bundle) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setCancelable(false);

                                builder.setMessage("Did you finish your drink?");

                                // "Reset Quiz" Button
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        CalculatorManager.finishDrink();
                                        Toast.makeText(MainActivity.this, "Drink Finished", Toast.LENGTH_SHORT).show();
                                    }
                                } // end anonymous inner class
                                ); // end call to setPositiveButton
                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
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
                } else {
                    Toast.makeText(MainActivity.this, "No Unfinished Drinks", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display drink log list
        drinkAdapter = new DrinkListArrayAdapter(this, R.layout.dashboard_list_item, CalculatorManager.getDrinkLog());
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

        // Timer to update BAC in header bar
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
                        // convert the sober time Double to String to parse out Hours and Minutes
                        formatter = new DecimalFormat("#0.00");
                        String soberDoubleString = formatter.format(CalculatorManager.getFutureSoberTime());
                        soberIn.setText(soberDoubleString.substring(0, (soberDoubleString.length() - 3)) + ":" + (Integer.valueOf(soberDoubleString.substring(soberDoubleString.length() - 2, soberDoubleString.length())) * 60) / 100);
                        //graph view
                        GraphView graph = (GraphView) findViewById(R.id.graph);
                        graph.removeAllSeries(); // clear last graph
                        LineGraphSeries<DataPoint> series;
                        DataPoint[] dataPoints = new DataPoint[CalculatorManager.getDrinkLogSize() + 2];

                        // If there are drinks in the log
                        if (CalculatorManager.getDrinkLogSize() > 0) {
                            // Get first data point
                            dataPoints[0] = new DataPoint(
                                    CalculatorManager.getDrink(0).getDrinkStartedCalendar().getTime(),
                                    0
                            );

                            // Make data points for all other drink start times
                            for (int i=1; i < CalculatorManager.getDrinkLogSize(); i++) {
                                dataPoints[i] = new DataPoint(
                                        CalculatorManager.getDrink(i).getDrinkStartedCalendar().getTime(),
                                        CalculatorManager.calculateASimpleFutureBAC(0, i)
                                );
                            }

                            // Add Max BAC Data Point
                            int lastDataPointIndex = CalculatorManager.getDrinkLogSize();
                            dataPoints[lastDataPointIndex] = new DataPoint(
                                    CalculatorManager.getDrink(lastDataPointIndex-1).getDrinkEndedCalendar().getTime(),
                                    CalculatorManager.calculateASimpleFutureBAC(0, lastDataPointIndex)
                            );

                            // Add Sober Data point
                            lastDataPointIndex++;
                            Calendar calendar = Calendar.getInstance();
                            final long futureSoberLong = calendar.getTimeInMillis() + (long) (CalculatorManager.getFutureSoberTime() * 3600000.0);
                            Calendar soberCalendarTime = Calendar.getInstance();
                            soberCalendarTime.setTimeInMillis(futureSoberLong);
                            dataPoints[lastDataPointIndex] = new DataPoint(
                                    soberCalendarTime.getTime(),
                                    0
                            );

                            // Add data to graph
                            series = new LineGraphSeries<>(dataPoints);
                            series.setColor(getResources().getColor(R.color.graph_line_color));
                            graph.addSeries(series);

                            // set date x-axis label formatter
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext(), dateFormat));
                            graph.getGridLabelRenderer().setNumHorizontalLabels(3);

                            // set y-axis formats
                            graph.getGridLabelRenderer().setNumVerticalLabels(5);

                            // graph viewport settings
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 1);
                            Date d1 = cal.getTime();
                            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 2);
                            Date d2 = cal.getTime();

                            // x-axis viewport
                            graph.getViewport().setXAxisBoundsManual(true);
                            graph.getViewport().setMinX(d1.getTime());
                            graph.getViewport().setMaxX(d2.getTime());

                            // Compute y-axis units
                            double largestBAC = 0;
                            for (int i = 0; i < dataPoints.length; i++) {
                                if (dataPoints[i].getY() > largestBAC) {
                                    largestBAC = dataPoints[i].getY();
                                }
                            }
                            largestBAC += 0.01;
                            BigDecimal bd = new BigDecimal(largestBAC);
                            BigDecimal roundedDouble = bd.setScale(2, BigDecimal.ROUND_UP);

                            // y-axis viewport
                            graph.getViewport().setYAxisBoundsManual(true);
                            graph.getViewport().setMinY(0.0);
                            graph.getViewport().setMaxY(roundedDouble.doubleValue());

                            // enable/disable other settings
                            graph.getViewport().setScalable(false);
                            graph.getViewport().setScrollable(true);
                        }
                    }
                });
            }
        };

        Timer timer = new Timer("Update BAC");
        timer.scheduleAtFixedRate(updateBAC, 30, 5000);
    }

    /**
     * Show disclaimer on app's first run
     */
    private void showDisclaimer() {
        Boolean isFirstRun = sharedPreferences.getBoolean(Constants.PREF_DISCLAIMER, true);

        // Check if app's first run
        if (isFirstRun) {
            //show disclaimer
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.disclaimer_title));
            builder.setMessage(getString(R.string.disclaimer));
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.disclaimer_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // close window
                }
            });
            builder.create().show();

            // store in preferences
            sharedPreferences.edit().putBoolean(Constants.PREF_DISCLAIMER, false).commit();
        }
    }

    /**
     * Update the drink list when resuming this activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        drinkAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Activity menu for accessing settings, Untappd, etc..
     *
     * @param item The menu item selected
     * @return true to close drop down
     */
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
}
