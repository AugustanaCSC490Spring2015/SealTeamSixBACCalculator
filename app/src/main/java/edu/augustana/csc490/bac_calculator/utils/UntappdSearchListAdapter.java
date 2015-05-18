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
 * The UntappdSearchListAdapter extends ArrayAdapter of UntappdBeer objects and is used to display
 * the Untappd search results, holding information like a lazy-loaded and cached image, name,
 * brewery, and ABV.
 */
public class UntappdSearchListAdapter extends ArrayAdapter<UntappdBeer> {

    private final Context context; // The current context.
    private int resource; //The resource ID for a layout file containing a TextView to use when instantiating views.
    private final List<UntappdBeer> beers; // The objects to represent in the ListView.

    /**
     * Constructor sets class variables to given parameters
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public UntappdSearchListAdapter(Context context, int resource, List<UntappdBeer> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.beers = objects;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Inflate layout and get the current row
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);

        // Get references to layout components
        TextView beerName = (TextView) rowView.findViewById(R.id.beerName);
        TextView breweryName = (TextView) rowView.findViewById(R.id.breweryName);
        TextView beerABV = (TextView) rowView.findViewById(R.id.beerABV);
        ImageView beerLabel = (ImageView) rowView.findViewById(R.id.beerLabel);

        // Set values
        beerName.setText(beers.get(position).getBeerName());
        breweryName.setText(beers.get(position).getBreweryName());
        beerABV.setText(beers.get(position).getBeerABV() + "%");

        // Cache and display image using Universal Image Loader Library
        ImageLoader.getInstance().displayImage(beers.get(position).getBeerLabel(), beerLabel);

        return rowView;
    }

}
