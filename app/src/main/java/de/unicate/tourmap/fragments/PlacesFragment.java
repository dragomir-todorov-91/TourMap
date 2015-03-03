package de.unicate.tourmap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.unicate.tourmap.R;
import de.unicate.tourmap.adapters.PlacesListAdapter;

/**
 * Created by André on 22.11.2014.
 */
public class PlacesFragment extends android.support.v4.app.Fragment {
    View fragmentView;
    PlacesListAdapter customAdapter;
    ListView listView;

    public static PlacesFragment newInstance() {
        return new PlacesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_items, container, false);


        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // List view and it's adapter
        listView = (ListView) fragmentView.findViewById(R.id.listView);
        customAdapter = new PlacesListAdapter(getActivity());
        listView.setAdapter(customAdapter);
    }

    public void searchPlace(String placeName) {
        customAdapter.filterData(placeName);
        listView.setAdapter(customAdapter);
    }
}
