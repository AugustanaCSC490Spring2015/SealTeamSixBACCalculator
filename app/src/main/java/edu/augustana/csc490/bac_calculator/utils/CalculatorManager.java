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
    public static double weightInPounds;
    private static int howMuchAte;
    private static boolean isMale;
    private static double currentBAC;
    private static double futureBAC;
    private static double futureSoberTime; // time in a Double format

    private static ArrayList<Drink> drinkLog;
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

        // something to do before the calculations
        int drinksToEliminate = getHowManyDrinksToEliminate();
        Log.e("BAC", "DRINKSTOELIMINATE:" + drinksToEliminate);

        // if all the drinks are eliminated or none have been entered, don't bother calculating, and set values to 0;
        if (drinksToEliminate >= drinkLog.size()){
            currentBAC = 0.0;
            futureBAC = 0.0;
            futureSoberTime = 0.00;
            return;
        }

        // get current time for calculations
        long currentTimeInMS = getCurrentTimeInMS();

        // Get time of the first drink:
        Drink firstDrink = drinkLog.get(drinksToEliminate); // We don't want to count eliminated drinks, so we start with the number of eliminated ones
        long totalMSSinceFirstDrink = currentTimeInMS - firstDrink.getDrinkStartedCalendar().getTimeInMillis();

        /**
         *****Calculate Current BAC*****
         */

        // Calculate total Alcohol of all drinks except the last one
        double totalAlcoholWithWidmark = getTotalAlcoholWithWidmark(drinksToEliminate, drinkLog.size() - 1);

        // Get the latest drink
        Drink latestDrink = drinkLog.get(drinkLog.size()-1);

        // Calculate alcohol in the latest drink
        double alcoholConsumedInLastDrink = getTotalAlcoholWithWidmark(drinkLog.size() - 1, drinkLog.size());

        if (!latestDrink.isDrinkFinished()) { // make sure its not already finished

            // Get time since the last drink
            long latestDrinkTimeInMS = drinkLog.get(drinkLog.size()-1).getDrinkStartedCalendar().getTime().getTime();
            long difference = currentTimeInMS - latestDrinkTimeInMS;

            if (difference < 1200000){ // calculate percentage of drink drank based on 20 minutes (1200000ms in 20 minutes)
                double drinkPercentage = (difference * 1.0) / 1200000.0; // multiply by 1.0 to convert to doubles, then do division
                alcoholConsumedInLastDrink *= drinkPercentage;
            } else{ // if its longer than 20 minutes, consider it all drank and mark drink as drank
                finishDrink();
            }
        }

        totalAlcoholWithWidmark += alcoholConsumedInLastDrink; // add last drink into total alcohol

        double bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();

        // Get total hours since first drink
        double totalHoursSinceFirstDrink = (totalMSSinceFirstDrink * 1.0) / 3600000.0;

        double alcoholElimination = (getAverageAlcoholEliminationRate()) * totalHoursSinceFirstDrink;
        currentBAC = totalAlcoholWithWidmark / bodyWater;
        currentBAC = currentBAC - alcoholElimination;
        if(currentBAC <= 0.0){
            currentBAC = 0.0;
        }

        /*
        *****Calculate Future BAC*****
         */

        if (!latestDrink.isDrinkFinished()) { // make sure its not already finished

            // Pull drinks from arraylist and calculate future BAC:
            totalAlcoholWithWidmark = getTotalAlcoholWithWidmark(drinksToEliminate, drinkLog.size());
            futureBAC = calculateFutureMaxBAC(totalAlcoholWithWidmark, totalMSSinceFirstDrink);

        } else {

            // just set it to the current BAC and Current time
            futureBAC = currentBAC;
        }

        /*
        *****Calculate Future Sober Time*****
         */

        double numberOfHours = calculateFutureSoberTime(totalAlcoholWithWidmark, totalMSSinceFirstDrink);
        if (numberOfHours < 0.00){
            futureSoberTime = 0.00;
        } else {
            futureSoberTime = numberOfHours;
        }
        // Save Preferences to backup
        saveBACPreferences();
    }

    public static boolean isDrinkUnfinished() {
        if (drinkLog.size() > 0) {
            Drink latestDrink = drinkLog.get(drinkLog.size() - 1);
            if (latestDrink.isDrinkFinished()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private static double getTotalAlcoholWithWidmark(int start, int end){
        double totalAlcoholWithWidmark = 0.0;
        for (int i=start; i<end; i++){
            totalAlcoholWithWidmark += (drinkLog.get(i).getDrinkABV() * Constants.WIDMARKS_CONSTANT) * drinkLog.get(i).getDrinkVolume();  //calculate Alcohol Volume and add to total
        }
        return totalAlcoholWithWidmark;
    }

    public static double calculateASimpleFutureBAC(int start, int end){
        double totalAlcoholWithWidmark = getTotalAlcoholWithWidmark(start, end);
        double bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();

        // Get total hours since first drink
        double totalHoursSinceFirstDrink;
        Log.e("BAC", "" + (end - start));
        if ((end-start) > 1 && !drinkLog.get(end - 1).isDrinkFinished()){
            totalHoursSinceFirstDrink = getCurrentTimeInMS() - drinkLog.get(getHowManyDrinksToEliminate()).getDrinkStartedCalendar().getTimeInMillis();
        } else {
            totalHoursSinceFirstDrink = drinkLog.get(end - 1).getDrinkStartedCalendar().getTimeInMillis() - (drinkLog.get(0).getDrinkStartedCalendar().getTimeInMillis() * 1.0);
        }
        totalHoursSinceFirstDrink =  (totalHoursSinceFirstDrink * 1.0) / 3600000.0;
        double alcoholElimination = (getAverageAlcoholEliminationRate()) * totalHoursSinceFirstDrink;

        double BAC = totalAlcoholWithWidmark / bodyWater;

        BAC = BAC - alcoholElimination;
        return BAC;
    }

    public static long getCurrentTimeInMS(){
        Calendar currentTimeCalendar = Calendar.getInstance();
        return currentTimeCalendar.getTimeInMillis();
    }

    public static boolean finishDrink(){

        // prevent null pointers
        if (drinkLog.size() < 1){
            return false;
        }
        if (!drinkLog.get(drinkLog.size() -1).isDrinkFinished()) {

            // get current time for calculations
            Calendar currentTimeCalendar = Calendar.getInstance();

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
    public static int getHowManyDrinksToEliminate() {

        /*
        Traverse through the drinkLog, detecting if a drink is fully digested by calculating the sober time starting from
        the beginning and incrementing up and seeing if there is a greater time difference between the sober time for those
        drinks and the next drink started
         */
        for (int i = 1; i <= drinkLog.size(); i++) {
            long firstDrinkTime = drinkLog.get(0).getDrinkStartedCalendar().getTimeInMillis();
            long nextDrinkTime;

            // If you are on the last drink in the loop, check to see if it's finished or not, and use the current time, if needed
            if (i < drinkLog.size()) {
                nextDrinkTime = drinkLog.get(i).getDrinkStartedCalendar().getTimeInMillis();
            } else {
                if (!drinkLog.get(i - 1).isDrinkFinished()) {
                    nextDrinkTime = Calendar.getInstance().getTimeInMillis();
                } else {
                    nextDrinkTime = drinkLog.get(i - 1).getDrinkEndedCalendar().getTimeInMillis();
                }
            }

            long differenceTimeBetweenDrinks = nextDrinkTime - firstDrinkTime;
            double differenceInHours = (double) differenceTimeBetweenDrinks / 3600000; // 3600000ms in 1 hour

            // Calculate your sober time for the drinks
            double alcWithWidmark = getTotalAlcoholWithWidmark(0, i);
            double aFutureSoberTime = calculateFutureSoberTime(alcWithWidmark, differenceTimeBetweenDrinks);
            double differenceBetweenDrinks = aFutureSoberTime - differenceInHours;

            // If you get a negative difference between the future sober time and the time difference between consuming
            // the drinks, then eliminate the previous drinks, because they are already digested
            if (differenceBetweenDrinks <= 0.0) {
                return i;
            }
        }

        // If you never get a negative difference, then none of the drinks can be eliminated!
        return 0;
    }

    private static double calculateFutureSoberTime(double totalAlcoholWithWidmark, long totalMSSinceFirstDrink){

        //http://www.wsp.wa.gov/breathtest/docs/webdms/Studies_Articles/Widmarks%20Equation%2009-16-1996.pdf

        double futureSober = calculateFutureMaxBAC(totalAlcoholWithWidmark, totalMSSinceFirstDrink);
        // next step is to subtract BAC, but we want BAC=0, so we subtract 0
        futureSober = (futureSober / getAverageAlcoholEliminationRate());
        return futureSober;
    }

    private static double calculateFutureMaxBAC(double alcoholWithWidmarkConstant, long totalMSSinceFirstDrink){
        double bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();
        double totalHoursSinceFirstDrink = (totalMSSinceFirstDrink * 1.0) / 3600000.0;
        double alcoholElimination = (getAverageAlcoholEliminationRate()) * totalHoursSinceFirstDrink;
        double BAC =  (alcoholWithWidmarkConstant / bodyWater);
        return BAC - alcoholElimination;
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
        weightInPounds = Double.parseDouble(savedPreferences.getString(Constants.PREF_WEIGHT, "180.0"));  // default value of 180.0
        howMuchAte = savedPreferences.getInt(Constants.PREF_HOW_MUCH_ATE, 0);

        drinkLog = new ArrayList<>();
        int drinkSize = savedPreferences.getInt(Constants.PREF_DRINK_LOG_SIZE, 0);
        for (int i = 0; i<drinkSize; i++){
            String json = savedPreferences.getString(Constants.PREF_DRINK_LOG + i, "");
            Gson gson = new Gson();
            Drink d = gson.fromJson(json, Drink.class);
            drinkLog.add(d);
        }
        if (drinkLog == null){
            drinkLog = new ArrayList<>();
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
        for(int i=0; i<drinkLog.size(); i++) {
            String json = gson.toJson(drinkLog.get(i));
            saver.putString(Constants.PREF_DRINK_LOG + i, json);
        }
        saver.commit();
    }

    public static double getAverageAlcoholEliminationRate(){

        if (isMale){
            return 0.017;
        } else {
            return 0.015;
        }
    }

    public static void deleteAllDrinks(){
        drinkLog.removeAll(drinkLog); // good code?
        calculateCurrentAndFutureBAC(); // update calculation right away
        saveBACPreferences();
    }

    public static void removeDrink(int id){
        drinkLog.remove(id);
        calculateCurrentAndFutureBAC(); // update calculation right away
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
        return futureSoberTime;
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

    public static double getWeightInPounds() { return weightInPounds; }

    public static int getDrinkLogSize(){
        return drinkLog.size();
    }

    public static void addDrink(Drink drink){
        drinkLog.add(drink);
    }

    public static Drink getDrink(int d){
        return drinkLog.get(d);
    }

    public static Drink getFirstDrink(){
        return drinkLog.get(getHowManyDrinksToEliminate());
    }

    public static ArrayList<Drink> getDrinkLog(){
        return drinkLog;
    }
}
