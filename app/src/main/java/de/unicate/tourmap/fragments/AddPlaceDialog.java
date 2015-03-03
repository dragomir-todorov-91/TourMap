package de.unicate.tourmap.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.unicate.tourmap.R;
import de.unicate.tourmap.models.Place;
import de.unicate.tourmap.utils.DatabaseHandler;

/**
 * Created by Drago on 16.2.2015 г..
 */
public class AddPlaceDialog extends DialogFragment implements TextView.OnEditorActionListener, View.OnClickListener {
    View fragmentDialogView;
    ImageView placePhoto;
    Place inputPlace;
    private EditText nameET, descriptionET;
    private Button cancelButton, okButton;

    public AddPlaceDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentDialogView = inflater.inflate(R.layout.fragment_add_item_dialog, container);
        getDialog().setTitle("Add a new place");


        // 1. Getting arguments from previous view and setting all items
        inputPlace = getArguments().getParcelable("data");

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateandTime = "" + sdf.format(new Date());

        // Current time
        inputPlace.setDatetime(currentDateandTime);

        //  Edit text fields to be taken out
        nameET = (EditText) fragmentDialogView.findViewById(R.id.etPlaceName);
        descriptionET = (EditText) fragmentDialogView.findViewById(R.id.etDescription);

        // Setting all fields data
        nameET.setText(inputPlace.getName());
        descriptionET.setText(inputPlace.getDescription());
        ((TextView) fragmentDialogView.findViewById(R.id.tvLat)).setText("Latitude: " + inputPlace.getLatitude());
        ((TextView) fragmentDialogView.findViewById(R.id.tvLong)).setText("Longtitude: " + inputPlace.getLongitude());
        ((TextView) fragmentDialogView.findViewById(R.id.tvTime)).setText(inputPlace.getDatetime());


        placePhoto = (ImageView) fragmentDialogView.findViewById(R.id.ivPlacePic);
        placePhoto.setOnClickListener(this);


        // Grabbing buttons and setting event listeners
        okButton = (Button) fragmentDialogView.findViewById(R.id.okButton);
        okButton.setOnClickListener(this);
        cancelButton = (Button) fragmentDialogView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        return fragmentDialogView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.okButton: {
                inputPlace.setName(nameET.getText().toString());
                inputPlace.setDescription(descriptionET.getText().toString());

                // Opening connection to database and dismissing dialog after
                DatabaseHandler db = new DatabaseHandler(getActivity());
                db.addPlace(inputPlace);
                db.close();

                // Dismissing dialog
                getDialog().dismiss();
                uploadOnServer(inputPlace);
                break;
            }
            case R.id.cancelButton: {
                getDialog().dismiss();
                break;
            }
            case R.id.ivPlacePic: {
                break;
            }
        }

    }

    public void uploadOnServer(Place p) {
        ParseObject places = new ParseObject("Places");
        places.put("Name", p.getName());
        places.put("Description", p.getDescription());
        places.put("Latitude", p.getLatitude());
        places.put("Longitude", p.getLongitude());
        places.put("Visited", p.getVisited());
        places.put("Date", p.getDatetime());
        places.put("UserID", "---");

//        Bitmap photo = p.getImageShot();
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//        ParseFile file = new ParseFile(p.getName(), stream.toByteArray());
//        places.put("Photo", file);

        places.saveInBackground();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            /*
            // Return input text to activity
            EditNameDialogListener activity = (EditNameDialogListener) getActivity();
            activity.onFinishEditDialog(mEditText.getText().toString());
            this.dismiss();
            return true;
            */
        }
        return false;
    }

    public interface AddPlaceDialogListener {
        void onFinishAddPlace(String inputText);
    }

}

