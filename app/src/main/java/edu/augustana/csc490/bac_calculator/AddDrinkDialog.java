package edu.augustana.csc490.bac_calculator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.augustana.csc490.bac_calculator.utils.CalculatorManager;

/**
 * Created by Dan on 4/24/15.
 */
public class AddDrinkDialog extends Dialog implements View.OnClickListener {

    private EditText drinkNameEditText;
    private EditText drinkVolumeEditText;
    private EditText drinkAlcoholContentEditText;
    private Button drinkStartedTimeButton;
    private Button drinkStartedDateButton;
    private CheckBox drinkFinishedCheckBox;
    private Button drinkFinishedTimeButton;
    private Button drinkFinishedDateButton;
    private Button addDrinkButton;
    private String drinkName;
    private String drinkABV;
    private TextView drinkFinishedLabel;
    private Calendar drinkStartedCalendar;
    private Calendar drinkFinishedCalendar;
    SimpleDateFormat dateFormat;
    SimpleDateFormat timeFormat;

    /**
     * This constructor is used when creating a drink manually and it passes in null values for the
     * name and ABV
     * @param context
     */
    public AddDrinkDialog(Context context) {
        super(context);

        drinkName = null;
        drinkABV = null;
    }

    /**
     * This constructor is used when loading a drink from Untappd. It automatically fills in the
     * name and ABV passed in into the EditTexts
     * @param context
     * @param beerName
     * @param beerABV
     */
    public AddDrinkDialog(Context context, String beerName, String beerABV) {
        super(context);

        drinkName = beerName;
        drinkABV = beerABV;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_drink_dialog);

        // Get references to UI elements
        drinkNameEditText = (EditText) findViewById(R.id.drinkNameEditText);
        drinkVolumeEditText = (EditText) findViewById(R.id.drinkVolumeEditText);
        drinkAlcoholContentEditText = (EditText) findViewById(R.id.drinkAlcoholContentEditText);
        drinkStartedTimeButton = (Button) findViewById(R.id.drinkStartedTimeButton);
        drinkStartedDateButton = (Button) findViewById(R.id.drinkStartedDateButton);
        drinkFinishedCheckBox = (CheckBox) findViewById(R.id.drinkFinishedCheckBox);
        drinkFinishedTimeButton = (Button) findViewById(R.id.drinkFinishedTimeButton);
        drinkFinishedDateButton = (Button) findViewById(R.id.drinkFinishedDateButton);
        addDrinkButton = (Button) findViewById(R.id.addDrinkButton);
        drinkFinishedLabel = (TextView) findViewById(R.id.drinkFinishedLabel);

        // add button listeners
        addDrinkButton.setOnClickListener(this);
        drinkStartedTimeButton.setOnClickListener(this);
        drinkStartedDateButton.setOnClickListener(this);
        drinkFinishedTimeButton.setOnClickListener(this);
        drinkFinishedDateButton.setOnClickListener(this);

        // add checkbox listener
        drinkFinishedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drinkFinishedCheckBox.isChecked()){
                    setDrinkFinishedButtonsEnabled(false);
                } else {
                    setDrinkFinishedButtonsEnabled(true);
                }
            }
        });

        // Set settings from Untappd
        if (drinkName != null) {
            drinkNameEditText.setText(drinkName);
        }
        if (drinkABV != null) {
            drinkAlcoholContentEditText.setText(drinkABV);
        }

        // Set the drink started and finished times to current time
        drinkStartedCalendar = Calendar.getInstance();
        drinkFinishedCalendar = Calendar.getInstance();

        // Set date and time formats
        dateFormat = new SimpleDateFormat("M/d/yy"); //ex: 5/17/15
        timeFormat = new SimpleDateFormat("h:mm a"); // ex: 4:13 PM

        //set date and time as button labels
        drinkStartedDateButton.setText(dateFormat.format(drinkStartedCalendar.getTime()));
        drinkStartedTimeButton.setText(timeFormat.format(drinkStartedCalendar.getTime()));
        drinkFinishedDateButton.setText(dateFormat.format(drinkFinishedCalendar.getTime()));
        drinkFinishedTimeButton.setText(timeFormat.format(drinkFinishedCalendar.getTime()));

    }

    private DatePickerDialog.OnDateSetListener drinkStartedDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            drinkStartedCalendar.set(year, monthOfYear, dayOfMonth);
            // set button text as new time
            drinkStartedDateButton.setText(dateFormat.format(drinkStartedCalendar.getTime()));
        }
    };

    private TimePickerDialog.OnTimeSetListener drinkStartedTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            drinkStartedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            drinkStartedCalendar.set(Calendar.MINUTE, minute);
            drinkStartedTimeButton.setText(timeFormat.format(drinkStartedCalendar.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener drinkFinishedDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            drinkFinishedCalendar.set(year, monthOfYear, dayOfMonth);
            drinkFinishedDateButton.setText(dateFormat.format(drinkFinishedCalendar.getTime()));
        }
    };

    private TimePickerDialog.OnTimeSetListener drinkFinishedTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            drinkFinishedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            drinkFinishedCalendar.set(Calendar.MINUTE, minute);
            drinkFinishedTimeButton.setText(timeFormat.format(drinkFinishedCalendar.getTime()));
        }
    };

    private void setDrinkFinishedButtonsEnabled(Boolean isEnabled) {
        drinkFinishedTimeButton.setEnabled(isEnabled);
        drinkFinishedDateButton.setEnabled(isEnabled);
        drinkFinishedLabel.setEnabled(isEnabled);

    }

    /**
     * Checks if the user has entered a valid drink name, volume, ABV, and that the end time is
     * after the start time and alerts the user what to change if they entered something wrong
     * @return True if all fields are valid, false otherwise
     */
    private boolean isValidDrink() {
        if (drinkNameEditText.getText().toString() == null || drinkNameEditText.getText().toString().equals("")) {
            // invalid name
            Toast.makeText(getContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return false;
        } else if(drinkVolumeEditText.getText().toString() == null || drinkVolumeEditText.getText().toString().equals("") || Double.parseDouble(drinkVolumeEditText.getText().toString()) <= 0.0) {
            // invalid drink volume
            Toast.makeText(getContext(), "Please enter a valid volume", Toast.LENGTH_SHORT).show();
            return false;
        } else if(drinkAlcoholContentEditText.getText().toString() == null || drinkAlcoholContentEditText.getText().toString().equals("") || Double.parseDouble(drinkAlcoholContentEditText.getText().toString()) <= 0) {
            // invalid drink ABV
            Toast.makeText(getContext(), "Please enter a valid ABV", Toast.LENGTH_SHORT).show();
            return false;
        } else if(!drinkFinishedCheckBox.isChecked() && drinkStartedCalendar.after(drinkFinishedCalendar)){
            // drink times in wrong order
            Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            //valid drink
            Toast.makeText(getContext(), "Drink Added", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addDrinkButton:
                // Check if drink is valid and add to drink list
                if (isValidDrink()) {
                    // if no end time is entered, assume 20 minutes and it will be corrected when
                    // the user clicks the finish drink button
                    if (drinkFinishedCheckBox.isChecked()) {
                        drinkFinishedCalendar = drinkStartedCalendar;
                        drinkFinishedCalendar.set(Calendar.MINUTE, drinkFinishedCalendar.get(Calendar.MINUTE) + 20);
                    }

                    Drink drink = new Drink (
                            drinkNameEditText.getText().toString(), //drink name
                            drinkAlcoholContentEditText.getText().toString(), //drink ABV
                            drinkVolumeEditText.getText().toString(), //drink volume
                            drinkStartedCalendar, //drink started
                            drinkFinishedCalendar //drink ended
                    );
                    CalculatorManager.drinkLog.add(drink); // add to drink log
                    dismiss(); // close dialog window
                }
                break;
            case R.id.drinkStartedTimeButton:
                new TimePickerDialog(
                        getContext(),
                        drinkStartedTimeListener,
                        drinkStartedCalendar.get(Calendar.HOUR_OF_DAY),
                        drinkStartedCalendar.get(Calendar.MINUTE),
                        false // do not use 24 hr view
                ).show();
                break;
            case R.id.drinkStartedDateButton:
                new DatePickerDialog(
                        getContext(),
                        drinkStartedDateListener,
                        drinkStartedCalendar.get(Calendar.YEAR),
                        drinkStartedCalendar.get(Calendar.MONTH),
                        drinkStartedCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;
            case R.id.drinkFinishedTimeButton:
                new TimePickerDialog(
                        getContext(),
                        drinkFinishedTimeListener,
                        drinkFinishedCalendar.get(Calendar.HOUR_OF_DAY),
                        drinkFinishedCalendar.get(Calendar.MINUTE),
                        false // do not use 24hr view
                ).show();
                break;
            case R.id.drinkFinishedDateButton:
                new DatePickerDialog(
                        getContext(),
                        drinkFinishedDateListener,
                        drinkFinishedCalendar.get(Calendar.YEAR),
                        drinkFinishedCalendar.get(Calendar.MONTH),
                        drinkFinishedCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;
            default:
                break;
        }
    }
}
