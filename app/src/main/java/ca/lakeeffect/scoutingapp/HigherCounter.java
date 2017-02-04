package ca.lakeeffect.scoutingapp;


import android.content.Context;
import android.content.res.TypedArray;
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
    Button plusBigButton;
    Button minusBigButton;

    int bigButton = 1;

    public HigherCounter(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.higher_counter, this);

    for(int i = 0; i < attrs.getAttributeCount(); i++){
        System.out.println(attrs.getAttributeName(i)+"\t"+attrs.getAttributeValue(i));
        if(attrs.getAttributeName(i).equals("BigButtonValue")){
            bigButton = Integer.parseInt(attrs.getAttributeValue(i));
            break;
        }
    }


        plusBigButton = (Button) findViewById(R.id.plus5Button);
        plusBigButton.setOnClickListener(this);
        plusOneButton = (Button) findViewById(R.id.plusOneButton);
        plusOneButton.setOnClickListener(this);
        minusOneButton = (Button) findViewById(R.id.minusOneButton);
        minusOneButton.setOnClickListener(this);
        minusBigButton = (Button) findViewById(R.id.minus5Button);
        minusBigButton.setOnClickListener(this);//TODO CHANGE TO 3 AND 9
        plusBigButton.setText(String.valueOf(bigButton));
        minusBigButton.setText(String.valueOf(bigButton));

        counterText = (TextView) findViewById(R.id.counterText);
    }

    @Override
    public void onClick(View view){
        times.add(new Long(System.nanoTime() - MainActivity.start));
        if(view == plusOneButton) count ++;
        else if(view == plusBigButton) count +=bigButton;
        if(view == minusOneButton) count --;
        else if(view == minusBigButton) count -=bigButton;
        if(count < 0) count = 0;
        counterText.setText(count+"");
    }


}
