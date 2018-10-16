package ca.lakeeffect.scoutingapp;

import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ajay on 02/10/2017.
 *
 * This thread runs once the client is connected to the sever, it waits for the server to ask for the data
 */

public class ConnectionThread implements Runnable {

    MainActivity mainActivity;

    BluetoothSocket bluetoothSocket;
    BluetoothServerSocket bss;

    OutputStream out = null;
    InputStream in = null;

    ArrayList<String> sentPendingMessages = new ArrayList<>();

    public ConnectionThread(MainActivity mainActivity, BluetoothSocket bluetoothSocket, OutputStream out, InputStream in, BluetoothServerSocket bss){
        this.mainActivity = mainActivity;
        this.bluetoothSocket = bluetoothSocket;
        this.out = out;
        this.in = in;
        this.bss = bss;
    }

    @Override
    public void run() {

        //used if the full message is not sent
        String data = "";

        while(out != null && in != null && bluetoothSocket.isConnected()){
            try {
                byte[] bytes = new byte[100000];
                int amount = in.read(bytes);

                //if some bytes were sent, then we received something, then cut out the unused bytes (bytes array is very big because it must be the MAXIMUM amount of data you are willing to receive
                if(amount > 0)  bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
                else continue;

                String message = data + new String(bytes, Charset.forName("UTF-8"));

                //message has not been fully sent, add to data and continue
                if (!message.endsWith("END")) {
                    data += message;
                    continue;
                }

                //data has been fully sent, removed "END" from it
                message = message.substring(0, message.length() - 3);

                if (message.contains("SEND SCHEDULE")) { //received data about the schedule
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainActivity, "Received schedule",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    loadSchedule(message);
                } else if (message.contains("REQUEST DATA")) { //received a request
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainActivity, "Sending Data",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    sendData();
                } else if (message.contains("REQUEST LABELS")) { //received a request
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainActivity, "Sending Labels",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    sendLabels();
                } else if (message.contains("RECEIVED")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    in.close();
                    out.close();
                    bluetoothSocket.close();
                    bss.close();
                    deleteData();
                    mainActivity.listenerThread.run();
                    new Thread(mainActivity.listenerThread).start();
                    break;
                }

                //message has been fully sent and dealt with, reset data
                data = "";

            } catch (IOException e) {
                e.printStackTrace();
                mainActivity.listenerThread = new ListenerThread(mainActivity);
                new Thread(mainActivity.listenerThread).start();
                break;
            }


        }

    }

    public void loadSchedule(String schedule) {
        String[] userSchedules = schedule.split(":::")[1].split("::");
        String matchSchedule = schedule.split(":::")[2];

        String[] matches = matchSchedule.split("::");
        System.out.println(schedule + " malen");

        //reset schedules
        mainActivity.schedules = new ArrayList<>();

        //go through the user schedule and assign robots based on the match schedule
        for (int userID = 0; userID < userSchedules.length; userID++) {
            String name = userSchedules[userID].split(":")[0];
            String[] userSchedule = userSchedules[userID].split(":")[1].split(",");

            UserData currentUserData = new UserData(userID, name);

            mainActivity.schedules.add(currentUserData);

            for (int matchNum = 0; matchNum < userSchedule.length; matchNum++) {
                System.out.println(matches[matchNum] + " ma: " + matchNum);
                String[] robotNumbers = matches[matchNum].split(",");

                int robotIndex = Integer.parseInt(userSchedule[matchNum]);
                currentUserData.robots.add(Integer.parseInt(robotNumbers[robotIndex]));
                currentUserData.alliances.add(robotIndex >= 3);
            }
        }
    }

    public void sendLabels(){
        try {
            System.out.println(mainActivity.labels + " sadsadsad");
            this.out.write((mainActivity.versionCode + ":::" + mainActivity.labels).getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(){
        try {
            String fullMessage = mainActivity.versionCode + ":::";
            for(String message : mainActivity.pendingMessages){
                if(!fullMessage.equals(mainActivity.versionCode + ":::")){
                    fullMessage += "::";
                }
                fullMessage += message;

                sentPendingMessages.add(message);
            }

            if(mainActivity.pendingMessages.isEmpty()){
                fullMessage += "nodata::end";
            }

            this.out.write((fullMessage + "\n").getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteData(){ //deleted items that are in sent pending messages (because they now have been sent)

        SharedPreferences prefs2 = mainActivity.getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        if( prefs2.getInt("messageAmount", 0) - sentPendingMessages.size() >= 0){
            editor2.putInt("messageAmount", prefs2.getInt("messageAmount", 0) - sentPendingMessages.size());
        } else {
            editor2.putInt("messageAmount", 0);
        }
        editor2.apply();

        for(String message: new ArrayList<>(sentPendingMessages)){
            mainActivity.pendingMessages.remove(message);
            sentPendingMessages.remove(message);

            int loc = mainActivity.getLocationInSharedMessages(message);

            if(loc != -1){
                SharedPreferences prefs = mainActivity.getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("message"+loc, null);
                editor.apply();
            }
        }

        //set pending messages number on ui
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) ((RelativeLayout) mainActivity.findViewById(R.id.numberOfPendingMessagesLayout)).findViewById(R.id.numberOfPendingMessages)).setText(mainActivity.pendingMessages.size() + "");
            }
        });
    }
}
