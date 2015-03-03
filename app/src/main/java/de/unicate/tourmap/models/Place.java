package de.unicate.tourmap.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Drago on 17.2.2015 г..
 */
public class Place implements Parcelable {
    // Information about the place
    Integer ID;
    String name;
    String description;
    Boolean visited;
    String latitude;
    String longitude;
    String datetime;

    // Two images if imageShot is  there  we show it instead of mapShot
    Bitmap mapShot; //TODO are we going to use this
    Bitmap imageShot; // todo or this

    // Constructors
    // Default constructor
    public Place() {
    }

    public Place(Integer ID, String name, String description, int visited, String latitude,
                 String longitude, String datetime, byte[] mapShot, byte[] imageShot) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        if (visited == 0) this.visited = false;
        else this.visited = true;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetime = datetime;

        if (mapShot != null)
            this.mapShot = BitmapFactory.decodeByteArray(mapShot, 0, mapShot.length);
        if (imageShot != null)
            this.imageShot = BitmapFactory.decodeByteArray(imageShot, 0, imageShot.length);
    }

    //

    // Setters and Getters


    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Boolean getVisited() {
        return visited;
    }

    public void setVisited(Boolean visited) {
        this.visited = visited;
    }


    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }


    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }


    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Bitmap getMapShot() {
        return mapShot;
    }

    public void setMapShot(byte[] mapShot) {
//        this.mapShot = BitmapFactory.decodeByteArray(mapShot, 0, mapShot.length);
    }


    public Bitmap getImageShot() {
        return imageShot;
    }

    public void setImageShot(byte[] imageShot) {
        if (imageShot != null)
            this.imageShot = BitmapFactory.decodeByteArray(imageShot, 0, imageShot.length);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
