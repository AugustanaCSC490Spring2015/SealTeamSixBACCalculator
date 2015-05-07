package edu.augustana.csc490.bac_calculator;

import java.util.Calendar;

/**
 * Created by Caitlin on 4/29/2015.
 */
public class Drink {

    private String drinkName;
    private String drinkABV;
    private String drinkVolume;
    private Calendar drinkStarted;
    private Calendar drinkEnded;
    private boolean drinkFinished;

    public Drink(String drinkName, String drinkABV, String drinkVolume, Calendar started, Calendar ended){
        this.drinkName = drinkName;
        this.drinkABV = drinkABV;
        this.drinkVolume = drinkVolume;
        this.drinkStarted = started;
        this.drinkEnded = ended;
        this.drinkFinished = false; // TODO:need to do a check
    }

    // default drink (for SavedPreferences)
    public Drink(){
        this.drinkName = "";
        this.drinkABV = "";
        this.drinkVolume = "";
        this.drinkStarted = Calendar.getInstance();
        this.drinkEnded = Calendar.getInstance();

    }

    public String getDrinkName() { return drinkName; }

    public Double getDrinkABV(){
        return Double.parseDouble(drinkABV);
    }

    public Double getDrinkVolume(){
        return Double.parseDouble(drinkVolume);
    }

    public Calendar getDrinkStartedCalendar() { return drinkStarted; }

    public Calendar getDrinkEndedCalendar(){ return drinkEnded; }

    public void setDrinkFinishedTime(Calendar finishedTime){
        this.drinkEnded = finishedTime;
        drinkFinished = true;
    }

    public boolean isDrinkFinished(){
        return drinkFinished;
    }
}
