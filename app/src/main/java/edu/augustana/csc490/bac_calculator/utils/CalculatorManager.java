package edu.augustana.csc490.bac_calculator.utils;

import android.content.SharedPreferences;
import edu.augustana.csc490.bac_calculator.utils.Constants;

public class CalculatorManager {

    // Variables
    public static double totalAlcoholInOunces;
    public static double weightInPounds;
    public static double totalHoursSinceFirstDrink;
    public static double averageAlcoholEliminationRate;  // avg. is 0.015
    public static boolean isMale;

    // SharedPreferences stuff
    public static SharedPreferences savedPreferences;

    public static double calculateCurrentBAC() {

        /**
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

        double bodyWater = weightInPounds * Constants.OUNCES_IN_POUNDS * getWaterConstant();
        double alcoholElimination = (averageAlcoholEliminationRate / Constants.GRAMS_IN_KILOGRAMS) * totalHoursSinceFirstDrink;

        return ((totalAlcoholInOunces* Constants.WIDMARKS_CONSTANT) / bodyWater) - alcoholElimination;
    }

    // Adds drink to total alcohol in system
    public static void addToTotalAchohol(double ounces, double alcoholPercent){

        double totalAlcoholInDrink = ounces * alcoholPercent;
        totalAlcoholInOunces += totalAlcoholInDrink;

    }

    // Gets body water distribution that depends on male/female
    public static double getWaterConstant(){
        if(isMale){
            return Constants.MALE_CONSTANT;
        } else {
            return Constants.FEMALE_CONSTANT;
        }
    }

    public static void loadBACPreferences(){
        totalAlcoholInOunces = Double.parseDouble(savedPreferences.getString(Constants.PREF_TOTAL_ALCOHOL, "0.0"));  // default value of 0.0
        isMale = savedPreferences.getBoolean(Constants.PREF_GENDER, true); // default value of "Male"
        weightInPounds = Double.parseDouble(savedPreferences.getString(Constants.PREF_WEIGHT, "0.0"));  // default value of 0.0
        totalHoursSinceFirstDrink = Double.parseDouble(savedPreferences.getString(Constants.PREF_TOTAL_HOURS, "0.0"));  // default value of 0.0
        averageAlcoholEliminationRate = Double.parseDouble(savedPreferences.getString(Constants.PREF_AVG_ALC_ELIMINATION_RATE, "0.015")); // default value of 0.015 (average)
    }

    public static void saveBACPreferences(){
        savedPreferences.edit().putString(Constants.PREF_TOTAL_ALCOHOL, totalAlcoholInOunces + "");
        savedPreferences.edit().putBoolean(Constants.PREF_GENDER, isMale);
        savedPreferences.edit().putString(Constants.PREF_WEIGHT, weightInPounds + "");
        savedPreferences.edit().putString(Constants.PREF_TOTAL_HOURS, totalHoursSinceFirstDrink + "");
        savedPreferences.edit().putString(Constants.PREF_AVG_ALC_ELIMINATION_RATE, averageAlcoholEliminationRate + "");
    }
}
