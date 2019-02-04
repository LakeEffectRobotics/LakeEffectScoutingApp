package ca.lakeeffect.scoutingapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by Ajay on 9/25/2016.
 */
public class PregamePage extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        View view = inflator.inflate(R.layout.pregamepage, container, false);

        Spinner startSpinner = view.findViewById(R.id.startPositionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.startPosition, R.layout.spinner);
        startSpinner.setAdapter(adapter);

        view.setTag("page1");


        return view;

    }
}
