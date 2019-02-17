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

    public PostgamePage() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        View view = inflator.inflate(R.layout.postgamepage, container, false);

        climb = view.findViewById(R.id.endgameClimb);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.climb, R.layout.spinner);
        climb.setAdapter(adapter);

        climb = view.findViewById(R.id.endgameClimbType);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.climbType, R.layout.spinner);
        climb.setAdapter(adapter);

        final Spinner endgameClimbType = (Spinner) view.findViewById(R.id.endgameClimbType);
        final Spinner endgameClimb = (Spinner) view.findViewById(R.id.endgameClimb);

        endgameClimb.setVisibility(View.INVISIBLE);

        endgameClimbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);

                System.out.println(pos);

                if (pos >= 3) {
                    endgameClimb.setVisibility(View.VISIBLE);
                } else {
                    endgameClimb.setVisibility(View.INVISIBLE);
                    endgameClimb.setSelection(0);
                }

            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}
