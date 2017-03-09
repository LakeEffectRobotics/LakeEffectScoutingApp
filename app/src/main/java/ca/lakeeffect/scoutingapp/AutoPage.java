package ca.lakeeffect.scoutingapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Ajay on 9/25/2016.
 */
public class AutoPage extends Fragment{

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState){

        View view = inflator.inflate(R.layout.autopage, container, false);

        view.setTag("page1");

        Spinner spinner = (Spinner) view.findViewById(R.id.autoPeg);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.pegs, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(1);
//        ((TextView) view.findViewById(R.id.autoPeg)).setTextSize(15);
        return view;

    }
}
