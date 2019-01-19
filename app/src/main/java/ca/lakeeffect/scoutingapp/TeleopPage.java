package ca.lakeeffect.scoutingapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Ajay on 9/25/2016.
 * Xan's doing stuff now
 * Ha ha
 * Yay
 * Well, mainly system.out.printlns
 *      _
 *  .__(.)>
 *  \___)
 *
 *  copyright amazon.com
 *  well, just the duck
 */
public class TeleopPage extends Fragment implements View.OnClickListener {

    SurfaceView surface;
    Field field;

    Button pickup;
    Button drop;
    Button undo;
    Button fail;
    Button failedDropOff;


    //All the events made by the person this matchNumber
    ArrayList<Event> events = new ArrayList<Event>();

    Vibrator vibrator;
    boolean hasVibrator;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState){

        View view = inflator.inflate(R.layout.teleoppage, container, false);

        surface = view.findViewById(R.id.fieldCanvas);
        Bitmap fieldRed = BitmapFactory.decodeResource(getResources(), R.drawable.fieldred);
        Bitmap fieldBlue = BitmapFactory.decodeResource(getResources(), R.drawable.fieldblue);
        field = new Field(surface, fieldRed, fieldBlue);
        surface.setOnTouchListener(field);

        pickup = view.findViewById(R.id.pickupButton);
        pickup.setOnClickListener(this);
        drop = view.findViewById(R.id.dropButton);
        drop.setOnClickListener(this);
        undo = view.findViewById(R.id.undo);
        undo.setOnClickListener(this);
        fail = view.findViewById(R.id.failButton);
        fail.setOnClickListener(this);
        failedDropOff = view.findViewById(R.id.failDropOffButton);
        failedDropOff.setOnClickListener(this);

        view.setTag("page2");

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        hasVibrator = vibrator.hasVibrator();

        return view;
    }

    @Override
    public void onClick(View v) {



        if(v == undo){

            if(events.size() > 0){

                Event event = events.get(events.size()-1);

                String location = "";

                if(field.selected != -1) {
                    location += "location " + field.selected;
                } else{
                    location += "the field";
                }

                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm")
                        .setMessage("Are you sure you would like to undo the action that said " + getActionText(event.eventType) + location + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                events.remove(events.size()-1);
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
        if(hasVibrator) vibrator.vibrate(new long[] {0, 100, 25, 100}, -1);

        final Event event;
        int eventType = -1;

        String action = "";

        if(v == pickup) {
            eventType = 0;
        } else if(v == drop) {
            eventType = 1;
        } else if(v == fail) {
            eventType = 2;
        }else if(v == failedDropOff) {
            eventType = 3;
        }




        action = getActionText(eventType);

        if(field.selected != -1) {
            action += "location " + field.selected;
        } else{
            action += "the field";
        }

        if(eventType != -1) {
            final String a = action;
            event = new Event(eventType, field.selected, System.currentTimeMillis(), 0);
            if (hasVibrator) {
                addEvent(event, action, true);
            }
            else{
                addEvent(event, a, false);
                new AlertDialog.Builder(getContext())
                        .setTitle("You just clicked a button!")
                        .setMessage("You said that " + action + "\n\nThe even has already been registered, to undo it hit the ok button and then undo.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        }
    }

    private void addEvent(Event e, String action, boolean makeToast){
        events.add(e);

        if(makeToast){
            Toast.makeText(getContext(), "Event "+action+" recorded", Toast.LENGTH_SHORT).show();
        }
    }


    public String getActionText(int eventType){
        switch (eventType){
            case 0:
                return "that the robot picked up from ";
            case 1:
                return "that the robot dropped onto ";
            case 2:
                return "that the robot failed picking up in ";
            case 3:
                return "that the robot failed dropping off in ";
        }
        return "invalid event";
    }

    public void reset(){
        events.clear();
    }

    public String[] getData(){
        StringBuilder labels = new StringBuilder();
        StringBuilder data = new StringBuilder();

        int scaleHit=0;
        int scaleMiss=0;
        int ownSwitchHit=0;
        int ownSwitchMiss=0;
        int otherSwitchHit=0;
        int otherSwitchMiss=0;
        int vaultHit=0;
        int vaultMiss=0;

        for(Event e : events){
            int location = e.location;

            if((!MainActivity.side && MainActivity.alliance) || (MainActivity.side && !MainActivity.alliance)){
                location = MainActivity.flipLocation(location);
            }

            if(e.eventType==1){
                if(location==1){
                    vaultHit++;
                }
                if(location==4||location==5){
                    ownSwitchHit++;
                }
                if(location==6||location==7){
                    scaleHit++;
                }
                if(location==8||location==9){
                    otherSwitchHit++;
                }
            }
            if(e.eventType==3){
                if(location==1){
                    vaultMiss++;
                }
                if(location==4||location==5){
                    ownSwitchMiss++;
                }
                if(location==6||location==7){
                    scaleMiss++;
                }
                if(location==8||location==9){
                    otherSwitchMiss++;
                }
            }
        }

        labels.append("Own Switch Cubes,");
        data.append(ownSwitchHit+",");
        labels.append("Own Switch Miss,");
        data.append(ownSwitchMiss+",");
        labels.append("Scale Cubes,");
        data.append(scaleHit+",");
        labels.append("Scale Miss,");
        data.append(scaleMiss+",");
        labels.append("Other Switch Cubes,");
        data.append(otherSwitchHit+",");
        labels.append("Other Switch Miss,");
        data.append(otherSwitchMiss+",");
        labels.append("Vault Cubes,");
        data.append(vaultHit+",");
        labels.append("Vault Miss,");
        data.append(vaultMiss+",");

        return(new String[] {labels.toString(), data.toString()});
    }

}
