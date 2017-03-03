package ca.lakeeffect.scoutingapp;

import android.Manifest;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    Button startScouting, changeTheme;

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

        startScouting = (Button) findViewById(R.id.startScouting);
        changeTheme = (Button) findViewById(R.id.changeTheme);

        startScouting.setOnClickListener(this);
        changeTheme.setOnClickListener(this);

        //Ask for permissions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        //Set Version Text
        TextView versionNum = (TextView) findViewById(R.id.versionNum);
        try {
            assert versionNum != null;
            versionNum.setText("Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);

    }

    @Override
    public void onClick(View v) {
        if(v == startScouting){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else if(v == changeTheme){
            String[] themes = {"Dark", "Light"};
            new AlertDialog.Builder(this)
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
    }
    public void onBackPressed(){
        return;
    }
}
