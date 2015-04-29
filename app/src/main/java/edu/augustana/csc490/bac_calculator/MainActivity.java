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
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.augustana.csc490.bac_calculator.utils.Constants;


public class MainActivity extends ActionBarActivity {

    Button addDrinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        //graph view
        // example data for testing
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        calendar.add(Calendar.MINUTE, 30);
        Date d2 = calendar.getTime();
        calendar.add(Calendar.MINUTE, 30);
        Date d3 = calendar.getTime();
        calendar.add(Calendar.MINUTE, 30);
        Date d4 = calendar.getTime();
        calendar.add(Calendar.MINUTE, 30);
        Date d5 = calendar.getTime();
        calendar.add(Calendar.MINUTE, 30);
        Date d6 = calendar.getTime();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
            new DataPoint(d1, 0.02),
            new DataPoint(d2, 0.04),
            new DataPoint(d3, 0.07),
            new DataPoint(d4, 0.09),
            new DataPoint(d5, 0.10),
            new DataPoint(d6, 0.11)
        });
        graph.addSeries(series);

        // set date label formatter
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, dateFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);

        // graph viewport settings
        graph.getViewport().setMinX(d2.getTime());
        graph.getViewport().setMaxX(d5.getTime());
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(0.0);
        graph.getViewport().setMaxY(0.4);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScalable(true);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
