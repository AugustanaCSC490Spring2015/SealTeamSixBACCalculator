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

import java.util.Calendar;

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
    private int drinkStartedHour;
    private int drinkStartedMinute;
    private int drinkStartedDay;
    private int drinkStartedMonth;
    private int drinkStartedYear;
    private int drinkFinishedHour;
    private int drinkFinishedMinute;
    private int drinkFinishedDay;
    private int drinkFinishedMonth;
    private int drinkFinishedYear;



    public AddDrinkDialog(Context context) {
        super(context);

        drinkName = null;
        drinkABV = null;
    }

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

        // get calendar and current time and date
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // months 0-11
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // hours 0-23
        int currentMinute = calendar.get(Calendar.MINUTE); // 0-59

        // set the inital times the date and time dialogs should be set at
        drinkStartedYear = currentYear;
        drinkStartedMonth = currentMonth;
        drinkStartedDay = currentDay;
        drinkStartedHour = currentHour;
        drinkStartedMinute = currentMinute;
        drinkFinishedYear = currentYear;
        drinkFinishedMonth = currentMonth;
        drinkFinishedDay = currentDay;
        drinkFinishedHour = currentHour;
        drinkFinishedMinute = currentMinute;


        //set date and time defaults
        drinkStartedDateButton.setText(getDateString(currentMonth, currentDay, currentYear));
        drinkStartedTimeButton.setText(getTimeString(currentHour, currentMinute));
        drinkFinishedDateButton.setText(getDateString(currentMonth, currentDay, currentYear));
        drinkFinishedTimeButton.setText(getTimeString(currentHour, currentMinute));

    }

    private String getDateString(int month, int day, int year) {
        drinkStartedMonth = month;
        drinkStartedDay = day;
        drinkStartedYear = year;
        return month + "/" + day + "/" + year;
    }

    private String getTimeString(int hour, int minute) {
        String stringMinute = "" + minute;
        String amPmString = " AM";
        if (hour >= 12) {
            amPmString = " PM";
        }
        if (minute >= 60) {
            minute = minute%60;
            hour++;
        }
        if (hour >= 12) {
            hour = hour%12;
        }

        if (hour == 0) {
            hour = 12;
        }
        if (minute <  10) {
            stringMinute = "0"+minute;
        }

        return hour  + ":" + stringMinute + amPmString;
    }

    private DatePickerDialog.OnDateSetListener drinkStartedDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            drinkStartedYear = year;
            drinkStartedMonth = monthOfYear;
            drinkStartedDay = dayOfMonth;
            drinkStartedDateButton.setText(getDateString(monthOfYear, dayOfMonth, year));
        }
    };

    private TimePickerDialog.OnTimeSetListener drinkStartedTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            drinkStartedHour = hourOfDay;
            drinkStartedMinute = minute;
            drinkStartedTimeButton.setText(getTimeString(hourOfDay, minute));
        }
    };

    private DatePickerDialog.OnDateSetListener drinkFinishedDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            drinkFinishedYear = year;
            drinkFinishedMonth = monthOfYear;
            drinkFinishedDay = dayOfMonth;
            drinkFinishedDateButton.setText(getDateString(monthOfYear, dayOfMonth, year));
        }
    };

    private TimePickerDialog.OnTimeSetListener drinkFinishedTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            drinkFinishedHour = hourOfDay;
            drinkFinishedMinute = minute;
            drinkFinishedTimeButton.setText(getTimeString(hourOfDay, minute));
        }
    };

    private void setDrinkFinishedButtonsEnabled(Boolean isEnabled) {
        drinkFinishedTimeButton.setEnabled(isEnabled);
        drinkFinishedDateButton.setEnabled(isEnabled);
        drinkFinishedLabel.setEnabled(isEnabled);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addDrinkButton:
                // do something
                break;
            case R.id.drinkStartedTimeButton:
                new TimePickerDialog(getContext(), drinkStartedTimeListener, drinkStartedHour, drinkStartedMinute, false).show();
                break;
            case R.id.drinkStartedDateButton:
                new DatePickerDialog(getContext(), drinkStartedDateListener, drinkStartedYear, drinkStartedMonth, drinkStartedDay).show();
                break;
            case R.id.drinkFinishedTimeButton:
                new TimePickerDialog(getContext(), drinkFinishedTimeListener, drinkFinishedHour, drinkFinishedMinute, false).show();
                break;
            case R.id.drinkFinishedDateButton:
                new DatePickerDialog(getContext(), drinkFinishedDateListener, drinkFinishedYear, drinkFinishedMonth, drinkFinishedDay).show();
                break;
            default:
                break;
        }
    }
}
