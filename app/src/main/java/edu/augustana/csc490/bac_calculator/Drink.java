package edu.augustana.csc490.bac_calculator;

/**
 * Created by Caitlin on 4/29/2015.
 */
public class Drink {

    private String drinkName;
    private String drinkABV;
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

    public Drink(String drinkName, String drinkABV, int drinkStartedHour, int drinkStartedMinute, int drinkStartedDay, int drinkStartedMonth,
                 int drinkStartedYear, int drinkFinishedHour, int drinkFinishedMinute, int drinkFinishedDay, int drinkFinishedMonth,
                 int drinkFinishedYear) {

        this.drinkName = drinkName;
        this.drinkABV = drinkABV;
        this.drinkStartedHour = drinkStartedHour;
        this.drinkStartedMinute = drinkStartedMinute;
        this.drinkStartedDay = drinkStartedDay;
        this.drinkStartedMonth = drinkStartedMonth;
        this.drinkStartedYear = drinkStartedYear;
        this.drinkFinishedHour = drinkFinishedHour;
        this.drinkFinishedMinute = drinkFinishedMinute;
        this.drinkFinishedDay = drinkFinishedDay;
        this.drinkFinishedMonth = drinkFinishedMonth;
        this.drinkFinishedYear = drinkFinishedYear;


    }
}
