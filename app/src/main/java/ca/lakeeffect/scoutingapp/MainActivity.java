package ca.lakeeffect.scoutingapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    List<Counter> counters = new ArrayList<>();
    List<CheckBox> checkboxes = new ArrayList<>();
    List<RadioGroup> radiogroups = new ArrayList<>();
    List<Button> buttons = new ArrayList<>();
    List<SeekBar> seekbars = new ArrayList<>();

//    Button submit;

    TextView timer;
    TextView robotNumText;//robotnum and round

    int robotNum = 2708;
    int round = 2;

    static long start;

    FragmentPagerAdapter pagerAdapter;
    ViewPager viewPager;

    BluetoothSocket bluetoothsocket;
    OutputStream out;
    InputStream in;

    boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add all buttons and counters etc.

//        counters.add((Counter) findViewById(R.id.goalsCounter));

        //setup scrolling viewpager
        viewPager = (ViewPager) findViewById(R.id.scrollingview);
        pagerAdapter = new InputPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

//        NumberPicker np = (NumberPicker) findViewm counters
//        np.setWrapSelectorWheel(false);ById(R.id.numberPicker);
//
//        np.setMinValue(0);
//        np.setMaxValue(20);    //maybe switch fro
//        np.setValue(0);

        //add onClickListeners

//        checkboxes.add((CheckBox) findViewById(R.id.scaleCheckBox));

//        submit = (Button) findViewById(R.id.submitButton);

//        timer = (TextView) findViewById(R.id.timer);
        robotNumText = (TextView) findViewById(R.id.robotNum);

        robotNumText.setText("Round: " + round + "  Robot: " + robotNum);

//        submit.setOnClickListener(this);

        //Ask for permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);

        //bluetooth stuff

        Thread thread = new Thread(){
            public void run(){
                try {
                    BluetoothServerSocket bss = ba.listenUsingRfcommWithServiceRecord("DialUpInternet", UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));
                    bluetoothsocket = bss.accept();
                    out = bluetoothsocket.getOutputStream();
                    in = bluetoothsocket.getInputStream();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "connected!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    connected = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        start = System.nanoTime();
    }

    @Override
    public void onClick(View v) {
//        if(v == submit){
//            saveData();
//        }
    }

    public void saveData(){
//        PercentRelativeLayout layout = (PercentRelativeLayout) viewPager.findViewWithTag("page1");
//        for(int i=0;i<layout.getChildCount();i++){
//            if(layout.getChildAt(i) instanceof CheckBox) {
//                try {
//                    out.write(Byte.valueOf("," + String.valueOf( ( (CheckBox) layout.getChildAt(i)).isChecked())));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return;

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

            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableLayout layout = (TableLayout) inflater.inflate(R.layout.autopage, null).findViewById(R.id.autopagetablelayout);
//            PercentRelativeLayout layout = (PercentRelativeLayout) findViewById(R.layout.autopage);
            Log.d("iodailjasdl",String.valueOf(layout==null));
            out.append("auto");
            for(int i=0;i<layout.getChildCount();i++){
                for(int s = 0; s<((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                    Log.d("iodailjasdl", "loop working");
                    if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof CheckBox) {
                        out.append("," + String.valueOf(((CheckBox) ((TableRow) layout.getChildAt(i)).getChildAt(s)).isChecked()));
                    }
                }
            }
//
////            for(CheckBox checkbox: checkboxes){
////                out.append("checkbox " + getResources().getResourceEntryName(checkbox.getId()) + " " + checkbox.isChecked() + "\n");
////            }
////
////            for(RadioGroup radiogroup: radiogroups){
////                out.append("radiogroup " + getResources().getResourceEntryName(radiogroup.getId()) + " " + radiogroup.indexOfChild(findViewById(radiogroup.getCheckedRadioButtonId())) + "\n");
////            }
////
//////            TODO: Write button data, might not be needed
//////            for(Button button: buttons){
//////                out.append("button " + getResources().getResourceEntryName(button.getId()) + " " + counter.count + "\n");
//////            }
////
////            for(SeekBar seekbar: seekbars){
////                out.append("seekbar " + getResources().getResourceEntryName(seekbar.getId()) + " " + seekbar.getProgress() + "\n");
////            }
////
////
////            out.append("end");
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
