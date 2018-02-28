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
            action = "say that the robot picked up from location " + field.selected;
        } else if(v == drop) {
            eventType = 1;
            action = "say that the robot dropped onto location " + field.selected;
        } else if(v == fail) {
            eventType = 2;
            action = "say that the robot failed picking up in location " + field.selected;
        }else if(v == failedDropOff) {
            eventType = 3;
            action = "say that the robot failed dropping off in location " + field.selected;
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
}
