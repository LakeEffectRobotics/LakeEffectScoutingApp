package ca.lakeeffect.scoutingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class QualitativePage  extends Fragment {

    Button submit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        View view = inflator.inflate(R.layout.qualitativepage, container, false);

        view.setTag("page4");

        submit = view.findViewById(R.id.submit);

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
