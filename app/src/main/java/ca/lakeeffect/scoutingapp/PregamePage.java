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

        view.setTag("page1");

        Spinner spinner = view.findViewById(R.id.autoStartLocation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.startPosition, R.layout.spinner);
        spinner.setAdapter(adapter);

        spinner = view.findViewById(R.id.firstAutoCubeLocation);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.places, R.layout.spinner);
        spinner.setAdapter(adapter);

        spinner = view.findViewById(R.id.secondAutoCubeLocation);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.places, R.layout.spinner);
        spinner.setAdapter(adapter);

        spinner = view.findViewById(R.id.thirdAutoCubeLocation);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.places, R.layout.spinner);
        spinner.setAdapter(adapter);

        spinner = view.findViewById(R.id.firstAutoCubeOrientation);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.orientations, R.layout.spinner);
        spinner.setAdapter(adapter);

        spinner = view.findViewById(R.id.secondAutoCubeOrientation);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.orientations, R.layout.spinner);
        spinner.setAdapter(adapter);

        spinner = view.findViewById(R.id.thirdAutoCubeOrientation);
        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.orientations, R.layout.spinner);
        spinner.setAdapter(adapter);


        return view;

    }
}
