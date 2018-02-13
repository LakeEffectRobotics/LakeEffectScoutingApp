package ca.lakeeffect.scoutingapp;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    Button deselect;
    Button score;
    Button fail;

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
        deselect = (Button) view.findViewById(R.id.deselectButton);
        deselect.setOnClickListener(this);
        score = (Button) view.findViewById(R.id.scoreButton);
        score.setOnClickListener(this);
        fail = (Button) view.findViewById(R.id.failButton);
        fail.setOnClickListener(this);

        view.setTag("page2");

        return view;
    }

    @Override
    public void onClick(View v) {
        Event event = null;

        if(v == pickup) {
            event = new Event(0, field.selected, System.currentTimeMillis(), 0);
        } else if(v == drop) {
            event = new Event(1, field.selected, System.currentTimeMillis(), 0);
        } else if(v == deselect) {
            event = new Event(2, field.selected, System.currentTimeMillis(), 0);
        } else if(v == score) {
            event = new Event(3, field.selected, System.currentTimeMillis(), 0);
        } else if(v == fail) {
            event = new Event(4, field.selected, System.currentTimeMillis(), 0);
        }
    }
}
