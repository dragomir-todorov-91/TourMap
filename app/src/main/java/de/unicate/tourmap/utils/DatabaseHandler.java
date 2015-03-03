package de.unicate.tourmap.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.unicate.tourmap.models.Place;

/**
 * Created by Drago on 1.2.2015 г..
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // Constant variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "PlacesManager";

    // Places table name
    private static final String TABLE_PLACES = "places";


    // Places Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_VISITED = "visited";
    private static final String KEY_DATETIME = "datetime";

    private static final String KEY_MAPSHOT = "mapshot";
    private static final String KEY_IMAGESHOT = "imageshot";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_PLACES_TABLE = "CREATE TABLE " + TABLE_PLACES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_VISITED + " INT DEFAULT 0,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT,"
                + KEY_DATETIME + " TEXT,"
                + KEY_MAPSHOT + " BLOB,"
                + KEY_IMAGESHOT + " BLOB"
                + ")";

        db.execSQL(CREATE_PLACES_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new place
    public void addPlace(Place place) {
        SQLiteDatabase db = this.getWritableDatabase();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, place.getName()); // Name
        values.put(KEY_DESCRIPTION, place.getDescription()); // Description
        values.put(KEY_VISITED, place.getVisited()); // Is the place visited
        values.put(KEY_LATITUDE, place.getLatitude()); // Latitude
        values.put(KEY_LONGITUDE, place.getLongitude()); // Longitude
        values.put(KEY_DATETIME, place.getDatetime()); // DateTime

        Bitmap mapShot = place.getMapShot();
        Bitmap imageShot = place.getImageShot();

        byte[] bArray;

        if (mapShot != null) {
            // Compressing generated image to put in the db
            mapShot.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bArray = bos.toByteArray();
            values.put(KEY_MAPSHOT, bArray);
        }

        if (imageShot != null) {
            // Compressing generated image to put in the db
            imageShot.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bArray = bos.toByteArray();

            values.put(KEY_IMAGESHOT, bArray);
        }

        // Inserting Row
        db.insert(TABLE_PLACES, null, values);
        db.close(); // Closing database connection
    }

    // Getting single place by ID
    public Place getPlace(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query
                (TABLE_PLACES,
                        new String[]{KEY_ID, KEY_NAME, KEY_DESCRIPTION, KEY_VISITED, KEY_LATITUDE, KEY_LONGITUDE,
                                KEY_DATETIME, KEY_MAPSHOT, KEY_IMAGESHOT},
                        KEY_ID + "=?",
                        new String[]{String.valueOf(id)},
                        null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Place place = new Place(
                Integer.parseInt(cursor.getString(0)),     // ID
                cursor.getString(1),                       // Name
                cursor.getString(2),                       // Description
                Integer.parseInt(cursor.getString(3)), // Visited
                cursor.getString(4),                       // Latitude
                cursor.getString(5),                       // Longitude
                cursor.getString(6),                       // Datetime
                cursor.getBlob(7),                         // Map Shot
                cursor.getBlob(8)                          // Image Shot
        );

        // return the requested place
        return place;
    }

    // Getting All Places
    public List<Place> getAllPlaces() {
        List<Place> placesList = new ArrayList<Place>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PLACES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Place place = new Place();

                place.setID(Integer.parseInt(cursor.getString(0)));              // ID
                place.setName(cursor.getString(1));                              // Name
                place.setDescription(cursor.getString(2));                       // Description

                if (cursor.getInt(3) == 0)
                    place.setVisited(false);
                else
                    place.setVisited(true);

                place.setLatitude(cursor.getString(4));                          // Latitude
                place.setLongitude(cursor.getString(5));                         // Longitude
                place.setDatetime(cursor.getString(6));                          // Datetime
//              place.setMapShot(cursor.getBlob(7));                       // Map Shot
                if (cursor.getBlob(8) != null)
                    place.setImageShot(cursor.getBlob(8));                       // Image Shot

                // Adding contact to list
                placesList.add(place);
            } while (cursor.moveToNext());
        }

        // return contact list
        return placesList;
    }

/*
    public int setVisited(int id, boolean visited)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VISITED, place.getVisited());

        // return mDb.update(DATABASE_TABLE_TODO_LIST, con, "id ='" + id + "'",null);
        int code = db.update(TABLE_PLACES, values, KEY_ID + " = '" + place.getID() + "'",null);
        db.close();
        return code;
    }
*/

    // Updating single place by ID, used to update a place to visited
    public int updatePlace(Place place, boolean dataChange) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (dataChange) {
            values.put(KEY_NAME, place.getName());
            values.put(KEY_DESCRIPTION, place.getDescription());

            if (place.getImageShot() != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                place.getImageShot().compress(Bitmap.CompressFormat.PNG, 100, stream);
                values.put(KEY_IMAGESHOT, stream.toByteArray());
            }

        } else
            values.put(KEY_VISITED, place.getVisited());

        // updating row
        int code = db.update(TABLE_PLACES, values, KEY_ID + "=?", new String[]{String.valueOf(place.getID())});
        db.close();
        return code;

    }

    public void dropDBTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_PLACES);
        db.close();
    }

    // Deleting single place by ID
    public void deletePlace(Place place) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLACES, KEY_ID + " = ?",
                new String[]{String.valueOf(place.getID())});
        db.close();
    }


    // Get ID by place name
    public int getIdByPlaceName(String placeName) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_PLACES + " WHERE " + KEY_NAME + "='" + placeName + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            if (cursor.getString(1).equals(placeName)) {
                db.close();
                return Integer.parseInt(cursor.getString(0));
            }
        }
        return 0;
    }

    // Getting contacts Count
    public int getPlacesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PLACES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }


    public Place checkNearByPlace(String Lat, String Lng) {
        Place place = null;

        String query = "SELECT  * FROM " + TABLE_PLACES + " WHERE VISITED=0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String dbLat = cursor.getString(4);
                String dbLng = cursor.getString(5);

                double distance = getDistance(Double.parseDouble(Lat), Double.parseDouble(Lng),
                        Double.parseDouble(dbLat), Double.parseDouble(dbLng));

                if (distance <= 5) {
                    place = new Place();
                    place.setID(Integer.parseInt(cursor.getString(0)));              // ID
                    place.setName(cursor.getString(1));                              // Name
                    place.setDescription(cursor.getString(2));                       // Description
                    place.setVisited(Boolean.parseBoolean(cursor.getString(3)));     // Visited
                    place.setLatitude(cursor.getString(4));                          // Latitude
                    place.setLongitude(cursor.getString(5));                         // Longitude
                    place.setDatetime(cursor.getString(6));                          // Datetime
                    place.setImageShot(cursor.getBlob(8));                           // Image Shot

                    break;
                }
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return place;
    }

    /**
     * calculates the distance between two locations in KM
     */
    private double getDistance(double lat1, double lng1, double lat2, double lng2) {

        //double earthRadius = 3958.75; // in miles, change to 6371 for kilometers
        double earthRadius = 6371; // in miles, change to 6371 for kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        return dist;
    }

}
