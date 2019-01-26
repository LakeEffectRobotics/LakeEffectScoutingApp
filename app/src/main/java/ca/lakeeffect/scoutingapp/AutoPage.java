package ca.lakeeffect.scoutingapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Ajay on 9/25/2016.
 */
public class AutoPage extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        View view = inflator.inflate(R.layout.autopage, container, false);

        view.setTag("page1");

        Spinner spinner = (Spinner) view.findViewById(R.id.autoStartLocation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.startPosition, R.layout.spinner);
        spinner.setAdapter(adapter);

        final RadioButton success = (RadioButton) view.findViewById(R.id.leftHabSuccess);

        final RadioButton fail = (RadioButton) view.findViewById(R.id.leftHabFail);

        success.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    fail.setChecked(false);
                }
            }
        });

        fail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    success.setChecked(false);
                }
            }
        });

        final CheckBox cargo = (CheckBox) view.findViewById(R.id.startingObjectsCargo);

        final CheckBox hatch = (CheckBox) view.findViewById(R.id.startingObjectsHatch);

        cargo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    hatch.setChecked(false);
                }
            }
        });

        hatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    cargo.setChecked(false);
                }
            }
        });

        return view;

    }
}
