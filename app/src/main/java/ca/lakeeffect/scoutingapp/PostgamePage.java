package ca.lakeeffect.scoutingapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PostgamePage extends Fragment {

    Spinner climb;

    //why is this here
    public PostgamePage() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        View view = inflator.inflate(R.layout.postgame_page, container, false);

        climb = view.findViewById(R.id.endgameClimb);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.climb, R.layout.spinner);
        climb.setAdapter(adapter);

        climb = view.findViewById(R.id.endgameClimbType);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.climbType, R.layout.spinner);
        climb.setAdapter(adapter);

        final Spinner endgameClimbType = view.findViewById(R.id.endgameClimbType);

        final Spinner endgameClimb = view.findViewById(R.id.endgameClimb);

        final TextView atClimbText = view.findViewById(R.id.climbAtText);

        endgameClimb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);

                System.out.println(pos);

                //if it parked
                if (pos == 1) {
                    endgameClimbType.setSelection(0);
                    endgameClimbType.setVisibility(View.INVISIBLE);
                    atClimbText.setVisibility(View.INVISIBLE);
                }else{
                    endgameClimbType.setVisibility(View.VISIBLE);
                    atClimbText.setVisibility(View.VISIBLE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}
