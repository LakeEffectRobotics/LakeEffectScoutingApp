package ca.lakeeffect.scoutingapp;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HigherCounter extends LinearLayout implements View.OnClickListener{

    TextView counterText;

    int count;

    List<Long> times = new ArrayList<>();

    Button plusOneButton;
    Button minusOneButton;
    Button plus5Button;
    Button minus5Button;

    public HigherCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.higher_counter, this);

        plus5Button = (Button) findViewById(R.id.plus5Button);
        plus5Button.setOnClickListener(this);        
        plusOneButton = (Button) findViewById(R.id.plusOneButton);
        plusOneButton.setOnClickListener(this);
        minusOneButton = (Button) findViewById(R.id.minusOneButton);
        minusOneButton.setOnClickListener(this);
        minus5Button = (Button) findViewById(R.id.minus5Button);
        minus5Button.setOnClickListener(this);


        counterText = (TextView) findViewById(R.id.counterText);
    }

    @Override
    public void onClick(View view){
        times.add(new Long(System.nanoTime() - MainActivity.start));
        if(view == plusOneButton) count ++;
        else if(view == plus5Button) count +=5;
        if(view == minusOneButton) count --;
        else if(view == minus5Button) count -=5;
        if(count < 0) count = 0;
        counterText.setText(count+"");
    }

}
