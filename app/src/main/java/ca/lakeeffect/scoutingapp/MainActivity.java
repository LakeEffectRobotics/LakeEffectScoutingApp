package ca.lakeeffect.scoutingapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    ArrayList<String> pendingmessages = new ArrayList<>();
    boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new AlertDialog.Builder(this)
                .setView(R.layout.dialog)
                .setTitle("Enter Info")
                .setPositiveButton(android.R.string.yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog, null);
                        EditText robotNumin = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText);
                        EditText roundin = (EditText) ((AlertDialog) dialog).findViewById(R.id.editText2);
                        robotNum = Integer.parseInt(robotNumin.getText().toString());
                        round = Integer.parseInt(roundin.getText().toString());
                        robotNumText = (TextView) findViewById(R.id.robotNum);
                        robotNumText.setText("Round: " + round + "  Robot: " + robotNum);
                    }
                })
                .create()
                .show();
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
        try {
            final BluetoothServerSocket bss = ba.listenUsingRfcommWithServiceRecord("DialUpInternet", UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));

            final Thread thread = new Thread(){
                public void run(){
                    try {
                        Log.d("Uh Oh", "CONNECTINGJADLKJASDKLJ");
                        bluetoothsocket = bss.accept();
                        out = bluetoothsocket.getOutputStream();
                        in = bluetoothsocket.getInputStream();
                        connected = true;
                        Log.d("Uh Oh", "CONNECTED");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.status)).setText("CONNECTED");
                                ((TextView) findViewById(R.id.status)).setTextColor(Color.argb(255,0,255,0));
                                Toast.makeText(MainActivity.this, "connected!",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        while(!pendingmessages.isEmpty()){
                            for(String message: pendingmessages){
                                out.write(message.getBytes(Charset.forName("UTF-8")));
                                byte[] bytes = new byte[1000000];
                                int amount = in.read(bytes);
                                if(amount>0)  bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
                                else continue;
                                if(new String(bytes, Charset.forName("UTF-8")).equals("done")){
                                    pendingmessages.remove(message);
                                    break;
                                }
                            }//TODO TEST IF THIS WORKS
                            Log.d("Uh Oh", "Uh oh sadjkhasdkjhasdkjhsadkadshkjsad");
                        }

    //                    while(bluetoothsocket.isConnected()){
    //                        Log.d("SDsddsdssd","fasdfdfdfsdfsdfsdfsddfsfdsfd");
    //                        try {
    //                            Thread.sleep(200);
    //                        } catch (InterruptedException e) {
    //                            e.printStackTrace();
    //                        }
    //                    }
    //                    runOnUiThread(new Runnable() {
    //                        @Override
    //                        public void run() {
    //                            ((TextView) findViewById(R.id.status)).setText("DISCONNECTED");
    //                            ((TextView) findViewById(R.id.status)).setTextColor(Color.argb(255,255,0,0));
    //                        }
    //                    });
    //                    run();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

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
                                    ((TextView) findViewById(R.id.status)).setText("DISCONNECTED");
                                    ((TextView) findViewById(R.id.status)).setTextColor(Color.argb(255,255,0,0));
                                }
                            });
                            Thread thread1 = new Thread(thread);
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
//                                    out.write(message.getBytes(Charset.forName("UTF-8")));
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
        } catch (IOException e) {
            e.printStackTrace();
        }



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

            final StringBuilder data = new StringBuilder();

            DateFormat dateFormat = new SimpleDateFormat("dd HH mm ss");
            Date date = new Date();

            data.append("\n" + "start " + round + " " + dateFormat.format(date) + "\n");

            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableLayout layout = (TableLayout) inflater.inflate(R.layout.autopage, null).findViewById(R.id.autopagetablelayout);
//            PercentRelativeLayout layout = (PercentRelativeLayout) findViewById(R.layout.autopage);
            data.append("auto");
            for(int i=0;i<layout.getChildCount();i++){
                for(int s = 0; s<((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                    if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof CheckBox) {
                        data.append("," + String.valueOf(((CheckBox) ((TableRow) layout.getChildAt(i)).getChildAt(s)).isChecked()));
                    }
                }
            }

            layout = (TableLayout) inflater.inflate(R.layout.teleoppage, null).findViewById(R.id.teleoptablelayout);
            data.append("\nteleop");
            for(int i=0;i<layout.getChildCount();i++){
                for(int s = 0; s<((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                    if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof Counter) {
                        data.append("," + String.valueOf(((Counter) ((TableRow) layout.getChildAt(i)).getChildAt(s)).count));
                    }
                }
            }

            layout = (TableLayout) inflater.inflate(R.layout.teleoppage, null).findViewById(R.id.teleoptablelayout);
            data.append("\nendgame");
            for(int i=0;i<layout.getChildCount();i++){
                for(int s = 0; s<((TableRow) layout.getChildAt(i)).getChildCount(); s++) {
                    if (((TableRow) layout.getChildAt(i)).getChildAt(s) instanceof CheckBox) {
                        data.append("," + String.valueOf(((CheckBox) ((TableRow) layout.getChildAt(i)).getChildAt(s)).isChecked()));
                    }
                }
            }

            data.append("\nend");//make sure full message has been sent

            out.append(data.toString());
            out.close();

            f.close();

            if(bluetoothsocket != null && bluetoothsocket.isConnected()) this.out.write((robotNum + ":" + data.toString()).getBytes(Charset.forName("UTF-8")));
            else pendingmessages.add(robotNum + ":" + data.toString());

            Thread thread = new Thread(){
                public void run(){
                    while(true) {
                        byte[] bytes = new byte[1000];
                        try {
                            if(!connected){
                                pendingmessages.add(robotNum + ":" + data.toString());
                                return;
                            }
                            int amount = in.read(bytes);
                            if (new String(bytes, Charset.forName("UTF-8")).equals("done")) {
                                return;
                            }
                            if(!connected){
                                pendingmessages.add(robotNum + ":" + data.toString());
                                return;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();

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
            }
        }
    }
}
