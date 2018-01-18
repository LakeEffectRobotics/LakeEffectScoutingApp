package ca.lakeeffect.scoutingapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

    InputPagerAdapter pagerAdapter;
    ViewPager viewPager;

    ArrayList<String> pendingmessages = new ArrayList<>();
    boolean connected;

    Button moreOptions;

    ListenerThread listenerThread;

    String savedLabels = null; //generated at the beginning

    int versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set version code
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //check what theme is selected and set it as the theme
        SharedPreferences prefs1 = getSharedPreferences("theme", MODE_PRIVATE);
        switch (prefs1.getInt("theme", 0)) {
            case 0:
                setTheme(R.style.AppTheme);
                break;
            case 1:
                setTheme(R.style.AppThemeLight);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //call alert (asking scout name and robot number)
        alert();

        //add all buttons and counters etc.

        //go through all saved pending messages and add them to the variable
        SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
        for (int i = 0; i < prefs.getInt("messageAmount", 0); i++) {
            if (prefs.getString("message" + i, null) == null) {
                SharedPreferences.Editor editor = prefs.edit();
                for (int s = i; s < prefs.getInt("messageAmount", 0) - 1; s++) {
                    editor.putString("message" + s, prefs.getString("message" + (s + 1), ""));
                }
                editor.putInt("messageAmount", prefs.getInt("messageAmount", 0) - 1);
                editor.commit();
            } else {
                pendingmessages.add(prefs.getString("message" + i, ""));
            }
        }

        //set onclick listener for moreOptions
        moreOptions = (Button) findViewById(R.id.moreOptions);
        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Remove this once notification testing is done
                startNotificationAlarm(getApplicationContext());
                PopupMenu menu = new PopupMenu(MainActivity.this, v, Gravity.CENTER_HORIZONTAL);
                menu.getMenuInflater().inflate(R.menu.more_options, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.reset) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Confirm")
                                    .setMessage("Continuing will reset current data.")
                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            reset();

                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create()
                                    .show();
                        }
                        if (item.getItemId() == R.id.changeNum) {
                            alert();
                        }
                        if (item.getItemId() == R.id.resetPendingMessages) {
                            for (String message : pendingmessages) {
                                pendingmessages.remove(message);

                                int loc = getLocationInSharedMessages(message);

                                if (loc != -1) {
                                    SharedPreferences prefs = getSharedPreferences("pendingmessages", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("message" + loc, null);
                                    editor.apply();
                                }
                            }

                            //set pending messages number on ui
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) ((RelativeLayout) findViewById(R.id.numberOfPendingMessagesLayout)).findViewById(R.id.numberOfPendingMessages)).setText(pendingmessages.size() + "");
                                }
                            });
                        }

                        if (item.getItemId() == R.id.changeTheme) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Confirm")
                                    .setMessage("Continuing will reset current data.")
                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
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

        //set device name
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        ((TextView) ((RelativeLayout) findViewById(R.id.deviceNameLayout)).findViewById(R.id.deviceName)).setText(ba.getName()); //if this method ends up not working refer to https://stackoverflow.com/a/6662271/1985387

        //set pending messages number on ui
        ((TextView) ((RelativeLayout) findViewById(R.id.numberOfPendingMessagesLayout)).findViewById(R.id.numberOfPendingMessages)).setText(pendingmessages.size() + "");


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

    }

    public void startListenerThread() {
        if (savedLabels == null) savedLabels = getData(true)[1];
        System.out.println(savedLabels + " labels");

        //start listening
        if (listenerThread == null) {
            listenerThread = new ListenerThread(this);
            new Thread(listenerThread).start();
        }
    }

    StringBuilder data;
    StringBuilder labels;

    public String[] getData(boolean bypassChecks) {
        if (!bypassChecks) {
            if (((RatingBar) pagerAdapter.teleopPage.getView().findViewById(R.id.driveRating)).getRating() <= 0) {
                runOnUiThread(new Thread() {
                    public void run() {
                        new Toast(MainActivity.this).makeText(MainActivity.this, "You didn't rate the drive ability!", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
            if (((RadioGroup) pagerAdapter.autoPage.getView().findViewById(R.id.autoBaselineGroup)).getCheckedRadioButtonId() <= 0) {
                runOnUiThread(new Thread() {
                    public void run() {
                        new Toast(MainActivity.this).makeText(MainActivity.this, "You forgot to specify if it crossed the baseline! Go back to the teleop page!", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
            if (((RadioGroup) pagerAdapter.endgamePage.getView().findViewById(R.id.endgameClimbGroup)).getCheckedRadioButtonId() <= 0) {
                runOnUiThread(new Thread() {
                    public void run() {
                        new Toast(MainActivity.this).makeText(MainActivity.this, "You forgot to specify if it climbed!", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
        }

        data = new StringBuilder();
        labels = new StringBuilder();

        //General Info
        data.append(robotNum + ",");
        labels.append("Robot,");

        labels.append("Date and Time Of Match,");
        DateFormat dateFormat = new SimpleDateFormat("dd HH:mm:ss");
        Date date = new Date();
        data.append(dateFormat.format(date) + ",");


        PercentRelativeLayout layout;

        //Auto page
        layout = (PercentRelativeLayout) pagerAdapter.autoPage.getView().findViewById(R.id.autoPageLayout);
        enterLayout(layout);

        //Tele page
        layout = (PercentRelativeLayout) pagerAdapter.teleopPage.getView().findViewById(R.id.telePageLayout);
        enterLayout(layout);

        //Endgame page
        layout = (PercentRelativeLayout) pagerAdapter.endgamePage.getView().findViewById(R.id.endgamePageLayout);
        enterLayout(layout);

        labels.append("Scout,\n");
        data.append(scoutName + ",\n");

        System.out.println(labels.toString());
        System.out.println(data.toString());
        String[] out = {data.toString(), labels.toString()};
        return out;
    }

    void enterLayout(ViewGroup top) {
        //Iterate over all child layouts
        for (int i = 0; i < top.getChildCount(); i++) {
            View v = top.getChildAt(i);
            //If the layout has a valid ID
            if (v.getId() > 0) {
                if (v instanceof EditText) {
                    data.append(((EditText) v).getText().toString().replace("|", "||").replace(",", "|c").replace("\n", "|n").replace("\"", "|q").replace(":", ";") + ",");
                    labels.append(getName(v) + ",");
                }
                if (v instanceof CheckBox) {
                    data.append(((CheckBox) v).isChecked() + ",");
                    labels.append(getName(v) + ",");
                }
                if (v instanceof Counter) {
                    data.append(((Counter) v).count + ",");
                    labels.append(getName(v) + ",");
                }
                if (v instanceof HigherCounter) {
                    data.append(((HigherCounter) v).count + ",");
                    labels.append(getName(v) + ",");
                }
                if (v instanceof RatingBar) {
                    data.append(((RatingBar) v).getRating() + ",");
                    labels.append(getName(v) + ",");
                }
                if (v instanceof Spinner) {
                    //TODO
                    data.append(((Spinner) v).getSelectedItem().toString() + ",");
                    System.out.println(((Spinner) v).getSelectedItem().toString() + ",");
                    labels.append(getName(v) + ",");
                }
            }
            if (v instanceof RadioGroup) {
                //Radio button ID will be result output in data
                data.append(getName(v.findViewById(((RadioGroup) v).getCheckedRadioButtonId())) + ",");
//                data.append(((RadioGroup) v).getCheckedRadioButtonId() + ",");
                labels.append(getName(v) + ",");
            }
            //If the child is a layout, enter it
            else if (v instanceof ViewGroup) {
                enterLayout((ViewGroup) v);
            }
        }
    }

    //Caps => spaces then letter
    //First letter capital

    String getName(View v) {
        if (v == null) return "NULL";
        String id = getResources().getResourceEntryName(v.getId());
        String out = id.substring(0, 1).toUpperCase() + id.substring(1);
        for (int i = 1; i < out.length(); i++) {
            if (Character.isUpperCase(out.charAt(i))) {
                System.out.println("TEST");
                out = out.substring(0, i) + " " + out.substring(i);
                i++;
            }
        }
        return out;
    }


    public boolean saveData() {
        File sdCard = Environment.getExternalStorageDirectory();
//        File dir = new File (sdCard.getPath() + "/ScoutingData/");

        File file = new File(sdCard.getPath() + "/#ScoutingData/" + robotNum + ".csv");

        try {

            boolean newfile = false;
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
                newfile = true;
            }

            FileOutputStream f = new FileOutputStream(file, true);

            OutputStreamWriter out = new OutputStreamWriter(f);

            String[] data = getData(false);
            if (data == null) {
                return false;
            }

            //save to file
            if (newfile) out.append(data[1]);
            out.append(data[0]);

            //add to pending messages
            pendingmessages.add(robotNum + ":" + data[0]);
            //add to sharedprefs
            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("message" + prefs.getInt("messageAmount", 0), robotNum + ":" + data[0]);
            editor.putInt("messageAmount", prefs.getInt("messageAmount", 0) + 1);
            editor.apply();

            //set pending messages number on ui
            ((TextView) ((RelativeLayout) findViewById(R.id.numberOfPendingMessagesLayout)).findViewById(R.id.numberOfPendingMessages)).setText(pendingmessages.size() + "");

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void waitForConformation(final StringBuilder labels, final StringBuilder data) {
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    System.out.println("aaaa");
                    byte[] bytes = new byte[1000];
                    try {
                        if (!connected) {
                            pendingmessages.add(robotNum + ":" + labels.toString() + ":" + data.toString());
                            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("message" + prefs.getInt("messageAmount", 0), robotNum + ":" + labels.toString() + ":" + data.toString());
                            editor.putInt("messageAmount", prefs.getInt("messageAmount", 0) + 1);
                            editor.apply();
                            return;
                        }
                        int amount = listenerThread.in.read(bytes);
                        if (new String(bytes, Charset.forName("UTF-8")).equals("done")) {
                            return;
                        }
                        if (!connected) {
                            pendingmessages.add(robotNum + ":" + labels.toString() + ":" + data.toString());
                            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("message" + prefs.getInt("messageAmount", 0), robotNum + ":" + labels.toString() + ":" + data.toString());
                            editor.putInt("messageAmount", prefs.getInt("messageAmount", 0) + 1);
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

    public int getLocationInSharedMessages(String message) {
        SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
        for (int i = 0; i < prefs.getInt("messageAmount", 0); i++) {
            if (prefs.getString("message" + i, "").equals(message)) {
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
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            moreOptions.callOnClick();
        }
        return;
    }

    public void reset() {
        //setup scrolling viewpager
        alert();

//        viewPager = (ViewPager) findViewById(R.id.scrollingview);
        viewPager.setAdapter(pagerAdapter);
//        viewPager.setOffscreenPageLimit(3);
//        viewPager.getAdapter().notifyDataSetChanged();

        PercentRelativeLayout layout;

        //Auto page
        layout = (PercentRelativeLayout) pagerAdapter.autoPage.getView().findViewById(R.id.autoPageLayout);
        clearData(layout);

        //Tele page
        layout = (PercentRelativeLayout) pagerAdapter.teleopPage.getView().findViewById(R.id.telePageLayout);
        clearData(layout);

        //Endgame page
        layout = (PercentRelativeLayout) pagerAdapter.endgamePage.getView().findViewById(R.id.endgamePageLayout);
        clearData(layout);
    }

    public void clearData(ViewGroup top) {
        for (int i = 0; i < top.getChildCount(); i++) {
            View v = top.getChildAt(i);
            if (v.getId() > 0) {
                if (v instanceof EditText) {
                    ((EditText) v).setText("");
                }
                if (v instanceof CheckBox) {
                    ((CheckBox) v).setChecked(false);
                }
                if (v instanceof RadioGroup) {
                    ((RadioGroup) v).clearCheck();
                }
                if (v instanceof RatingBar) {
                    ((RatingBar) v).setRating(0);
                }
                if (v instanceof Spinner) {
                    ((Spinner) v).setSelection(0);
                }
            }
            if (v instanceof ViewGroup) {
                clearData((ViewGroup) v);
            }
        }
    }


    public void alert() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog)
                .setTitle("Enter Info")
                .setPositiveButton(android.R.string.yes, null)
                .setCancelable(false)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                //start bluetooth, all views are probably ready now
                startListenerThread();

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

                            if (round > 99) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Invalid Match Number",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }

                            if (scoutName.equals("")) {
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

    public void startNotificationAlarm(Context context) {
        System.out.println("Setting alarm");
//        new PendingNotification().send(context);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent (context, PendingNotification.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
        Date date = Calendar.getInstance().getTime();
        System.out.println(date.toString());
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000, pending);
    }

}
