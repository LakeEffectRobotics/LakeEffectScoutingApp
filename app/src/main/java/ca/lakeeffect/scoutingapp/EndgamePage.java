package ca.lakeeffect.scoutingapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to submit?")
                    .setTitle("Confirm Submission");
            AlertDialog dialog = builder.create();
//            https://developer.android.com/guide/topics/ui/dialogs.html
        }
    }
}
