package ca.lakeeffect.scoutingapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class Timer extends LinearLayout implements View.OnClickListener{

    long timeClicked = -1;

    Button storeTime;

    public Timer(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.timer, this);

        storeTime = (Button) findViewById(R.id.plus5Button);
        storeTime.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v == storeTime){
            timeClicked = System.currentTimeMillis();
        }
    }
}
