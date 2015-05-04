package edu.augustana.csc490.bac_calculator;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by Dan on 4/29/15.
 */
public class UserSettingsActivity extends ActionBarActivity {

    private TextView heightTextView;
    private TextView feetTextView;
    private EditText feetEditText;
    private TextView inchesTextView;
    private EditText inchesEditText;
    private TextView weightTextView;
    private TextView lbsTextView;
    private EditText lbsEditText;
    private TextView genderTextView;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private TextView aOFISTextView;
    private RadioButton emptyRadioButton;
    private RadioButton runningLowRadioButton;
    private RadioButton satisfiedRadioButton;
    private RadioButton fullRadioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        heightTextView = (TextView) findViewById(R.id.heightTextView);
        feetTextView = (TextView) findViewById(R.id.feetTextView);
        feetEditText = (EditText) findViewById(R.id.feetEditText);
        inchesTextView = (TextView) findViewById(R.id.inchesTextView);
        inchesEditText = (EditText) findViewById(R.id.inchesEditText);
        weightTextView = (TextView) findViewById(R.id.weightTextView);
        lbsTextView = (TextView) findViewById(R.id.lbsTextView);
        lbsEditText = (EditText) findViewById(R.id.lbsEditText);
        genderTextView = (TextView) findViewById(R.id.genderTextView);
        maleRadioButton = (RadioButton) findViewById(R.id.maleRadioButton);
        femaleRadioButton = (RadioButton) findViewById(R.id.femaleRadioButton);
        aOFISTextView = (TextView) findViewById(R.id.aOFISTextView);
        emptyRadioButton = (RadioButton) findViewById(R.id.emptyRadioButton);
        runningLowRadioButton = (RadioButton) findViewById(R.id.runningLowRadioButton);
        satisfiedRadioButton = (RadioButton) findViewById(R.id.satisfiedRadioButton);
        fullRadioButton = (RadioButton) findViewById(R.id.fullRadioButton);



        // Set the up arrow in the ActionBar to the home screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
