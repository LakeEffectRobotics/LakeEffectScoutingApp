package ca.lakeeffect.scoutingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    List<Counter> counters = new ArrayList<>();
    List<CheckBox> checkboxes = new ArrayList<>();
    List<RadioGroup> radiogroups = new ArrayList<>();
    List<Button> buttons = new ArrayList<>();
    List<SeekBar> seekbars = new ArrayList<>();

    Button submit;

    TextView timer;
    TextView robotNumText;//robotnum and round

    int robotNum = 0000;
    int round = 1;

    static long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add all buttons and counters etc.

        counters.add((Counter) findViewById(R.id.goalsCounter));

        checkboxes.add((CheckBox) findViewById(R.id.scaleCheckBox));

        submit = (Button) findViewById(R.id.submitButton);

        timer = (TextView) findViewById(R.id.timer);
        robotNumText = (TextView) findViewById(R.id.robotNum);

        robotNumText.setText("Round: " + round + "  Robot: " + robotNum);

//        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
//
//        np.setMinValue(0);
//        np.setMaxValue(20);    //maybe switch from counters
//        np.setWrapSelectorWheel(false);
//        np.setValue(0);

        //add onClickListeners

        submit.setOnClickListener(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        start = System.nanoTime();
    }

    @Override
    public void onClick(View v) {
        if(v == submit){
            saveData();
        }
    }

    public void saveData(){

        File sdCard = Environment.getExternalStorageDirectory();
//        File dir = new File (sdCard.getPath() + "/ScoutingData/");

        File file = new File(sdCard.getPath() + "/ScoutingData/" + robotNum + ".txt");

        try {
            file.getParentFile().mkdirs();
            if(!file.exists()){
                file.createNewFile();
            }

            FileOutputStream f = new FileOutputStream(file, true);

            OutputStreamWriter out = new OutputStreamWriter(f);

            DateFormat dateFormat = new SimpleDateFormat("dd HH mm ss");
            Date date = new Date();

            out.append("\n" + "start " + round + " " + dateFormat.format(date) + "\n");

            for(Counter counter: counters){
                out.append("counter " + getResources().getResourceEntryName(counter.getId()) + " " + counter.count + " " + counter.times.toString() + "\n");
            }

            for(CheckBox checkbox: checkboxes){
                out.append("checkbox " + getResources().getResourceEntryName(checkbox.getId()) + " " + checkbox.isChecked() + "\n");
            }

            for(RadioGroup radiogroup: radiogroups){
                out.append("radiogroup " + getResources().getResourceEntryName(radiogroup.getId()) + " " + radiogroup.indexOfChild(findViewById(radiogroup.getCheckedRadioButtonId())) + "\n");
            }

//            TODO: Write button data, might not be needed
//            for(Button button: buttons){
//                out.append("button " + getResources().getResourceEntryName(button.getId()) + " " + counter.count + "\n");
//            }

            for(SeekBar seekbar: seekbars){
                out.append("seekbar " + getResources().getResourceEntryName(seekbar.getId()) + " " + seekbar.getProgress() + "\n");
            }


            out.append("end");
            out.close();

            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    Toast.makeText(MainActivity.this, "The app has to save items to the external storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
