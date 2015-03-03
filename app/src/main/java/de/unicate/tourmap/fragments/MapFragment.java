package de.unicate.tourmap.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

import de.unicate.tourmap.ItemDetailsActivity;
import de.unicate.tourmap.R;
import de.unicate.tourmap.models.Place;
import de.unicate.tourmap.utils.CurrentUserLocation;
import de.unicate.tourmap.utils.DatabaseHandler;

/**
 * Created by André on 22.11.2014.
 */
public class MapFragment extends Fragment implements View.OnClickListener, GoogleMap.OnInfoWindowClickListener {
    private final float MapZoom = 15;
    View fragmentView;
    ImageView floatingButtonIV;
    Marker markerPointer;
    String userLongitude, userLatitude;
    Place newPlace = new Place(); // saving the information on the new place to add
    private MarkerOptions markerOptions;
    private LatLng latLng;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //
    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.fragment_map, container, false);

        // Getting floating button image and setting it animation and onclick listener
        this.floatingButtonIV = (ImageView) fragmentView.findViewById(R.id.floatingButton);
        Animation floating_button_slide_in = AnimationUtils.loadAnimation(getActivity(), R.anim.floating_button_slide_in);
        floatingButtonIV.startAnimation(floating_button_slide_in);
        (fragmentView.findViewById(R.id.floatingButton)).setOnClickListener(this);

        setUpMapIfNeeded(); // Shows the map
        //showAllMarkersOnMap(); // Shows all places on the map
        mMap.setOnInfoWindowClickListener(this);

        return fragmentView;
    }

    // Function to set up mMap
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                showUserPosition();
            }
        }
    }

    // Show user current position
    private void showUserPosition() {
        // Initial location setter

        mMap.setMyLocationEnabled(true);

        // Zooming camera to user position
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

        double latitude;
        double longtitude;

        if (location == null)
            location = mMap.getMyLocation();

        // Getting current location and setting it in shared preferences
        CurrentUserLocation userLocation = new CurrentUserLocation(getActivity());
        if(location != null)
            userLocation.onLocationChanged(location);

        if (location != null) {
            // Getting location coordinates
            latitude = location.getLatitude();
            longtitude = location.getLongitude();

            // After we get users coordinates we save them for later use
            userLatitude = String.valueOf(latitude);
            userLongitude = String.valueOf(longtitude);

            // Setting new details
            newPlace.setLongitude(userLongitude);
            newPlace.setLatitude(userLatitude);
            newPlace.setVisited(true);

            // Fly over the camera to the user location on the map
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), MapZoom));
        }
    }


    // OnClick listener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floatingButton: {
                // Showing dialog fragment to enter a new place in DB
                FragmentManager fm = getActivity().getSupportFragmentManager();
                AddPlaceDialog editPlaceDetails = new AddPlaceDialog();

                // Setting map details into the passed data
                Bundle placeData = new Bundle();
                placeData.putParcelable("data", newPlace);
                editPlaceDetails.setArguments(placeData);

                editPlaceDetails.show(fm, "add_place_fragment");

                break;
            }
        }
    }


    // Search for an address or an object
    public void searchAddress(String address) {
        // Getting user input location
        if (address != null && !address.equals("")) {
            // Ion getter of google maps places data

            String placesApiKey = "AIzaSyC1gXC5oVnzLB2jjEY7c1OflpX7SHJ-faQ";
            String type = "json";
            String link = "https://maps.googleapis.com/maps/api/place/textsearch/" + type + "?query=" + address.replaceAll(" ", "%20") + "&key=" + placesApiKey;
            Ion.with(this)
                    .load(link)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            if (result != null && result.get("status") != null && result.get("status").getAsString().equalsIgnoreCase("ok")) {
                                JsonArray placeArr = result.get("results").getAsJsonArray();
                                JsonObject firstPlace = placeArr.get(0).getAsJsonObject();

                                String nameText = firstPlace.get("name").getAsString();
                                String addressText = firstPlace.get("formatted_address").getAsString(); // to be used as description
                                String Lat = firstPlace.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsString();
                                String Lng = firstPlace.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsString();

                                // Remove last unused marker
                                if (markerPointer != null)
                                    markerPointer.remove();

                                // Setting all data about the requested place
                                newPlace.setLatitude(Lat);
                                newPlace.setLongitude(Lng);
                                newPlace.setName(nameText);
                                newPlace.setDescription(addressText);
                                newPlace.setVisited(false);

                                // set address marker options
                                latLng = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lng));

                                markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(firstPlace.get("name").getAsString());
                                markerOptions.snippet(Lat + ", " + Lng);

                                markerPointer = mMap.addMarker(markerOptions);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapZoom));
                            }
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showAllMarkersOnMap();
    }

    private void showAllMarkersOnMap() {
        mMap.clear();
        DatabaseHandler db = new DatabaseHandler(getActivity());
        List<Place> places = db.getAllPlaces();
        Place place;

        for (int i = 0; i < places.size(); i++) {
            place = places.get(i);

            // set address marker options
            latLng = new LatLng(Double.parseDouble(place.getLatitude()), Double.parseDouble(place.getLongitude()));

            markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(place.getVisited() ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED));

            markerOptions.title(place.getName());
            markerOptions.snippet(place.getLatitude() + ", " + place.getLongitude());

            mMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        DatabaseHandler db = new DatabaseHandler(getActivity());

        int id = db.getIdByPlaceName(marker.getTitle());
        if (id != 0) {
            Intent viewSingleItem = new Intent(getActivity(), ItemDetailsActivity.class);
            viewSingleItem.putExtra("itemID", id);
            getActivity().startActivity(viewSingleItem);
        }

    }
}
