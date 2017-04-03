package ca.lakeeffect.scoutingapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EndgamePage extends Fragment implements View.OnClickListener{

    Button submit;

    public EndgamePage() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState){
        View view = inflator.inflate(R.layout.endgamepage, container, false);

        submit = (Button) view.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        view.setTag("page3");

        return view;
    }

    public void onClick(View v){
//        Toast.makeText(getActivity(),
//                getActivity().toString(), Toast.LENGTH_LONG).show();
        if(v==submit){
            new AlertDialog.Builder(getActivity())
                .setTitle("Submiting")
                .setMessage("Are you sure you would like to submit?")
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),
                                "Keep scouting then...", Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton(android.R.string.yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(((MainActivity)getActivity()).saveData()) {
                            Toast.makeText(getActivity(),
                                    "Saving", Toast.LENGTH_LONG).show();
                            ((MainActivity) getActivity()).reset();
                        }

                    }
                })
                .create()
                .show();
        }
    }

//    public void saveData(){
//
//        File sdCard = Environment.getExternalStorageDirectory();
////        File dir = new File (sdCard.getPath() + "/ScoutingData/");
//
//        getView().findViewById(R.id.header0);
//
//
//        try {
//            file.getParentFile().mkdirs();
//            if(!file.exists()){
//                file.createNewFile();
//            }
//
//            FileOutputStream f = new FileOutputStream(file, true);
//
//            OutputStreamWriter out = new OutputStreamWriter(f);
//
//            DateFormat dateFormat = new SimpleDateFormat("dd HH mm ss");
//            Date date = new Date();
//
//            out.append("\n" + "start " + round + " " + dateFormat.format(date) + "\n");
//
//            for(Counter counter: counters){
//                out.append("counter " + getResources().getResourceEntryName(counter.getId()) + " " + counter.count + " " + counter.times.toString() + "\n");
//            }
//
//            for(CheckBox checkbox: checkboxes){
//                out.append("checkbox " + getResources().getResourceEntryName(checkbox.getId()) + " " + checkbox.isChecked() + "\n");
//            }
//
//            for(RadioGroup radiogroup: radiogroups){
//                out.append("radiogroup " + getResources().getResourceEntryName(radiogroup.getId()) + " " + radiogroup.indexOfChild(findViewById(radiogroup.getCheckedRadioButtonId())) + "\n");
//            }
//
////            TODO: Write button data, might not be needed
////            for(Button button: buttons){
////                out.append("button " + getResources().getResourceEntryName(button.getId()) + " " + counter.count + "\n");
////            }
//
//            for(SeekBar seekbar: seekbars){
//                out.append("seekbar " + getResources().getResourceEntryName(seekbar.getId()) + " " + seekbar.getProgress() + "\n");
//            }
//
//
//            out.append("end");
//            out.close();
//
//            f.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
}


