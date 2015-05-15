package edu.augustana.csc490.bac_calculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import edu.augustana.csc490.bac_calculator.utils.CalculatorManager;
import edu.augustana.csc490.bac_calculator.utils.Constants;

/**
 * Created by Dan on 4/29/15.
 */
public class UserSettingsActivity extends ActionBarActivity {

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
    private SharedPreferences sharedPreferences;
    private Button saveButton;
    private RadioGroup sexRadioGroup;
    private RadioGroup fullnessRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        weightTextView = (TextView) findViewById(R.id.weightTextView);
        lbsTextView = (TextView) findViewById(R.id.lbsTextView);
        lbsEditText = (EditText) findViewById(R.id.lbsEditText);
        genderTextView = (TextView) findViewById(R.id.sexTextView);
        sexRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
        maleRadioButton = (RadioButton) findViewById(R.id.maleRadioButton);
        femaleRadioButton = (RadioButton) findViewById(R.id.femaleRadioButton);
        aOFISTextView = (TextView) findViewById(R.id.aOFISTextView);
        fullnessRadioGroup = (RadioGroup) findViewById((R.id.foodInBellyRadiogroup));
        emptyRadioButton = (RadioButton) findViewById(R.id.emptyRadioButton);
        runningLowRadioButton = (RadioButton) findViewById(R.id.runningLowRadioButton);
        satisfiedRadioButton = (RadioButton) findViewById(R.id.satisfiedRadioButton);
        fullRadioButton = (RadioButton) findViewById(R.id.fullRadioButton);
        saveButton = (Button) findViewById(R.id.saveButton);


        sharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);

        if (CalculatorManager.getIsMale()){
            maleRadioButton.setChecked(true);
        } else {
            femaleRadioButton.setChecked(true);
        }

        lbsEditText.setText(""+CalculatorManager.getWeightInPounds());

        int amountAte = CalculatorManager.getHowMuchAte();

        if(amountAte == 0){
            emptyRadioButton.setChecked(true);
        } else if(amountAte == 1){
            runningLowRadioButton.setChecked((true));
        } else if(amountAte == 2){
            satisfiedRadioButton.setChecked(true);
        } else{
            fullRadioButton.setChecked(true);
        }

        // Set the up arrow in the ActionBar to the home screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Double.parseDouble(lbsEditText.getText().toString()) <= 0){
                    Toast.makeText(UserSettingsActivity.this, "Invalid weight", Toast.LENGTH_SHORT).show();
                } else {
                    int tempAte = 0;
                    if(runningLowRadioButton.isChecked()) {
                        tempAte = 1;
                    } else if (satisfiedRadioButton.isChecked()) {
                        tempAte =2;
                    } else if (fullRadioButton.isChecked()) {
                        tempAte=3;
                    }
                    CalculatorManager.setHowMuchAte(tempAte);

                    CalculatorManager.setIsMale(maleRadioButton.isChecked());

                    double weight = Double.parseDouble(lbsEditText.getText().toString());
                    CalculatorManager.setWeightInPounds(weight);

                    CalculatorManager.saveBACPreferences();

                    Toast.makeText(UserSettingsActivity.this, "Successfully saved your settings!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
