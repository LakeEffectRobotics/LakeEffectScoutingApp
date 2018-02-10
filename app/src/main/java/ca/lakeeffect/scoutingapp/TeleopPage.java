package ca.lakeeffect.scoutingapp;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Ajay on 9/25/2016.
 */
public class TeleopPage extends Fragment {

    SurfaceView surface;
    Field field;

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

        view.setTag("page2");

        return view;
    }
}
