package de.unicate.tourmap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import de.unicate.tourmap.R;

/**
 * Created by Drago on 25.2.2015 г..
 */
public class CurrentUserLocation implements LocationListener {

    protected LocationManager locationManager;
    Context con;

    public CurrentUserLocation(Context context) {
        this.con = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d("onLocationChanged", "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());

        SharedPreferences sharedPref = con.getSharedPreferences(con.getString(R.string.user_data_pref_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(con.getString(R.string.user_lat), latitude);
        editor.putString(con.getString(R.string.user_lng), longitude);
        editor.commit();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }
}
