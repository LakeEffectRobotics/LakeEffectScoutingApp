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

public class PostgamePage extends Fragment implements View.OnClickListener {

    Button submit;
    Spinner climb;

    public PostgamePage() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        View view = inflator.inflate(R.layout.postgamepage, container, false);

        submit = view.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        climb = view.findViewById(R.id.endgameClimb);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.climb, R.layout.spinner);
        climb.setAdapter(adapter);

        climb = view.findViewById(R.id.endgameClimbType);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.climbType, R.layout.spinner);
        climb.setAdapter(adapter);

        final RatingBar defenceRating = view.findViewById(R.id.defenceRating);
        final TextView defenceText = view.findViewById(R.id.defenceText);

        final Spinner endgameClimbType = (Spinner) view.findViewById(R.id.endgameClimbType);
        final Spinner endgameClimb = (Spinner) view.findViewById(R.id.endgameClimb);

        defenceRating.setVisibility(View.INVISIBLE);
        defenceText.setVisibility(View.INVISIBLE);
        endgameClimb.setVisibility(View.INVISIBLE);

        final CheckBox defence = (CheckBox) view.findViewById(R.id.defense);


        view.setTag("page3");

        defence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    defenceRating.setVisibility(View.VISIBLE);
                    defenceText.setVisibility(View.VISIBLE);
                }else{
                    defenceRating.setVisibility(View.INVISIBLE);
                    defenceText.setVisibility(View.INVISIBLE);
                }
            }
        });

        endgameClimbType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                Object item = parent.getItemAtPosition(pos);

                System.out.println(pos);

                if(pos >= 3){
                    endgameClimb.setVisibility(View.VISIBLE);
                }else{
                    endgameClimb.setVisibility(View.INVISIBLE);
                }

            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });


        return view;
    }
    public void onClick(View v) {
        //If the submit button is pressed
        if (v == submit) {
            //save the time
            MainActivity.lastSubmit = System.currentTimeMillis();

            //Confirm Dialog
            MainActivity.startNotificationAlarm(getContext());
            new AlertDialog.Builder(getActivity())
                    .setTitle("Submitting")
                    .setMessage("Are you sure you would like to submit?")
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(),
                                    "Keep scouting then...", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Save the data
                            if (((MainActivity) getActivity()).saveData()) {
                                Toast.makeText(getActivity(),
                                        "Saving", Toast.LENGTH_LONG).show();
                                //Reset the inputs
                                ((MainActivity) getActivity()).reset();
                            }

                        }
                    })
                    .create()
                    .show();
        }
    }
}
