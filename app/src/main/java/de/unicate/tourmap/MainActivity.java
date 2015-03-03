package de.unicate.tourmap;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import de.unicate.tourmap.fragments.MapFragment;
import de.unicate.tourmap.fragments.PlacesFragment;
import de.unicate.tourmap.models.Place;
import de.unicate.tourmap.utils.CurrentUserLocation;
import de.unicate.tourmap.utils.DatabaseHandler;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    Menu menu;
    Toolbar toolbar;
    PlacesFragment placesFragment;
    MapFragment mapFragment;
    PendingIntent pi;
    BroadcastReceiver br;
    AlarmManager am;
    private boolean mapShown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupAlarm();

        //Parse.initialize(this, "jqz7G3jP0ANixc1PyNMVEWotzkYs2J5VMDUc05dk", "BLRmFl6Dj2nx9OE3zdZPvgpMVB19v38AAWlAYAr2");
        Parse.initialize(this, "A16uzwYgi0NN3NVlZxKsFSHlXBIsXjEObNGYXPH6", "jCWaEYWWO0rbjGMYaydGipy05Gw0teMmuwFAdxLz");
        //facebookLogIn();

        showMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(MainActivity.this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_show_places) {
            if (placesFragment == null)
                placesFragment = PlacesFragment.newInstance();

            // Changing to Places List -> showing PlacesFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, placesFragment)
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commit();

            menu.findItem(R.id.action_hide_places).setVisible(true);
            menu.findItem(R.id.action_show_places).setVisible(false);

            mapShown = false;
        }

        if (id == R.id.action_hide_places) {
            showMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMap() {
        // if(mapFragment == null)
        mapFragment = MapFragment.newInstance();

        // Changing to Maps -> showing Map View
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.container, mapFragment)
                .commit();


        if (menu != null) {
            menu.findItem(R.id.action_show_places).setVisible(true);
            menu.findItem(R.id.action_hide_places).setVisible(false);
        }

        mapShown = true;
    }


    // OnBackPressed handler, show map then exit
    @Override
    public void onBackPressed() {
        if (mapShown == false) {
            showMap();
        } else {
            super.onBackPressed();
        }
    }

    // Searching items
    @Override
    public boolean onQueryTextSubmit(String s) {
        if (mapShown == true)
            mapFragment.searchAddress(s);
        else
            placesFragment.searchPlace(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (mapShown == false)
            placesFragment.searchPlace(s);

        return false;
    }

    // Log in with facebook account
    private void facebookLogIn() {
        ParseFacebookUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                } else {
                    Log.d("MyApp", "User logged in through Facebook!");
                }
            }
        });
    }

    private void setupAlarm() {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("BroadcastReceiver", "onReceive");
                SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.user_data_pref_name), Context.MODE_PRIVATE);
                String userLat = sharedPref.getString(getString(R.string.user_lat), "");
                String userLng = sharedPref.getString(getString(R.string.user_lng), "");

                Log.d("Lat", userLat);
                Log.d("Lng", userLng);

                DatabaseHandler db = new DatabaseHandler(context);

                Place p = db.checkNearByPlace(userLat, userLng);
                if (p != null)
                    sendNotification(context, p.getName(), p.getDescription());


                UploadDBToServer();
            }
        };

        registerReceiver(br, new IntentFilter("de.unicate.tourmap"));
        pi = PendingIntent.getBroadcast(this, 0, new Intent("de.unicate.tourmap"), 0);
        am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        //am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60*60, 60*60, pi);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HALF_HOUR, pi);
        //am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, 10000, pi);
    }

    public void UploadDBToServer() {
        if (isOnline()) {
            DatabaseHandler db = new DatabaseHandler(this);
            //db.dropDBTable();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Places");
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> placesList, ParseException e) {
                    if (e == null) {
                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        Log.d("TourMap Parse.com", "Retrieved " + placesList.size() + " places");

                        for (int i = 0; i < placesList.size(); i++) {
                            int id = db.getIdByPlaceName(placesList.get(i).getString("Name"));

                            if (id == 0) {
                                Place p = new Place();

                                p.setName(placesList.get(i).getString("Name"));
                                p.setDescription(placesList.get(i).getString("Description"));
                                p.setLatitude(placesList.get(i).getString("Latitude"));
                                p.setLongitude(placesList.get(i).getString("Longitude"));
                                p.setVisited(placesList.get(i).getBoolean("Visited"));
                                p.setDatetime(placesList.get(i).getString("Date"));

                                db.addPlace(p);
                            }
                        }

                    } else {
                        Log.d("TourMap Parse.com", "Error: " + e.getMessage());
                    }
                }
            });
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onDestroy() {
        am.cancel(pi);
        unregisterReceiver(br);

        super.onDestroy();
    }

    public void sendNotification(Context activity, String notificationText, String notificationSubText) {

        // Create an intent that will be fired when the user clicks the notification.
        Intent intent = new Intent(activity, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);

        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity);

        // Icon in the notification bar. Also appears in the lower right hand corner of the notification itself.
        builder.setSmallIcon(R.drawable.abc_btn_radio_to_on_mtrl_015);
        // The content title, which appears in large type at the top of the notification
        builder.setContentTitle("Tour Map");
        // The content text, which appears in smaller text below the title
        builder.setContentText(notificationText);
        // Icon which appears on the left of the notification.
        builder.setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.drawable.abc_btn_radio_to_on_mtrl_015));
        // The subtext, which appears under the text on newer devices.
        // Devices running versions of Android prior to 4.2 will ignore this field, so don't use it for anything vital!
        builder.setSubText(notificationSubText);
        // Notification will disappear after the user taps it, rather than remaining until it's explicitly dismissed.
        builder.setAutoCancel(true);
        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Immediately display the notification icon in the notification bar.
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
        notificationManager.notify(26, builder.build());
    }
}