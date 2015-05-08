package edu.augustana.csc490.bac_calculator.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import edu.augustana.csc490.bac_calculator.Drink;

public class CalculatorManager {

    // Variables
    private static double totalAlcoholWithWidmark;
    public static double weightInPounds;
    private static double totalHoursSinceFirstDrink;
    private static double averageAlcoholEliminationRate;  // avg. is 0.015
    private static int howMuchAte;
    private static boolean isMale;
    private static double currentBAC;
    private static double futureBAC;
    private static Calendar futureMaxBACTime;
    private static Calendar futureSoberTime;

    public static ArrayList<Drink> drinkLog;
    public static Map<Calendar, Double> BACHistory;

    // SharedPreferences stuff
    public static SharedPreferences savedPreferences;

    // Will be called to get a "future" BAC, assuming the current drink will take 1 hour to drink; Future BAC will be a linear consumption of current drink.
    public static void calculateCurrentAndFutureBAC(){

        // prevents IndexOUtOfBounds if there are no drinks entered yet
        if(drinkLog.size() == 0){
            currentBAC = 0.0;
            futureBAC = 0.0;
            return;
        }

        // get current time for calculations
        Calendar currentTimeCalendar = Calendar.getInstance();
        long currentTimeInMS = currentTimeCalendar.getTime().getTime();
        Log.e("TIME", "CurrentTime:" + currentTimeInMS);

        // get time of last drink entered
        Drink latestDrink = drinkLog.get(drinkLog.size()-1);
        long latestDrinkTimeInMS = latestDrink.getDrinkStartedCalendar().getTime().getTime();
        Log.e("TIME", "LatestDrinkTime" + latestDrinkTimeInMS);

        // Calculate Current BAC

        // re-calculate totalHoursSinceFirstDrink
        long difference = currentTimeInMS - drinkLog.get(0).getDrinkStartedCalendar().getTimeInMillis();
        Log.e("TIME", "TIME DIFFERENCE:" + difference);
        totalHoursSinceFirstDrink = difference / 3600000; // 3600000ms in 1 hour
        Log.e("TIME", "TIME DIFFERENCE(HOURS):" + totalHoursSinceFirstDrink);

        // pull drinks from arraylist and re-calculate:
        totalAlcoholWithWidmark = 0.0;
        Log.e("BAC", "TOTALALCOZ:" + totalAlcoholWithWidmark);
        // get all drinks except the last one
        for (int i=0; i<drinkLog.size() - 1; i++){
            totalAlcoholWithWidmark += (drinkLog.get(i).getDrinkABV() * Constants.WIDMARKS_CONSTANT) * drinkLog.get(i).getDrinkVolume();  //calculate Alcohol Volume and add to total
            Log.e("BAC", "TOTALALCOZ:" + totalAlcoholWithWidmark);
        }

        // calculate alcohol in the latest drink
        double alcoholConsumedInLastDrink = 0.0;
        if (!latestDrink.isDrinkFinished()) { // make sure its not already finished
            if (difference < 1200000){ // calculate percentage of drink drank based on 20 minutes
                double drinkPercentage = (difference * 1.0) / 1200000.0; // multiply by 1.0 to convert to doubles, then do division
                Log.e("BAC", "HOURPERCENT:" + drinkPercentage);
                alcoholConsumedInLastDrink = (latestDrink.getDrinkABV() * Constants.WIDMARKS_CONSTANT) * (latestDrink.getDrinkVolume() * 1.0) * drinkPercentage;
            } else{ // if its longer than an hour, consider it all drank
                alcoholConsumedInLastDrink = (latestDrink.getDrinkABV() * Constants.WIDMARKS_CONSTANT) * (latestDrink.getDrinkVolume() * 1.0);
            }
        } else {  // if its already drank, then calculate it like normal
            alcoholConsumedInLastDrink = (latestDrink.getDrinkABV() * Constants.WIDMARKS_CONSTANT) * (latestDrink.getDrinkVolume() * 1.0);
            Log.e("BAC", "ALCINLASTELSE:" + alcoholConsumedInLastDrink);
        }
        Log.e("BAC", "ALCINLAST:" + alcoholConsumedInLastDrink);
        totalAlcoholWithWidmark += alcoholConsumedInLastDrink; // add last drink into total alcohol

        double bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();
        Log.e("BAC", "BODYWATER:" + bodyWater);
        double alcoholElimination = (averageAlcoholEliminationRate / Constants.GRAMS_IN_KILOGRAMS) * totalHoursSinceFirstDrink;
        Log.e("BAC", "TOTALALCOZ:" + totalAlcoholWithWidmark);
        currentBAC = totalAlcoholWithWidmark / bodyWater;
        currentBAC = currentBAC - alcoholElimination;
        Log.e("BAC", "ALCELIMINATION:" + alcoholElimination);
        Log.e("BAC", "Current BAC:" + Double.toString(CalculatorManager.currentBAC));
        saveBACPreferences();

        // Calculate Future BAC

        if (!latestDrink.isDrinkFinished()) { // make sure its not already finished

            // pull drinks from arraylist and calculate future BAC:
            totalAlcoholWithWidmark = 0.0;
            for (int i = 0; i < drinkLog.size(); i++) {
                totalAlcoholWithWidmark += (drinkLog.get(i).getDrinkABV() * Constants.WIDMARKS_CONSTANT) * (drinkLog.get(i).getDrinkVolume() * 1.0);  //calculate Alcohol Volume and add to total
            }

            bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();
            alcoholElimination = (averageAlcoholEliminationRate / Constants.GRAMS_IN_KILOGRAMS) * totalHoursSinceFirstDrink;
            futureBAC = (totalAlcoholWithWidmark / bodyWater) - alcoholElimination;

            // calculate future BAC time:
            long futureTime = latestDrinkTimeInMS + 3600000;
            futureMaxBACTime = Calendar.getInstance();
            futureMaxBACTime.setTimeInMillis(futureTime);

        } else {

            // just set it to the current BAC and Current time
            futureBAC = currentBAC;
            futureMaxBACTime.setTimeInMillis(latestDrinkTimeInMS);
        }
    }

    public static boolean finishDrink(){

        // prevent null pointers
        if (drinkLog.size() < 1){
            return false;
        }

        if (!drinkLog.get(drinkLog.size() -1).isDrinkFinished()) {

            // get current time for calculations
            Calendar currentTimeCalendar = Calendar.getInstance();
            long currentTimeInMS = currentTimeCalendar.getTime().getTime();
            Log.e("TIME", "CurrentTime:" + currentTimeInMS);

            // set end time for last drink
            Drink latestDrink = drinkLog.get(drinkLog.size() - 1);
            latestDrink.setDrinkFinishedTime(currentTimeCalendar);

            return true;
        } else {
            return false;
        }
    }

    private static void calculateFutureSoberTime(){

    }

    private static void calculateBACHistory(){
        // get current time for calculations
        Calendar currentTimeCalendar = Calendar.getInstance();
        currentTimeCalendar.setTimeInMillis(currentTimeCalendar.getTimeInMillis());

        BACHistory.put(currentTimeCalendar, currentBAC);

    }

    // Gets body water distribution that depends on male/female
    private static double getWaterConstant(){
        if(isMale){
            return Constants.MALE_CONSTANT;
        } else {
            return Constants.FEMALE_CONSTANT;
        }
    }

    public static void loadBACPreferences(){
        isMale = savedPreferences.getBoolean(Constants.PREF_GENDER, true); // default value of "Male"
        weightInPounds = Double.parseDouble(savedPreferences.getString(Constants.PREF_WEIGHT, "0.0"));  // default value of 0.0
        howMuchAte = savedPreferences.getInt(Constants.PREF_HOW_MUCH_ATE, 0);

        drinkLog = new ArrayList<Drink>();
        int drinkSize = savedPreferences.getInt(Constants.PREF_DRINK_LOG_SIZE, 0);
        for (int i = 0; i<drinkSize; i++){
            String json = savedPreferences.getString(Constants.PREF_DRINK_LOG + i, "");
            Gson gson = new Gson();
            Drink d = gson.fromJson(json, Drink.class);
            drinkLog.add(d);
        }
    }

    public static void saveBACPreferences(){
        SharedPreferences.Editor saver = savedPreferences.edit();
        saver.putBoolean(Constants.PREF_GENDER, isMale);
        saver.putString(Constants.PREF_WEIGHT, Double.toString(weightInPounds));
        saver.putInt(Constants.PREF_HOW_MUCH_ATE, howMuchAte);

        // save each object in the drinkLog
        // http://stackoverflow.com/questions/9186806/gson-turn-an-array-of-data-objects-into-json-android/9198626#9198626
        saver.putInt(Constants.PREF_DRINK_LOG_SIZE, drinkLog.size());
        Gson gson = new Gson();
        for(int i=0; i<drinkLog.size(); i++){
            String json = gson.toJson(drinkLog.get(i));
            saver.putString(Constants.PREF_DRINK_LOG + i, json);
        }
        saver.commit();
    }

    public static void calculateAverageAlcoholEliminationRate(){
        //http://www.alcohol.vt.edu/Students/Alcohol_effects/Intox_factors/index.html
        averageAlcoholEliminationRate = averageAlcoholEliminationRate;
    }

    public static double getAverageAlcoholEliminationRate(){

        return averageAlcoholEliminationRate;
    }

    public static void deleteAllDrinks(){
        drinkLog = new ArrayList<>();
    }

    public static void removeDrink(int id){
        drinkLog.remove(id);
    }

    public static void setIsMale(boolean b){
        isMale = b;
    }

    public static boolean getIsMale(){
        return isMale;
    }

    public static void setWeightInPounds(double weight){
        weightInPounds = weight;
    }

    public static double getCurrentBAC(){
        return currentBAC;
    }

    public static double getFutureBAC(){
        return futureBAC;
    }

    /**
     * 0 - none
     * 1 - a little
     * 2 - some
     * 3 - a lot
     * @param i - passed in from user settings
     */
    public static void setHowMuchAte(int i){
        howMuchAte = i;
    }

    public static int getHowMuchAte(){
        return howMuchAte;
    }

}
