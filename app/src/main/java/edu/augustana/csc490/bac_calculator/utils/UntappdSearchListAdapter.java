package edu.augustana.csc490.bac_calculator.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import edu.augustana.csc490.bac_calculator.R;
import edu.augustana.csc490.bac_calculator.UntappdBeer;

/**
 * Created by Dan on 4/20/15.
 */
public class UntappdSearchListAdapter extends ArrayAdapter<UntappdBeer> {

    private final Context context;
    private int resource;
    private final List<UntappdBeer> beers;

    public UntappdSearchListAdapter(Context context, int resource, List<UntappdBeer> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.beers = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(resource, parent, false);

        TextView beerName = (TextView) rowView.findViewById(R.id.beerName);
        TextView breweryName = (TextView) rowView.findViewById(R.id.breweryName);
        TextView beerABV = (TextView) rowView.findViewById(R.id.beerABV);
        ImageView beerLabel = (ImageView) rowView.findViewById(R.id.beerLabel);

        beerName.setText(beers.get(position).getBeerName());
        breweryName.setText(beers.get(position).getBreweryName());
        beerABV.setText(beers.get(position).getBeerABV() + "%");

        ImageLoader.getInstance().displayImage(beers.get(position).getBeerLabel(), beerLabel);

        return rowView;
    }

}
