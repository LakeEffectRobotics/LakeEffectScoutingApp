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
import android.widget.LinearLayout;
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

    Button pickupHatch;
    Button pickupCargo;
    Button failPickupHatch;
    Button failPickupCargo;
    Button undo;
    Button dropHatch;
    Button dropCargo;
    Button failDropHatch;
    Button failDropCargo;

    //All the events made by the person this matchNumber
    static ArrayList<Event> events = new ArrayList<Event>();

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
        if (firstPress == -1 && autoPage && v != undo) {
            firstPress = System.currentTimeMillis();
        } else if (autoPage && System.currentTimeMillis() - firstPress > 15000 && v != undo) {
            //it has been 15 seconds, they should be done auto by now
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
                Event event = events.get(events.size() - 1);

                String location = "";

                if (field.selected != -1) {
                    location += "location " + field.selected;
                } else {
                    location += "the field";
                }

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

    public String[] getData() {
        StringBuilder labels = new StringBuilder();
        StringBuilder data = new StringBuilder();

        String fieldPeriod = "TeleOp ";
        if (autoPage) {
            fieldPeriod = "Auto ";
        }

        //hatchHit, hatchMiss, cargoHit, cargoMiss
        int[] cargoShip = new int[4];
        int[] sideCargoShip = new int[4];
        int[] levelOneRocket = new int[4];
        int[] levelTwoRocket = new int[4];
        int[] levelThreeRocket = new int[4];
        int[] farRocket = new int[4];
        int[] closeRocket = new int[4];
        int[] fullRocket = new int[4];
        //the robot scored into any position
        int[] everything = new int[4];

        int[] cargoShipLocations = {12, 13, 14, 15, 16, 17, 18};
        int[] sideCargoShipLocations = {12, 13, 14, 16, 17, 18};
        int[] levelOneRocketLocations = {4, 5, 10, 11};
        int[] levelTwoRocketLocations = {2, 3, 8, 9};
        int[] levelThreeRocketLocations = {0, 1, 6, 7};
        int[] farRocketLocations = {1, 3, 5, 7, 9, 11};
        int[] closeRocketLocations = {0, 2, 4, 6, 8, 10};

        String[] labelActions = {"Hatch Hit", "Hatch Miss", "Cargo Hit", "Cargo Miss"};

        for (Event e : events) {
            float[] location = e.location;

            /*
            int id = 0;
            switch (e.eventType) {
                case 2:
                    //eventType 2: dropHatch
                    id = 0;
                    break;

                case 3:
                    //eventType 3: failDropHatch
                    id = 1;
                    break;

                case 6:
                    //eventType 6: dropCargo
                    id = 2;
                    break;

                case 7:
                    //eventType 7: failDropCargo
                    id = 3;
                    break;

                default:
                    break;
            }

             */
            
            /*
            //cargo ship
            if (MainActivity.arrayContains(cargoShipLocations, location)) {
                cargoShip[id]++;
            }
            if (MainActivity.arrayContains(sideCargoShipLocations, location)) {
                sideCargoShip[id]++;
            }

            //rocket levels
            if (MainActivity.arrayContains(levelOneRocketLocations, location)) {
                levelOneRocket[id]++;
                fullRocket[id]++;
            }
            if (MainActivity.arrayContains(levelTwoRocketLocations, location)) {
                levelTwoRocket[id]++;
                fullRocket[id]++;
            }
            if (MainActivity.arrayContains(levelThreeRocketLocations, location)) {
                levelThreeRocket[id]++;
                fullRocket[id]++;
            }

            //far and close rocket
            if (MainActivity.arrayContains(farRocketLocations, location)) {
                farRocket[id]++;
            }
            if (MainActivity.arrayContains(closeRocketLocations, location)) {
                closeRocket[id]++;
            }

            //totals for everything
            everything[id]++;

             */
        }

        for (int i = 0; i < labelActions.length; i++) {
            //overall data
            labels.append(fieldPeriod + "Total " + labelActions[i] + ",");
            data.append(everything[i] + ",");

            //most important data
            labels.append(fieldPeriod + "Full Cargo Ship " + labelActions[i] + ",");
            data.append(cargoShip[i] + ",");
            labels.append(fieldPeriod + "Full Rocket " + labelActions[i] + ",");
            data.append(fullRocket[i] + ",");

            //extra more detailed information
            labels.append(fieldPeriod + "Side Cargo Ship " + labelActions[i] + ",");
            data.append(sideCargoShip[i] + ",");

            if (i == 0 || i == 1) {
                //only for hatch, not for cargo
                labels.append(fieldPeriod + "Far Rocket " + labelActions[i] + ",");
                data.append(farRocket[i] + ",");
                labels.append(fieldPeriod + "Close Rocket " + labelActions[i] + ",");
                data.append(closeRocket[i] + ",");
            }
            
            labels.append(fieldPeriod + "Level 1 Rocket " + labelActions[i] + ",");
            data.append(levelOneRocket[i] + ",");
            labels.append(fieldPeriod + "Level 2 Rocket " + labelActions[i] + ",");
            data.append(levelTwoRocket[i] + ",");
            labels.append(fieldPeriod + "Level 3 Rocket " + labelActions[i] + ",");
            data.append(levelThreeRocket[i] + ",");
        }

        return (new String[]{labels.toString(), data.toString()});

    }

}
