package edu.augustana.csc490.bac_calculator.utils;

import android.content.SharedPreferences;

public class CalculatorManager {

    // Body Water Constants
    public static final double maleConstant = 0.68;     // (L/Kg)
    public static final double femaleConstant = 0.55;   // L/Kg

    // Widmark's Constant
    public static final double widmarksConstant = 0.8;

    public static final double ouncesInPounds = 16.0;
    public static final double gramsInKilograms = 1000.0;

    public static double totalAlcoholInOunces;
    public static double weightInPounds;
    public static double totalHoursSinceFirstDrink;
    public static double averageAlcoholEliminationRate;  // avg. is 0.015
    public static boolean isMale;

    public static SharedPreferences savedPreferences;


    public static double calculateCurrentBAC() {

        /**
         * Widmark Formula:
         * A = (Wr(Ct+Bt))/(8z)  (Solve for Ct)
         *
         * A: number of drinks consumed
         * W: body weight (ounces)
         * r: body water distribution constant (L/Kg)
         * Ct: BAC (Kg/L)
         * B: alcohol elimination rate (Kg/L/hr)
         * t: time since first drink (hours)
         * z: fluid ounces of alcohol/drink
         */

        double bodyWater = weightInPounds * ouncesInPounds * getWaterConstant();
        double alcoholElimation = (averageAlcoholEliminationRate / gramsInKilograms) * totalHoursSinceFirstDrink;

        return ((totalAlcoholInOunces* widmarksConstant) / bodyWater) - alcoholElimation;
    }

    public static void addToTotalAchohol(double ounces, double alcoholPercent){

        double totalAlcoholInDrink = ounces * alcoholPercent;
        totalAlcoholInOunces += totalAlcoholInDrink;

    }

    public static double getWaterConstant(){
        if(isMale){
            return maleConstant;
        } else {
            return femaleConstant;
        }
    }
}
