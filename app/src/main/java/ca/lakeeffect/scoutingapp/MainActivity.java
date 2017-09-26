package ca.lakeeffect.scoutingapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    //TODO: Redo text sizes

    List<Counter> counters = new ArrayList<>();
    List<CheckBox> checkboxes = new ArrayList<>();
    List<RadioGroup> radiogroups = new ArrayList<>();
    List<Button> buttons = new ArrayList<>();
    List<SeekBar> seekbars = new ArrayList<>();

//    Button submit;

    TextView timer;
    TextView robotNumText; //robotnum and round

    int robotNum = 2708;
    int round = 0;
    String scoutName = "Woodie Flowers";

    static long start;

    InputPagerAdapter pagerAdapter;
    ViewPager viewPager;

    BluetoothSocket bluetoothsocket;
    ArrayList<String> pendingmessages = new ArrayList<>();
    boolean connected;

    Button moreOptions;

    Thread bluetoothConnectionThread;

    ListenerThread listenerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs1 = getSharedPreferences("theme", MODE_PRIVATE);
        switch(prefs1.getInt("theme", 0)){
            case 0:
                setTheme(R.style.AppTheme);
                break;
            case 1:
                setTheme(R.style.AppThemeLight);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alert();
        //add all buttons and counters etc.

        SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
        for(int i=0;i<prefs.getInt("messageAmount",0);i++){
            if(prefs.getString("message"+i,null) == null){
                SharedPreferences.Editor editor = prefs.edit();
                for(int s=i;s<prefs.getInt("messageAmount",0)-1;s++) {
                    editor.putString("message" + s, prefs.getString("message" + (s+1), ""));
                }
                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)-1);
                editor.commit();
            }else {
                pendingmessages.add(prefs.getString("message" + i, ""));
            }
        }

        moreOptions = (Button) findViewById(R.id.moreOptions);
        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(MainActivity.this, v, Gravity.CENTER_HORIZONTAL);
                menu.getMenuInflater().inflate(R.menu.more_options, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.reset){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Confirm")
                                    .setMessage("Continuing will reset current data.")
                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int which){
                                            reset();

                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create()
                                    .show();
                        }
                        if(item.getItemId() == R.id.changeNum){
                            alert();
                        }

                        if(item.getItemId() == R.id.changeTheme) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Confirm")
                                    .setMessage("Continuing will reset current data.")
                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int which){
                                            Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create()
                                    .show();
                        }
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                menu.show();
            }
        });

//        counters.add((Counter) findViewById(R.id.goalsCounter));

        //setup scrolling viewpager
        viewPager = (ViewPager) findViewById(R.id.scrollingview);
        pagerAdapter = new InputPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);


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

        //start bluetooth pairing/connection
//        Thread thread = new Thread(new PairingThread(this, true));
//        thread.start();

        //start listening
        Thread thread = new Thread(new ListenerThread(bluetoothsocket));
        thread.start();


        start = System.nanoTime();
    }


    public void registerBluetoothListeners(){
        BroadcastReceiver bState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("SDAsadsadsadsad","iouweroiurweoiurewoirweuoiweru");
                String action = intent.getAction();
                switch (action){
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        connected = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) ((RelativeLayout) findViewById(R.id.statusLayout)).findViewById(R.id.status)).setText("DISCONNECTED");
                                ((TextView) ((RelativeLayout) findViewById(R.id.statusLayout)).findViewById(R.id.status)).setTextColor(Color.argb(255,255,0,0));
                            }
                        });
//                        if(bluetoothConnectionThread == null) setupBluetoothConnections();
                        Thread thread1 = new Thread(bluetoothConnectionThread);
                        thread1.start();
                        break;
//                    case BluetoothDevice.ACTION_ACL_CONNECTED:
//                        try {
//                            out = bluetoothsocket.getOutputStream();
//                            in = bluetoothsocket.getInputStream();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ((TextView) findViewById(R.id.status)).setText("CONNECTED");
//                                    ((TextView) findViewById(R.id.status)).setTextColor(Color.argb(255,0,255,0));
//                                    Toast.makeText(MainActivity.this, "connected!",
//                                            Toast.LENGTH_LONG).show();
//                                }
//                            });
//                            while(!pendingmessages.isEmpty()){
//                                for(String message: pendingmessages){
//
//                                    byte[] bytes = new byte[1000000];
//                                    int amount = in.read(bytes);
//                                    if(amount>0)  bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
//                                    else continue;
//                                    if(new String(bytes, Charset.forName("UTF-8")).equals("done")){
//                                        pendingmessages.remove(message);
//                                        break;
//                                    }
//                                }//TODO TEST IF THIS WORKS
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bState,filter);
    }

    public String[] getData(){
        if(((RatingBar) pagerAdapter.teleopPage.getView().findViewById(R.id.driveRating)).getRating() <= 0){
            runOnUiThread(new Thread(){
                public void run(){
                    new Toast(MainActivity.this).makeText(MainActivity.this, "You didn't rate the drive ability!", Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
        if(((RadioGroup) pagerAdapter.autoPage.getView().findViewById(R.id.autoBaselineGroup)).getCheckedRadioButtonId() <= 0){
            runOnUiThread(new Thread(){
                public void run(){
                    new Toast(MainActivity.this).makeText(MainActivity.this, "You forgot to specify if it crossed the baseline! Go back to the teleop page!", Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }if(((RadioGroup) pagerAdapter.endgamePage.getView().findViewById(R.id.endgameClimbGroup)).getCheckedRadioButtonId() <= 0){
            runOnUiThread(new Thread(){
                public void run(){
                    new Toast(MainActivity.this).makeText(MainActivity.this, "You forgot to specify if it climbed!", Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }

        final StringBuilder data = new StringBuilder();

        //The Labels
        final StringBuilder labels = new StringBuilder();
        labels.append("Date and Time Of Match,Round,");

        DateFormat dateFormat = new SimpleDateFormat("dd HH:mm:ss");
        Date date = new Date();

        data.append("\n" + dateFormat.format(date));

        data.append("," + round);

        TableLayout layout = (TableLayout) pagerAdapter.autoPage.getView().findViewById(R.id.autopagetablelayout);
        //            PercentRelativeLayout layout = (PercentRelativeLayout) findViewById(R.layout.autopage);
//            data.append("auto");

        StringBuilder extradata = new StringBuilder();
        StringBuilder extralabels = new StringBuilder();

        String[] autodata = new String[6];
        String[] autolabels = new String[6];
        int recordedData = 0;
        for(int i=0;i<layout.getChildCount();i++){
            for(int s = 0; s<((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof RadioGroup) {
                    int pressed = -1;
                    for(int r=0;r<((RadioGroup) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getChildCount();r++){
                        if(((RadioButton) ((RadioGroup) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getChildAt(r)).isChecked()){
                            pressed = 1-r;
                        }
                    }
                    autodata[4] = "," + pressed;
                    autolabels[4] = getResources().getResourceEntryName(((RadioGroup) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getId()) + ",";
//                        data.append("," + pressed);
//                        labels.append(getResources().getResourceEntryName(((RadioGroup) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getId()) + ",");
                    recordedData++;
                }
                else if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof Counter) {
                    String currentdata = "," + String.valueOf(((Counter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).count);
                    String currentlabel = getResources().getResourceEntryName(((Counter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getId()) + ",";
                    switch(recordedData) {
                        case 1:
                            autodata[2] = currentdata;
                            autolabels[2] = currentlabel;
                            break;
                        case 2:
                            autodata[3] = currentdata;
                            autolabels[3] = currentlabel;
                            break;
                        case 3:
                            autodata[0] = currentdata;
                            autolabels[0] = currentlabel;
                            break;
                        case 4:
                            autodata[1] = currentdata;
                            autolabels[1] = currentlabel;
                            break;

                    }
                    recordedData++;
//                        data.append();
//                        labels.append();

                }
            }
        }
        //AUTO GEAR
//            labels.append("autoGear,");
//            data.append(","+(((Spinner)pagerAdapter.autoPage.getView().findViewById(R.id.autoPeg)).getSelectedItemPosition()-1));
        autolabels[5] = "autoGear,";
        boolean autoGearSimpleData = (((Spinner)pagerAdapter.autoPage.getView().findViewById(R.id.autoPeg)).getSelectedItemPosition()-1) >= 1;
        autodata[5] = "," + (autoGearSimpleData ? 1 : 0);

        extralabels.append("autoGearPlacement,");
        extradata.append(","+(((Spinner)pagerAdapter.autoPage.getView().findViewById(R.id.autoPeg)).getSelectedItemPosition()-1));

        for(int i=0;i<autodata.length;i++){
            data.append(autodata[i]);
        }
        for(int i=0;i<autolabels.length;i++){
            labels.append(autolabels[i]);
        }

        DisplayMetrics m = getResources().getDisplayMetrics();
        PercentRelativeLayout v = null;
        if(m.widthPixels/m.density < 600) v = ((PercentRelativeLayout) ((ScrollView) pagerAdapter.teleopPage.getView()).getChildAt(0));
        else v = ((PercentRelativeLayout) pagerAdapter.teleopPage.getView());

        layout = (TableLayout) v.findViewById(R.id.teleoptablelayout);
//            data.append("\nteleop");
        String[] teledata = new String[6];
        String[] telelabels = new String[6];
        recordedData = 0;
        for(int i=0;i<layout.getChildCount();i++) {
            for (int s = 0; s < ((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof Counter) {
                    String currentdata = ("," + String.valueOf(((Counter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).count));
                    String currentlabel = (getResources().getResourceEntryName(((Counter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getId()) + ",");
                    if(recordedData == 6){
                        extradata.append(currentdata);
                        extralabels.append(currentlabel);
                    }else {
                        teledata[recordedData] = (currentdata);
                        telelabels[recordedData] = (currentlabel);
                    }
                    recordedData++;
                }
                if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof HigherCounter) {
                    String currentdata = "," + String.valueOf(((HigherCounter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).count);
                    String currentlabel = getResources().getResourceEntryName(((HigherCounter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).getId()) + ",";
                    switch(recordedData) {
                        case 0:
                            teledata[2] = currentdata;
                            telelabels[2] = currentlabel;
                            break;
                        case 1:
                            teledata[3] = currentdata;
                            telelabels[3] = currentlabel;
                            break;
                        case 2:
                            teledata[0] = currentdata;
                            telelabels[0] = currentlabel;
                            break;
                        case 3:
                            teledata[1] = currentdata;
                            telelabels[1] = currentlabel;
                            break;

                    }
                    recordedData++;
                }
            }
        }
        extradata.append(","+((RatingBar) pagerAdapter.teleopPage.getView().findViewById(R.id.driveRating)).getRating());
        extralabels.append("Drive Rating,");

        for(int i=0;i<teledata.length;i++){
            data.append(teledata[i]);
        }
        for(int i=0;i<autolabels.length;i++){
            labels.append(telelabels[i]);
        }

        v = ((PercentRelativeLayout) ((ScrollView) pagerAdapter.endgamePage.getView()).getChildAt(0));
        for(int i=0; i<v.getChildCount(); i++){
            if(v.getChildAt(i) instanceof RadioGroup){
                int pressed = -1;
                for(int r=0;r<((RadioGroup) v.getChildAt(i)).getChildCount();r++){
                    if(((RadioButton) ((RadioGroup) v.getChildAt(i)).getChildAt(r)).isChecked()){
                        pressed = r;
                    }
                }
                data.append("," + (pressed == 0 ? 1: 0));
                labels.append("Did Climb,");
                extradata.append("," + pressed);
                extralabels.append("ClimbExtraData (Includes no attempt),");
            }
            if(v.getChildAt(i) instanceof EditText){
                extradata.append(",\"" + ((EditText) v.getChildAt(i)).getText().toString().replace("\"", "\'").replace(":", ";").replace("\n", "\t") + "\"");
                extralabels.append(getResources().getResourceEntryName(((EditText) v.getChildAt(i)).getId()) + ",");
            }
            if(v.getChildAt(i) instanceof Counter){
                extradata.append("," + ((Counter) v.getChildAt(i)).count);
                extralabels.append(getResources().getResourceEntryName(((Counter) v.getChildAt(i)).getId()) + ",");
            }

            if(v.getChildAt(i) instanceof CheckBox){
                String currentlabel = getResources().getResourceEntryName(((CheckBox) v.getChildAt(i)).getId()) + ",";
                String currentdata = "," + (((CheckBox) v.getChildAt(i)).isChecked() ? 1 : 0);
                if((currentlabel).equals("died,")){
                    labels.append(currentlabel);
                    data.append(currentdata);
                }else {
                    extralabels.append(currentlabel);
                    extradata.append(currentdata);
                }
            }
        }

        //add extra data and labels
        labels.append(extralabels);
        data.append(extradata);

        labels.append("Scout,");
        data.append(","+scoutName);

        data.append(",end");//make sure full message has been sent
        labels.append("placeholder finish");

        return new String[]{data.toString(), labels.toString()};
    }

    public boolean saveData(){
        File sdCard = Environment.getExternalStorageDirectory();
//        File dir = new File (sdCard.getPath() + "/ScoutingData/");

        File file = new File(sdCard.getPath() + "/#ScoutingData/" + robotNum + ".csv");

        try{

            boolean newfile = false;
            file.getParentFile().mkdirs();
            if(!file.exists()) {
                file.createNewFile();
                newfile = true;
            }

            FileOutputStream f = new FileOutputStream(file, true);

            OutputStreamWriter out = new OutputStreamWriter(f);

            String[] data = getData();

            if(newfile) out.append(data[1].toString());
            out.append(data[0].toString());
            out.close();

            f.close();

//            Thread thread = new Thread(){
//                public void run(){
//                    while(true) {
//                        System.out.println("aaaa");
//                        byte[] bytes = new byte[1000];
//                        try {
//                            if(!connected){
//                                pendingmessages.add(robotNum + ":" + labels.toString() + ":"  + data.toString());
//                                SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
//                                SharedPreferences.Editor editor = prefs.edit();
//                                editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + labels.toString() + ":"  + data.toString());
//                                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
//                                editor.apply();
//                                return;
//                            }
//                            int amount = in.read(bytes);
//                            if (new String(bytes, Charset.forName("UTF-8")).equals("done")) {
//                                return;
//                            }
//                            if(!connected){
//                                pendingmessages.add(robotNum + ":" + labels.toString() + ":"  + data.toString());
//                                SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
//                                SharedPreferences.Editor editor = prefs.edit();
//                                editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + labels.toString() + ":"  + data.toString());
//                                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
//                                editor.apply();
//                                return;
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//
//            if(bluetoothsocket != null && bluetoothsocket.isConnected()){
//                System.out.println("aaaa");
//                this.out.write((robotNum + ":" + labels.toString() + ":" + data.toString()).getBytes(Charset.forName("UTF-8")));
//                thread.start();
//            }else{
//                pendingmessages.add(robotNum + ":" + labels.toString() + ":"  + data.toString());
//                SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + labels.toString() + ":"  + data.toString());
//                editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
//                editor.apply();
//            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void waitForConformation(final StringBuilder labels, final StringBuilder data){
        Thread thread = new Thread(){
            public void run(){
                while(true) {
                    System.out.println("aaaa");
                    byte[] bytes = new byte[1000];
                    try {
                        if(!connected){
                            pendingmessages.add(robotNum + ":" + labels.toString() + ":"  + data.toString());
                            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + labels.toString() + ":"  + data.toString());
                            editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
                            editor.apply();
                            return;
                        }
                        int amount = listenerThread.in.read(bytes);
                        if (new String(bytes, Charset.forName("UTF-8")).equals("done")) {
                            return;
                        }
                        if(!connected){
                            pendingmessages.add(robotNum + ":" + labels.toString() + ":"  + data.toString());
                            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + labels.toString() + ":"  + data.toString());
                            editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
                            editor.apply();
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public int getLocationInSharedMessages(String message){
        SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
        for(int i=0;i<prefs.getInt("messageAmount",0);i++) {
            if(prefs.getString("message" + i, "").equals(message)){
                return i;
            }
        }
        return -1;
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
            }
        }
    }

    @Override
    public void onBackPressed(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            moreOptions.callOnClick();
        }
        return;
    }

    public void reset(){
        //setup scrolling viewpager
        alert();

//        viewPager = (ViewPager) findViewById(R.id.scrollingview);
        viewPager.setAdapter(pagerAdapter);
//        viewPager.setOffscreenPageLimit(3);
//        viewPager.getAdapter().notifyDataSetChanged();

        ((RadioGroup) pagerAdapter.autoPage.getView().findViewById(R.id.autoBaselineGroup)).clearCheck();
//        ((RadioGroup) pagerAdapter.autoPage.getView().findViewById(R.id.autoGearGroup)).clearCheck();
        ((RadioGroup) pagerAdapter.endgamePage.getView().findViewById(R.id.endgameClimbGroup)).clearCheck();
        ((EditText) pagerAdapter.endgamePage.getView().findViewById(R.id.endgameComments)).setText("");
        ((CheckBox) pagerAdapter.endgamePage.getView().findViewById(R.id.defense)).setChecked(false);
        ((CheckBox) pagerAdapter.endgamePage.getView().findViewById(R.id.died)).setChecked(false);
        ((RatingBar) pagerAdapter.teleopPage.getView().findViewById(R.id.driveRating)).setRating(0);
//        ((SeekBar) pagerAdapter.endgamePage.getView().findViewById(R.id.rotors)).setProgress(0);
        ((Spinner) pagerAdapter.autoPage.getView().findViewById(R.id.autoPeg)).setSelection(1);

    }

    public void alert(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog)
                .setTitle("Enter Info")
                .setPositiveButton(android.R.string.yes,  null)
                .setCancelable(false)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
             @Override
             public void onShow(final DialogInterface dialog) {
                 SharedPreferences prefs = getSharedPreferences("scoutName", MODE_PRIVATE);
                 ((EditText) ((AlertDialog) dialog).findViewById(R.id.editText3)).setText(prefs.getString("scoutName", ""));
                 ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                         LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog, null);
                         EditText robotNumin = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText);
                         EditText roundin = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText2);
                         EditText scoutNamein = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText3);
                         try {
                             robotNum = Integer.parseInt(robotNumin.getText().toString());
                             round = Integer.parseInt(roundin.getText().toString());
                             scoutName = scoutNamein.getText().toString();

                             SharedPreferences prefs = getSharedPreferences("scoutName", MODE_PRIVATE);
                             SharedPreferences.Editor editor = prefs.edit();
                             editor.putString("scoutName", scoutName);
                             editor.apply();

                             if(round > 99){
                                 runOnUiThread(new Runnable() {
                                     @Override
                                     public void run() {
                                         Toast.makeText(MainActivity.this, "Invalid Match Number",
                                                 Toast.LENGTH_LONG).show();
                                     }
                                 });
                                 return;
                             }

                             if(scoutName.equals("")){
                                 runOnUiThread(new Runnable() {
                                     @Override
                                     public void run() {
                                         Toast.makeText(MainActivity.this, "Invalid Scout Name",
                                                 Toast.LENGTH_LONG).show();
                                     }
                                 });
                                 return;
                             }

                         } catch (NumberFormatException e) {
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     Toast.makeText(MainActivity.this, "Invalid Data! Are any fields blank?",
                                             Toast.LENGTH_LONG).show();
                                 }
                             });
                             return;
                         }
                         robotNumText = (TextView) findViewById(R.id.robotNum);
                         robotNumText.setText("Robot: " + robotNum + " " + "Round: " + round);
                         dialog.dismiss();
                     }
                 });

             }
         });
        dialog.show();
    }


}
