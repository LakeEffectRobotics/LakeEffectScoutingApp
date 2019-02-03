package ca.lakeeffect.scoutingapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class PostgamePage extends Fragment {

    Button submit;
    Spinner climb;

    public PostgamePage() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        View view = inflator.inflate(R.layout.postgamepage, container, false);

        climb = view.findViewById(R.id.endgameClimb);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.climb, R.layout.spinner);
        climb.setAdapter(adapter);

        view.setTag("page3");

        return view;
    }

}
