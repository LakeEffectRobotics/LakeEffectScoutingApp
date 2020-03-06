package ca.lakeeffect.scoutingapp;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by Ajay on 9/25/2016.
 */
public class FieldUIPage extends Fragment implements View.OnClickListener {

    SurfaceView surface;
    Field field;

    Button undo;
    Button controlPanel;
    Button pickupButton;
    Button shootingButton;

    //All the events made by the person this matchNumber
    ArrayList<Event> events = new ArrayList<Event>();

    Vibrator vibrator;
    boolean hasVibrator;

    //is this the auto page, if so a different background color will be shown
    boolean autoPage;

    //only used if this is the auto page
    //if so, it will use this to determine if it is has been 15 seconds
    //if so, it will warn the user
    long firstPress = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        View view = inflator.inflate(R.layout.field_ui_page, container, false);

        if (autoPage) {
            TypedValue typedValue = new TypedValue();
            inflator.getContext().getTheme().resolveAttribute(R.attr.colorAuto, typedValue, true);
            view.setBackgroundColor(typedValue.data);

            //remove the control panel button if it's in auto
            View controlPanel = view.findViewById(R.id.controlPanel);
            ((ViewGroup) controlPanel.getParent()).removeView(controlPanel);
        }

        surface = view.findViewById(R.id.fieldCanvas);
        Bitmap fieldBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.field);
        field = new Field(this, surface, fieldBitmap, getContext(), getLayoutInflater());
        surface.setOnTouchListener(field);

        /*
        pickupHatch = view.findViewById(R.id.pickupHatch);
        pickupHatch.setOnClickListener(this);

        pickupCargo = view.findViewById(R.id.pickupCargo);
        pickupCargo.setOnClickListener(this);

        failPickupHatch = view.findViewById(R.id.failPickupHatch);
        failPickupHatch.setOnClickListener(this);

        failPickupCargo = view.findViewById(R.id.failPickupCargo);
        failPickupCargo.setOnClickListener(this);

         */

        undo = view.findViewById(R.id.undo);
        undo.setOnClickListener(this);

        pickupButton = view.findViewById(R.id.pickup);
        pickupButton.setOnClickListener(this);

        shootingButton = view.findViewById(R.id.shootButton);
        shootingButton.setOnClickListener(this);

        //button doesn't exist during autopage
        if(!autoPage){
            controlPanel = view.findViewById(R.id.controlPanel);
            controlPanel.setOnClickListener(this);
        }else{
            //set weight
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    pickupButton.getLayoutParams();
            params.weight = 3;
            pickupButton.setLayoutParams(params);

            params = (LinearLayout.LayoutParams)
                    shootingButton.getLayoutParams();
            params.weight = 3;
            shootingButton.setLayoutParams(params);
        }



        /*

        dropHatch = view.findViewById(R.id.dropHatch);
        dropHatch.setOnClickListener(this);

        dropCargo = view.findViewById(R.id.dropCargo);
        dropCargo.setOnClickListener(this);

        failDropHatch = view.findViewById(R.id.failDropHatch);
        failDropHatch.setOnClickListener(this);

        failDropCargo = view.findViewById(R.id.failDropCargo);
        failDropCargo.setOnClickListener(this);

         */

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        hasVibrator = vibrator.hasVibrator();

        return view;
    }

    @Override
    public void onClick(final View v) {
        System.out.println("onClick");
        System.out.println(firstPress);
        System.out.println(Field.getX());
        System.out.println(Field.getY());
        if (firstPress == -1 && autoPage && v != undo) {
            firstPress = System.currentTimeMillis();
        } else if (autoPage && System.currentTimeMillis() - firstPress > 15000 && v != undo) {
            //it has been 15 seconds, they should be done auto by now
            System.out.println("it's been 15 seconds");
            new AlertDialog.Builder(getContext())
                    .setTitle("YOU ARE ON THE SANDSTORM PAGE! It has been 15 seconds since your last press!")
                    .setMessage("Are you sure you would like to put an event? SANDSTORM should be done by now!")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            firstPress = -1;
                            FieldUIPage.this.onClick(v);
                        }
                    })
                    .setNegativeButton("No", null)
                    .create()
                    .show();
            return;
        }

        if (v == undo) {
            if (events.size() > 0) {
                /*
                Event event = events.get(events.size() - 1);

                String location = "";

                if (field.selected != -1) {
                    location += "location " + field.selected;
                } else {
                    location += "the field";
                }

                 */

                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm")
                        .setMessage("Are you sure you would like to undo the last action?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                events.remove(events.size() - 1);

                                if (events.size() == 0 && autoPage) {
                                    //reset first press time, nothing has happened
                                    firstPress = -1;
                                }
                                Toast.makeText(getContext(), "Undid the last action", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .create()
                        .show();
            } else {
                Toast.makeText(getContext(), "There is nothing to undo, you have not made any events yet", Toast.LENGTH_SHORT).show();
            }

            return; //only have to undo, not add an event
        }
        else if(v==pickupButton){
            float[] data = {1, 0, 0, 0, 0};
            float[] location = {Field.getX(), Field.getY()};
            long[] time = {System.currentTimeMillis(), System.currentTimeMillis()};
            addEvent(new Event(data, location, time, 0), "", false);
            Toast.makeText(getContext(), "Robot picked up ball", Toast.LENGTH_SHORT).show();
        }

        else if(v==shootingButton){
            final long windowOpenedTime = System.currentTimeMillis();

            final View mainInputView = getLayoutInflater().inflate(R.layout.main_input, null);

            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Input")
                    .setView(mainInputView)
                    .setPositiveButton("Ok", (new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //make a new event
                            float[] data = {
                                    0,
                                    ((RatingBar) mainInputView.findViewById(R.id.missedShots)).getRating(),
                                    ((RatingBar) mainInputView.findViewById(R.id.level1Shots)).getRating(),
                                    ((RatingBar) mainInputView.findViewById(R.id.level2Shots)).getRating(),
                                    ((RatingBar) mainInputView.findViewById(R.id.level3Shots)).getRating()};

                            float[] location = {Field.getX(), Field.getY()};

                            long[] time = {windowOpenedTime, System.currentTimeMillis()};

                            addEvent(new Event(data, location, time, 0), "", false);
                            Toast.makeText(getContext(), "Event recorded", Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .setNegativeButton("Cancel", null)
                    .create();

            alertDialog.show();
            alertDialog.getWindow().setLayout(MainActivity.getScreenWidth() - 10, MainActivity.getScreenHeight() - 10);
        }
        else if(v==controlPanel){
            final View spinnyPageView = getLayoutInflater().inflate(R.layout.spinny_boi_page, null);
            final long windowOpenedTime = System.currentTimeMillis();
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Input")
                    .setView(spinnyPageView)
                    .setPositiveButton("Ok", (new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //basically save what happened, but as an event too

                            float[] data = {-1, -1, -1, -1, -1};
                            float[] location = {-1, -1};
                            long[] time = {windowOpenedTime, System.currentTimeMillis()};

                            //woah, repetitive code!
                            //fixing this would be nice, but I don't know how
                            int metadata = -1;
                            if(((CheckBox) spinnyPageView.findViewById(R.id.rotationButtonSuccess)).isChecked()){
                                metadata = 1;
                            }
                            if(((CheckBox) spinnyPageView.findViewById(R.id.rotationButtonFail)).isChecked()){
                                metadata = 2;
                            }
                            if(((CheckBox) spinnyPageView.findViewById(R.id.colourButtonSuccess)).isChecked()){
                                metadata = 3;
                            }
                            if(((CheckBox) spinnyPageView.findViewById(R.id.colourButtonFail)).isChecked()){
                                metadata = 4;
                            }

                            addEvent(new Event(data, location, time, metadata), "", false);
                            Toast.makeText(getContext(), "Made control panel event", Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        }


        //Vibrate the vibrator to notify scout
        if (hasVibrator) vibrator.vibrate(new long[]{0, 100, 25, 100}, -1);

        /*
        final Event event;
        int eventType = -1;

        String action = "";

        //hatch eventTypes are from 0-3, cargo eventTypes are from 4-7
        if (v == pickupHatch) {
            eventType = 0;
        } else if (v == failPickupHatch) {
            eventType = 1;
        } else if (v == dropHatch) {
            eventType = 2;
        } else if (v == failDropHatch) {
            eventType = 3;
        } else if (v == pickupCargo) {
            eventType = 4;
        } else if (v == failPickupCargo) {
            eventType = 5;
        } else if (v == dropCargo) {
            eventType = 6;
        } else if (v == failDropCargo) {
            eventType = 7;
        }

        action = getActionText(eventType);

        if (field.selected != -1) {
            action += "location " + field.selected;
        } else {
            action += "the field";
        }

        if (eventType != -1) {
            final String a = action;
            event = new Event(eventType, field.selected, System.currentTimeMillis(), 0);
            if (hasVibrator) {
                addEvent(event, action, true);
            } else {
                addEvent(event, a, true);
            }
        }

        System.out.println("Hit the field");
        System.out.println(field.selected);


         */

    }

    public void addEvent(Event e, String action, boolean makeToast) {
        events.add(e);

        if (makeToast) {
            Toast.makeText(getContext(), "Event " + action + " recorded", Toast.LENGTH_SHORT).show();
        }

        System.out.println(events.size());
    }

    public String getActionText(int eventType) {
        String item = "hatch";
        if (eventType > 3) {
            //this converts it to as if it was a hatch event, as they have the same
            // messages other than the different item
            eventType -= 4;
            item = "cargo";
        }

        switch (eventType) {
            case 0:
                return "that the robot picked up a " + item + " from ";
            case 1:
                return "that the robot failed picking up a " + item + " in ";
            case 2:
                return "that the robot dropped a " + item + " in ";
            case 3:
                return "that the robot failed dropping off a " + item + " onto ";
        }
        return "invalid event";
    }

    public void reset() {
        events.clear();
    }

    //what this basically does is summarises the data, so that it can be put in the main data file
    //the individual events are got in MainActivity.getEventData, and saved seperately
    public String[] getData() {
        StringBuilder labels = new StringBuilder();
        StringBuilder data = new StringBuilder();

        String fieldPeriod = "TeleOp ";
        if (autoPage) {
            fieldPeriod = "Auto ";
        }

        int missedShots = 0;
        int lowerShots = 0;
        int outerShots = 0;
        int innerShots = 0;

        int successfulPickups = 0;

        int rotation = 0;
        int failedRotation = 0;
        int selection = 0;
        int failedSelection = 0;

        for (Event e : events) {
            float[] location = e.location;

            //float[] controlPanelLocation = {-1, -1};
            System.out.println("location");
            System.out.println(e.location[0]);
            if (e.location[0] == -1.0) {
                //this is a controlPanelEvent
                //TODO: this doesn't work
                switch(e.metadata){
                    case 1:
                        rotation++;
                        break;
                    case 2:
                        failedRotation++;
                        break;
                    case 3:
                        selection++;
                        break;
                    case 4:
                        failedSelection++;
                        break;
                    default:
                        break;
                }
            }else{
                missedShots += e.eventData[1];
                lowerShots += e.eventData[2];
                outerShots += e.eventData[3];
                innerShots += e.eventData[4];

                successfulPickups += e.eventData[0];
            }
        }
        //data
        labels.append(fieldPeriod + "Missed Shots,");
        data.append(missedShots + ",");

        labels.append(fieldPeriod + "Low Shots,");
        data.append(lowerShots + ",");

        labels.append(fieldPeriod + "Outer Shots,");
        data.append(outerShots + ",");

        labels.append(fieldPeriod + "Inner Shots,");
        data.append(innerShots + ",");

        labels.append(fieldPeriod + "Pickups,");
        data.append(successfulPickups + ",");

        //control panel data
        //only on teleop though
        if(!autoPage){
            labels.append("Rotation,");
            data.append(rotation + ",");

            labels.append("Failed Rotation,");
            data.append(failedRotation + ",");

            labels.append("Selection,");
            data.append(selection + ",");

            labels.append("Failed Selection,");
            data.append(failedSelection + ",");
        }


        return (new String[]{labels.toString(), data.toString()});

    }

}
