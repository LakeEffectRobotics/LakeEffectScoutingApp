package ca.lakeeffect.scoutingapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class EndgamePage extends Fragment implements View.OnClickListener{

    Button submit;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState){
        View view = inflator.inflate(R.layout.endgamepage, container, false);
        submit = (Button) view.findViewById(R.id.submit);
        submit.setOnClickListener(this);
        return view;
    }

    public void onClick(View v){
        if(v==submit){
            new AlertDialog.Builder(getActivity())
                .setTitle("Submiting")
                .setMessage("Are you sure you would like to submit?")
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),
                                "You hit no", Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton(android.R.string.yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),
                                "Your Message", Toast.LENGTH_LONG).show();
                        Toast.makeText(getActivity(),
                                "Your Message", Toast.LENGTH_LONG).show();
                        Toast.makeText(getActivity(),
                                "Your Message", Toast.LENGTH_LONG).show();
                        Toast.makeText(getActivity(),
                                "Your Message", Toast.LENGTH_LONG).show();
                        Toast.makeText(getActivity(),
                                "Your Message", Toast.LENGTH_LONG).show();
                        Toast.makeText(getActivity(),
                                "Your Message", Toast.LENGTH_LONG).show();Toast.makeText(getActivity(),
                                "Your Message", Toast.LENGTH_LONG).show();
                    }
                })
                .create();
        }
    }
}


