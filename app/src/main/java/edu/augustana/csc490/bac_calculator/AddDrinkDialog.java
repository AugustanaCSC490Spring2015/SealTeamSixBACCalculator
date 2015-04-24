package edu.augustana.csc490.bac_calculator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

    }

    private void setDrinkFinishedButtonsEnabled(Boolean isEnabled) {
        drinkFinishedTimeButton.setEnabled(isEnabled);
        drinkFinishedDateButton.setEnabled(isEnabled);
        drinkFinishedLabel.setEnabled(isEnabled);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addDrinkButton:
                // do somethings
                break;
            case R.id.drinkStartedTimeButton:
                // do somethings
                break;
            case R.id.drinkStartedDateButton:
                //do something
                break;
            case R.id.drinkFinishedTimeButton:
                // do something
                break;
            case R.id.drinkFinishedDateButton:
                // do something
                break;
            default:
                break;
        }
    }
}
