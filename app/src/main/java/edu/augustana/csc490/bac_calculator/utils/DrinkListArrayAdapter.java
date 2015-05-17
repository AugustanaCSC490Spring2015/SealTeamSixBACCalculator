package edu.augustana.csc490.bac_calculator.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.augustana.csc490.bac_calculator.Drink;
import edu.augustana.csc490.bac_calculator.R;

/**
 * Created by cschroeder on 5/7/15.
 */
public class DrinkListArrayAdapter extends BaseAdapter {

    private final Context context;
    private int resource;
    private final ArrayList<Drink> drinks;

    public DrinkListArrayAdapter(Context context, int resource, ArrayList<Drink> objects) {
        this.context = context;
        this.resource = resource;
        this.drinks = objects;
    }

    @Override
    public int getCount(){ return drinks.size(); }

    @Override
    public String getItem(int position) { return drinks.get(position).getDrinkName(); }

    @Override
    public long getItemId(int arg0) { return arg0; }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(resource, parent, false);

        TextView beerText = (TextView) rowView.findViewById(R.id.beerNameTextView);
        beerText.setText(drinks.get(position).getDrinkName() + ", " + drinks.get(position).getDrinkVolume() + " oz, "
                + drinks.get(position).getDrinkABV() + "% alcohol");


        return rowView;
    }

}
