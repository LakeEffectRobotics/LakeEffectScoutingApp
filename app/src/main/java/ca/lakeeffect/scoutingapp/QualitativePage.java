package ca.lakeeffect.scoutingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class QualitativePage  extends Fragment implements View.OnClickListener {

    Button submit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        View view = inflator.inflate(R.layout.qualitativepage, container, false);

        final RatingBar defenceRating = view.findViewById(R.id.defenceRating);
        final TextView defenceText = view.findViewById(R.id.defenceText);

        defenceRating.setVisibility(View.INVISIBLE);
        defenceText.setVisibility(View.INVISIBLE);

        final CheckBox defence = (CheckBox) view.findViewById(R.id.defense);

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

        view.setTag("page4");

        submit = view.findViewById(R.id.submit);
        submit.setOnClickListener(this);

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
