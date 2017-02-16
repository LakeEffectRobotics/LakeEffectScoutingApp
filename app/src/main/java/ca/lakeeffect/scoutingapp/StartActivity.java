package ca.lakeeffect.scoutingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    }

    @Override
    public void onClick(View v) {
        if(v == startScouting){

        }else if(v == changeTheme){
            String[] themes = {"Dark","Light"};
            new AlertDialog.Builder(this)
            .setTitle("Which theme?")
            .setMultiChoiceItems(themes, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                public void onClick(DialogInterface dialog, final int which, boolean isChecked) {
                    switch(which){
                        case 0:
                            SharedPreferences prefs = getSharedPreferences("theme", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("theme", 0);
                            editor.apply();
                            break;
                        case 1:
                            prefs = getSharedPreferences("theme", MODE_PRIVATE);
                            editor = prefs.edit();
                            editor.putInt("theme", 0);
                            editor.apply();
                            break;
                    }
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();

        }
    }
}
