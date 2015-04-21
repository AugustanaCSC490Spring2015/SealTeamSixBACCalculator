package edu.augustana.csc490.bac_calculator;

/**
 * Created by Dan on 4/20/15.
 */
public class UntappdBeer {

    private String beerName;
    private String breweryName;
    private String beerLabel;
    private String beerABV;

    public UntappdBeer(String beerName, String breweryName, String beerLabel, String beerABV) {
        this.beerName = beerName;
        this.breweryName = breweryName;
        this.beerLabel = beerLabel;
        this.beerABV = beerABV;
    }

    public String getBeerName() {
        return beerName;
    }

    public String getBreweryName() {
        return breweryName;
    }

    public String getBeerLabel() {
        return beerLabel;
    }

    public String getBeerABV() {
        return beerABV;
    }
}
