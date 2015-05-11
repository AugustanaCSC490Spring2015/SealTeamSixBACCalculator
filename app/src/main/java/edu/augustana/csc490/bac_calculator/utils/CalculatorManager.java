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
    //private static double totalAlcoholWithWidmark;
    public static double weightInPounds;
    //private static double totalHoursSinceFirstDrink;
    private static double averageAlcoholEliminationRate;  // avg. is 0.015
    private static int howMuchAte;
    private static boolean isMale;
    private static double currentBAC;
    private static double futureBAC;
    private static double futureSober; // time in a Double format
   // private static Calendar futureMaxBACTime;
   // private static Calendar futureSoberTime;
   // private static int numOfDrinksToEliminate;

    public static ArrayList<Drink> drinkLog;
    public static Map<Calendar, Double> BACHistory;

    // SharedPreferences stuff
    public static SharedPreferences savedPreferences;

    // Will be called to get a "future" BAC, assuming the current drink will take 1 hour to drink; Future BAC will be a linear consumption of current drink.
    public static void calculateCurrentAndFutureBAC(){

        /**
         * http://www.wsp.wa.gov/breathtest/docs/webdms/Studies_Articles/Widmarks%20Equation%2009-16-1996.pdf
         * Widmark Formula:
         * A = (Wr(Ct+Bt))/(0.8z)  (Solve for Ct)
         *
         * A: number of drinks consumed
         * W: body weight (ounces)
         * r: body water distribution constant (L/Kg)
         * Ct: BAC (Kg/L)
         * B: alcohol elimination rate (Kg/L/hr)
         * t: time since first drink (hours)
         * 0.8: Widmark's constant
         * z: fluid ounces of alcohol/drink
         */

        // prevents IndexOUtOfBounds if there are no drinks entered yet
        if(drinkLog.size() == 0){
            currentBAC = 0.0;
            futureBAC = 0.0;
            return;
        }

        // something to do before the calculations
        int drinksToEliminate = getHowManyDrinksEliminated();
        Log.e("BAC", "DRINKSTOELIMINATE:" + drinksToEliminate);

        // if all the drinks are eliminated, don't bother calculating
        if (drinksToEliminate >= drinkLog.size()){
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
        long latestDrinkTimeInMS = 0;
        if (latestDrink != null){
            latestDrinkTimeInMS = latestDrink.getDrinkStartedCalendar().getTime().getTime();
        }
        Log.e("TIME", "LatestDrinkTime" + latestDrinkTimeInMS);

        // another thing to do
        calculateAverageAlcoholEliminationRate();

        // Calculate Current BAC

        // re-calculate totalHoursSinceFirstDrink
        long difference = currentTimeInMS - latestDrinkTimeInMS;
        Log.e("BAC", "DIFFERENCE:" + difference);
        double totalHoursSinceFirstDrink = (double) difference / 3600000; // 3600000ms in 1 hour
        Log.e("BAC", "TOTAL-HOURS:" + totalHoursSinceFirstDrink);

        // pull drinks from arraylist and re-calculate:
        double totalAlcoholWithWidmark = getTotalAlcoholWithWidmark(drinksToEliminate, drinkLog.size() - 1);
        // get all drinks except the last one

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
        }
        totalAlcoholWithWidmark += alcoholConsumedInLastDrink; // add last drink into total alcohol

        double bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();
        double alcoholElimination = (getAverageAlcoholEliminationRate()) * totalHoursSinceFirstDrink;
        currentBAC = totalAlcoholWithWidmark / bodyWater;
        currentBAC = currentBAC - alcoholElimination;
        if(currentBAC <= 0.0){
            currentBAC = 0.0;
        }
        Log.e("BAC", "ALCELIMINATION:" + alcoholElimination);
        Log.e("BAC", "Current BAC:" + Double.toString(CalculatorManager.currentBAC));
        saveBACPreferences();

        // Calculate Future BAC

        if (!latestDrink.isDrinkFinished()) { // make sure its not already finished

            // pull drinks from arraylist and calculate future BAC:
            totalAlcoholWithWidmark = getTotalAlcoholWithWidmark(drinksToEliminate, drinkLog.size());
            futureBAC = calculateAFutureBAC(totalAlcoholWithWidmark, totalHoursSinceFirstDrink);

            // calculate future BAC time:
            long differenceBetweenStartAndNow = currentTimeInMS - drinkLog.get(drinkLog.size()-drinksToEliminate-1).getDrinkStartedCalendar().getTimeInMillis();
            double numberOfHours = calculateFutureSoberTime(totalAlcoholWithWidmark, difference);
            numberOfHours = numberOfHours * 3600000;
            numberOfHours = numberOfHours - differenceBetweenStartAndNow;
            futureSober = numberOfHours / 3600000;

        } else {

            // just set it to the current BAC and Current time
            futureBAC = currentBAC;
           // futureMaxBACTime.setTimeInMillis(latestDrinkTimeInMS);
        }
    }

    private static double getTotalAlcoholWithWidmark(int start, int end){
        double totalAlcoholWithWidmark = 0.0;
        for (int i=start; i<end; i++){
            totalAlcoholWithWidmark += (drinkLog.get(i).getDrinkABV() * Constants.WIDMARKS_CONSTANT) * drinkLog.get(i).getDrinkVolume();  //calculate Alcohol Volume and add to total
        }
        return totalAlcoholWithWidmark;
    }

    private static long getCurrentTimeInMS(){
        Calendar currentTimeCalendar = Calendar.getInstance();
        long currentTimeInMS = currentTimeCalendar.getTime().getTime();
        Log.e("TIME", "CurrentTime:" + currentTimeInMS);
        return currentTimeInMS;
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

    // need to check to see if a drink is completely eliminated from the blood stream -
    // this would be when you have a drink, and your body metabolizes it completely
    // before the next drink
    private static int getHowManyDrinksEliminated(){
        if(drinkLog.size() == 0){
            return 0;
        }else if (drinkLog.size() == 1){ //if current BAC is 0, set all drinks to be eliminated
            long currentTime = getCurrentTimeInMS();
            long difference = currentTime - drinkLog.get(0).getDrinkStartedCalendar().getTimeInMillis();
            double totalHours = difference / 3600000;
            double alcWithWidmark = (drinkLog.get(0).getDrinkABV() * Constants.WIDMARKS_CONSTANT) * drinkLog.get(0).getDrinkVolume();
            if(calculateAFutureBAC(alcWithWidmark, totalHours) <= 0.0){
                return 1;
            } else {
                return 0;
            }
        } else {
            // calculate BACs in descending number of drinks, checking to see if any drinks should be eliminated
            int drinksEliminated = 0;
            for (int i = drinkLog.size() - 1; i >=1; i--){
                long currentTime = Calendar.getInstance().getTimeInMillis();
                long difference = currentTime - drinkLog.get(i).getDrinkStartedCalendar().getTimeInMillis();
                double totalHours = difference / 3600000;
                double alcWithWidmark = getTotalAlcoholWithWidmark(i, drinkLog.size() - 1);
                double BAC = calculateAFutureBAC(alcWithWidmark, totalHours);
                if(BAC <= 0.0){
                    for(int j=0; i<0; j++){
                    }
                    drinksEliminated = i+1;
                    if(i -1 ==0){
                        return 0;
                    }
                }
            }
            return drinksEliminated;
        }
    }

    public static double calculateFutureSoberTime(double totalAlcoholWithWidmark, double totalHoursSinceFirstDrink){

        //http://www.wsp.wa.gov/breathtest/docs/webdms/Studies_Articles/Widmarks%20Equation%2009-16-1996.pdf

        futureSober = calculateAFutureBAC(totalAlcoholWithWidmark, totalHoursSinceFirstDrink);
        // next step is to subtract BAC, but we want BAC=0, so we subtract 0
        return (futureSober / getAverageAlcoholEliminationRate());
    }

    private static double calculateAFutureBAC(double alcoholWithWidmarkConstant, double totalHours){
        double bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();
        double alcoholElimination = (averageAlcoholEliminationRate) * totalHours;
        double futBAC = (alcoholWithWidmarkConstant / bodyWater);
        Log.e("BAC", "FUTBAC:" + futBAC);
        return futBAC;
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
        Log.e("BAC", "DRINKSIZE:" + drinkSize);
        for (int i = 0; i<drinkSize; i++){
            String json = savedPreferences.getString(Constants.PREF_DRINK_LOG + i, "");
            Gson gson = new Gson();
            Drink d = gson.fromJson(json, Drink.class);
            drinkLog.add(d);
        }
        if (drinkLog == null){
            drinkLog = new ArrayList<Drink>();
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
        averageAlcoholEliminationRate = 0.015;
    }

    public static double getAverageAlcoholEliminationRate(){

        return averageAlcoholEliminationRate;
    }

    public static void deleteAllDrinks(){

        drinkLog.removeAll(drinkLog);
        calculateCurrentAndFutureBAC();
        futureSober = 0.0;
        saveBACPreferences();
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

    public static double getFutureSoberTime(){
        return futureSober;
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
