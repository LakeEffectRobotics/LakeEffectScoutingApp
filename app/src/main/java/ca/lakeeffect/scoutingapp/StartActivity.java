package ca.lakeeffect.scoutingapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class StartActivity extends ListeningActitivty implements View.OnClickListener {

    Button startScouting, moreOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("theme", MODE_PRIVATE);
        switch(prefs.getInt("theme", 0)){
            case 0:
                setTheme(R.style.AppTheme);
                break;
            case 1:
                setTheme(R.style.AppThemeLight);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        startScouting = findViewById(R.id.startScouting);
        moreOptions = findViewById(R.id.moreOptionsStartScreen);

        startScouting.setOnClickListener(this);
        moreOptions.setOnClickListener(this);

        //Ask for permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        //Set Unsent Messages Text
        TextView unsentMessages = findViewById(R.id.startUnsentMessages);
        assert unsentMessages != null;
        unsentMessages.setText("Unsent Messages: " + getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE).getInt("messageAmount", 0));

        //Set Version Text
        TextView buildNum = findViewById(R.id.buildNum);
        TextView versionNum = findViewById(R.id.versionNum);
        String buildNumText = "Build: " +  + BuildConfig.VERSION_CODE + " " + (BuildConfig.DEBUG ? "Debug" : "Release");
        //check if it is the first open on this version of the app
        SharedPreferences savedLabelsPrefs = getSharedPreferences("savedLabels", MODE_PRIVATE);
        if (savedLabelsPrefs.getInt("versionNumber", -1) != BuildConfig.VERSION_CODE) {
            //first app open on this new version
            buildNumText += " | First Open";
        }
        buildNum.setText(buildNumText);
        versionNum.setText("Version: " + BuildConfig.VERSION_NAME);

        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);

        //startup bluetooth threads
        startListenerThread();
    }

    public void startListenerThread() {
        if (savedLabels == null){
            SharedPreferences prefs = getSharedPreferences("savedLabels", MODE_PRIVATE);

            if (BuildConfig.VERSION_CODE == prefs.getInt("versionNumber", -1)) {
                savedLabels = prefs.getString("savedLabels", null);
                if (savedLabels == null) {
                    //this should really never happen, but just incase
                    return;
                }
            } else {
                //don't start a listener, the app has never been opened on this version
                return;
            }
        }

        //start listening
        if (listenerThread == null) {
            listenerThread = new ListenerThread(this);
            listenerThreadThreadClass = new Thread(listenerThread);
            listenerThreadThreadClass.start();
        }
    }

    public void stopListenerThread() {
        if (listenerThread != null) {
            if(listenerThread.connectionThreadThreadClass != null){
                try {
                    listenerThread.connectionThreadThreadClass.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else{
                if(listenerThreadThreadClass != null) {
                    try {
                        listenerThreadThreadClass.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void restartListenerThread(){
        stopListenerThread();

        startListenerThread();
    }

    @Override
    public void onClick(View v) {
        if(v == startScouting){
            //check if listener thread is currently doing something
            if (listenerThread != null && listenerThread.connected) {
                //do not start scouting
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(R.layout.dialog)
                        .setTitle("Device is currently sending data")
                        .setMessage("Please wait for the data to be fully sent." +
                                "\nIf stuck, you can try to restart the app. " +
                                "\n\nMAKE SURE someone isn't pulling from your device" +
                                " without you noticing first.")
                        .setPositiveButton("Ok", null)
                        .setCancelable(true)
                        .create();
                return;
            } else {
                //shutdown the listener thread
                //TODO: Make sure this actually stops the threads
                //A manual check might have to be inserted to break the while loop
                stopListenerThread();
            }

            //the main activity can be started
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if(v == moreOptions) {
            PopupMenu menu = new PopupMenu(this, findViewById(R.id.moreOptionsStartScreen), Gravity.CENTER_HORIZONTAL);
            menu.getMenuInflater().inflate(R.menu.more_options_start, menu.getMenu());
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.resetPendingMessages) {
                        SharedPreferences prefs = getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("messageAmount", 0);
                        editor.apply();

                        //set pending messages number on ui
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.startUnsentMessages)).setText("Unsent Data: 0");
                            }
                        });
                    }

                    if (item.getItemId() == R.id.changeTheme) {
                        String[] themes = {"Dark", "Light"};
                        new AlertDialog.Builder(StartActivity.this)
                                .setSingleChoiceItems(themes, 2, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, final int which) {
                                        Intent intent = new Intent(StartActivity.this, StartActivity.class);
                                        switch(which){
                                            case 0:
                                                SharedPreferences prefs = getSharedPreferences("theme", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putInt("theme", 0);
                                                editor.apply();
                                                dialog.dismiss();
                                                startActivity(intent);
                                                break;
                                            case 1:
                                                prefs = getSharedPreferences("theme", MODE_PRIVATE);
                                                editor = prefs.edit();
                                                editor.putInt("theme", 1);
                                                editor.apply();
                                                dialog.dismiss();
                                                startActivity(intent);
                                                break;
                                        }
                                    }
                                })

                                .setTitle("Select Theme")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setNegativeButton("Cancel", null)
                                .setCancelable(false)
                                .create()
                                .show();
                    }

                    Toast.makeText(StartActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            menu.show();
        }
    }

    public void onBackPressed(){
        return;
    }
}
