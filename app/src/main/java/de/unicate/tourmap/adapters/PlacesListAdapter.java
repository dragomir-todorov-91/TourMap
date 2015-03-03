package de.unicate.tourmap.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.unicate.tourmap.ItemDetailsActivity;
import de.unicate.tourmap.R;
import de.unicate.tourmap.models.Place;
import de.unicate.tourmap.utils.DatabaseHandler;

/**
 * Created by Drago on 22.2.2015 г..
 */
public class PlacesListAdapter extends BaseAdapter {
    List<Place> places, inputData;
    private Context context;

    public PlacesListAdapter(Context con) {
        this.context = con;
        DatabaseHandler db = new DatabaseHandler(con);
        places = db.getAllPlaces();
        inputData = db.getAllPlaces();

    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public Place getItem(int position) {
        return places.get(position);
    }

    @Override
    public long getItemId(int position) {
        return places.get(position).getID();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Getting all single row items
        ImageView ivMapImage;
        TextView tvPlaceName;
        LinearLayout placeRow;

        if (convertView == null) {
            // We create a view and fill the data within it
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_items_single_item, parent, false);

            // Getting all elements on screen
            ivMapImage = (ImageView) convertView.findViewById(R.id.placeImageList);
            tvPlaceName = (TextView) convertView.findViewById(R.id.placeName);

            // Getting a single row
            placeRow = (LinearLayout) convertView.findViewById(R.id.placeRow);

            // Setting tags for faster work with
            convertView.setTag(R.id.placeImageList, ivMapImage);
            convertView.setTag(R.id.placeName, tvPlaceName);
            convertView.setTag(R.id.placeRow, placeRow);
        } else {
            // Getting the items by tags
            ivMapImage = (ImageView) convertView.getTag(R.id.placeImageList);
            tvPlaceName = (TextView) convertView.getTag(R.id.placeName);
            placeRow = (LinearLayout) convertView.getTag(R.id.placeRow);
        }

        final Place currentItem = getItem(position);

        // if (currentItem.getMapShot() != null)
        //     ivMapImage.setImageBitmap(currentItem.getMapShot());
        if (currentItem.getImageShot() != null)
            ivMapImage.setImageBitmap(currentItem.getImageShot());

        tvPlaceName.setText(currentItem.getName());

        placeRow.setOnClickListener(null); // null-yfy onClickListener before setting new one
        placeRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewSingleItem = new Intent(context, ItemDetailsActivity.class);

                viewSingleItem.putExtra("itemID", currentItem.getID());

                context.startActivity(viewSingleItem);
            }
        });


        return convertView;
    }

    public void filterData(String placeName) {
        places = new ArrayList<Place>(inputData);

        if (placeName != null || placeName != "") {
            for (int i = 0; i < places.size(); i++) {
                if (!places.get(i).getName().toLowerCase().contains(placeName.toLowerCase())) {
                    places.remove(i);
                    i--;
                }
            }
        }
    }
}
