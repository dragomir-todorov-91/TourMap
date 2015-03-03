package de.unicate.tourmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import de.unicate.tourmap.models.Place;
import de.unicate.tourmap.utils.DatabaseHandler;

public class ItemDetailsActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1888;

    Toolbar toolbar;
    Place inputPlace;
    private EditText nameET, descriptionET;
    private Button visitedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        // Getting the relieved data
        Integer placeID = getIntent().getExtras().getInt("itemID");

        DatabaseHandler db = new DatabaseHandler(this);
        inputPlace = db.getPlace(placeID);

        //  Edit text fields to be taken out
        nameET = (EditText) findViewById(R.id.etPlaceNameDet);
        descriptionET = (EditText) findViewById(R.id.etDescriptionDet);

        // Setting all fields data
        nameET.setText(inputPlace.getName());
        descriptionET.setText(inputPlace.getDescription());
        ((TextView) findViewById(R.id.tvLatDet)).setText("Latitude: " + inputPlace.getLatitude());
        ((TextView) findViewById(R.id.tvLongDet)).setText("Longtitude: " + inputPlace.getLongitude());
        ((TextView) findViewById(R.id.tvTimeDet)).setText(inputPlace.getDatetime());
        ((TextView) findViewById(R.id.tvVisitedDet)).setText((inputPlace.getVisited()) ? "Visited" : "Not Visited");

        visitedButton = (Button) findViewById(R.id.buttonVisited);
        if (inputPlace.getVisited()) {
            visitedButton.setText("Unvisited");
        } else {
            visitedButton.setText("Visited");
        }
        visitedButton.setOnClickListener(this);

        Bitmap photo = inputPlace.getImageShot();
        if (photo != null)
            ((ImageView) findViewById(R.id.ivPlacePicDet)).setImageBitmap(photo);

        Button deletePlace = (Button) findViewById(R.id.buttonDeletePlace);
        deletePlace.setOnClickListener(this);
        Button changeDataButton = (Button) findViewById(R.id.buttonChangeData);
        changeDataButton.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            finish();
        }
        if (id == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonVisited: {
                DatabaseHandler db = new DatabaseHandler(this);

                if (inputPlace.getVisited() == false)
                    inputPlace.setVisited(true);
                else
                    inputPlace.setVisited(false);

                if (db.updatePlace(inputPlace, false) == 1) {
                    if (inputPlace.getVisited()) {
                        visitedButton.setText("Unvisited");
                    } else {
                        visitedButton.setText("Visited");
                    }
                    ((TextView) findViewById(R.id.tvVisitedDet)).setText((inputPlace.getVisited()) ? "Visited" : "Not Visited");
                }
                break;
            }
            case R.id.buttonDeletePlace: {
                DatabaseHandler db = new DatabaseHandler(this);
                db.deletePlace(inputPlace);
                finish();
            }
            case R.id.buttonChangeData: {
                DatabaseHandler db = new DatabaseHandler(this);

                inputPlace.setName(((EditText) findViewById(R.id.etPlaceNameDet)).getText().toString());
                inputPlace.setDescription(((EditText) findViewById(R.id.etDescriptionDet)).getText().toString());
                db.updatePlace(inputPlace, true);
                finish();
            }
        }
    }

    public void takePicture(View v) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            ImageView img = (ImageView) findViewById(R.id.ivPlacePicDet);

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(photo);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            inputPlace.setImageShot(stream.toByteArray());
        }
    }
}
