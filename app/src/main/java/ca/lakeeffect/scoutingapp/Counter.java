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

public class Counter extends LinearLayout implements View.OnClickListener{
    TextView counterText;

    int count;

    List<Long> times = new ArrayList<>();

    Button plusOneButton;
    Button minusOneButton;
    public Counter(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.counter, this);

        plusOneButton = (Button) findViewById(R.id.plusOneButton);
        plusOneButton.setOnClickListener(this);
        minusOneButton = (Button) findViewById(R.id.minusOneButton);
        minusOneButton.setOnClickListener(this);


        counterText = (TextView) findViewById(R.id.counterText);
    }

    public void onClick(View view){
        times.add(new Long(System.nanoTime() - MainActivity.start));
        if(view == plusOneButton){
            count++;
        }else{
            count--;
        }
        counterText.setText(count+"");
    }

}