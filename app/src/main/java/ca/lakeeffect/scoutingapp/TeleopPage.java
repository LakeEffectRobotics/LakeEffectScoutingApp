package ca.lakeeffect.scoutingapp;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by Ajay on 9/25/2016.
 */
public class TeleopPage extends Fragment implements View.OnClickListener {

    SurfaceView surface;
    Field field;

    Button pickup;
    Button drop;
    Button fail;
    Button failedDropOff;

    //All the events made by the person this round
    ArrayList<Event> events = new ArrayList<Event>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState){

        View view = inflator.inflate(R.layout.teleoppage, container, false);

        surface = (SurfaceView) view.findViewById(R.id.fieldCanvas);
        field = new Field(surface, BitmapFactory.decodeResource(getResources(), R.drawable.field));
        surface.setOnTouchListener(field);

        pickup = (Button) view.findViewById(R.id.pickupButton);
        pickup.setOnClickListener(this);
        drop = (Button) view.findViewById(R.id.dropButton);
        drop.setOnClickListener(this);
        fail = (Button) view.findViewById(R.id.failButton);
        fail.setOnClickListener(this);
        failedDropOff = (Button) view.findViewById(R.id.failDropOffButton);
        failedDropOff.setOnClickListener(this);

        view.setTag("page2");

        return view;
    }

    @Override
    public void onClick(View v) {
        final Event event;
        int eventType = -1;

        String action = "";

        if(v == pickup) {
            eventType = 0;
            action = "say that the robot picked up from ";
        } else if(v == drop) {
            eventType = 1;
            action = "say that the robot dropped onto ";
        } else if(v == fail) {
            eventType = 2;
            action = "say that the robot failed picking up in ";
        }else if(v == failedDropOff) {
            eventType = 3;
            action = "say that the robot failed dropping off in ";
        }

        if(field.selected != -1) {
            action += "location " + field.selected;
        } else{
            action += "the field";
        }

        if(eventType != -1){

            event = new Event(eventType, field.selected, System.currentTimeMillis(), 0);

            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm")
                    .setMessage("Are you sure you would like to " + action + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            events.add(event);
                        }
                    })
                    .setNegativeButton("No", null)
                    .create()
                    .show();
        }
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
            if(e.eventType==1){
                if(location==2){
                    vaultHit++;
                }
                if(location==5||location==6){
                    ownSwitchHit++;
                }
                if(location==7||location==8){
                    scaleHit++;
                }
                if(location==9||location==10){
                    otherSwitchHit++;
                }
            }
            if(e.eventType==3){
                if(location==2){
                    vaultMiss++;
                }
                if(location==5||location==6){
                    ownSwitchMiss++;
                }
                if(location==7||location==8){
                    scaleMiss++;
                }
                if(location==9||location==10){
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

        return(new String[] {labels.toString(), data.toString()});
    }

}
