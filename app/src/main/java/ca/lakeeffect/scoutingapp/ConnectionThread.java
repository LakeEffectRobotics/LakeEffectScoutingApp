package ca.lakeeffect.scoutingapp;

import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
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

    final String endSplitter = "{e}";

    public ConnectionThread(MainActivity mainActivity, BluetoothSocket bluetoothSocket, OutputStream out, InputStream in, BluetoothServerSocket bss) {
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

        while (out != null && in != null && bluetoothSocket.isConnected()) {
            try {
                byte[] bytes = new byte[100000];
                int amount = in.read(bytes);

                //if some bytes were sent, then we received something, then cut out the unused bytes (bytes array is very big because it must be the MAXIMUM amount of data you are willing to receive
                if (amount > 0)
                    bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
                else continue;

                String message = data + new String(bytes, Charset.forName("UTF-8"));

                //message has not been fully sent, add to data and continue
                if (!message.endsWith(endSplitter)) {
                    data = message;
                    continue;
                }

                //data has been fully sent, removed "{e}" (the end splitter) from it
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
                    this.out.write(("RECEIVED" + endSplitter).getBytes(Charset.forName("UTF-8")));
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

        //reset schedules
        mainActivity.schedules = new ArrayList<>();

        //go through the user schedule and assign robots based on the match schedule
        for (int userID = 0; userID < userSchedules.length; userID++) {
            String name = userSchedules[userID].split(":")[0];
            String[] userSchedule = userSchedules[userID].split(":")[1].split(",");

            UserData currentUserData = new UserData(userID, name);

            mainActivity.schedules.add(currentUserData);

            for (int matchNum = 0; matchNum < userSchedule.length; matchNum++) {
                String[] robotNumbers = matches[matchNum].split(",");

                int robotIndex = Integer.parseInt(userSchedule[matchNum]);
                if (robotIndex != -1) {
                    currentUserData.robots.add(Integer.parseInt(robotNumbers[robotIndex]));
                } else {
                    currentUserData.robots.add(-1);
                }
                currentUserData.alliances.add(robotIndex >= 3);
            }
        }

        if (mainActivity.userIDSpinner != null) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //update the userIDSpinner if the alert is open
                    mainActivity.updateUserIDSpinner();

                    //update the UI with the time remaining
                    mainActivity.updateMatchesLeft();
                }
            });
        }

        //update the shared preferences
        SharedPreferences prefs = mainActivity.getSharedPreferences("userSchedule", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //set size
        editor.putInt("userAmount", mainActivity.schedules.size());

        //go through each user and add the data
        for (int i = 0; i < mainActivity.schedules.size(); i++) {
            UserData user = mainActivity.schedules.get(i);

            String robots = "";
            for (int s = 0; s < user.robots.size(); s++) {
                robots += user.robots.get(s);

                if (s < user.robots.size() - 1) {
                    robots += ",";
                }
            }

            String alliances = "";
            for (int s = 0; s < user.robots.size(); s++) {
                alliances += user.alliances.get(s);

                if (s < user.alliances.size() - 1) {
                    alliances += ",";
                }
            }

            editor.putString("robots" + i, robots);
            editor.putString("alliances" + i, alliances);
            editor.putString("userName" + i, user.userName);
        }

        editor.apply();
    }

    public void sendLabels() {
        try {
            String outString = mainActivity.versionCode + ":::" + mainActivity.savedLabels + endSplitter;
            //convert to base 64 bytes
            byte[] outBase64 = Base64.encode(outString.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT);

            this.out.write(outBase64);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData() {
        try {
            String fullMessage = mainActivity.versionCode + ":::";
            for (String message : mainActivity.pendingMessages) {
                if (!fullMessage.equals(mainActivity.versionCode + ":::")) {
                    fullMessage += "::";
                }
                fullMessage += message;

                sentPendingMessages.add(message);
            }

            if (mainActivity.pendingMessages.isEmpty()) {
                fullMessage += "nodata";
            }

            fullMessage += endSplitter;

            byte[] fullMessageBase64 = Base64.encode(fullMessage.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT);

            this.out.write(fullMessageBase64);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteData() { //deleted items that are in sent pending messages (because they now have been sent)

        SharedPreferences prefs2 = mainActivity.getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        if (prefs2.getInt("messageAmount", 0) - sentPendingMessages.size() >= 0) {
            editor2.putInt("messageAmount", prefs2.getInt("messageAmount", 0) - sentPendingMessages.size());
        } else {
            editor2.putInt("messageAmount", 0);
        }
        editor2.apply();

        for (String message : new ArrayList<>(sentPendingMessages)) {
            mainActivity.pendingMessages.remove(message);
            sentPendingMessages.remove(message);

            int loc = mainActivity.getLocationInSharedMessages(message);

            if (loc != -1) {
                SharedPreferences prefs = mainActivity.getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("message" + loc, null);
                editor.apply();
            }
        }

        //set pending messages number on ui
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) (mainActivity.findViewById(R.id.numberOfPendingMessagesLayout)).findViewById(R.id.numberOfPendingMessages)).setText(mainActivity.pendingMessages.size() + "");
            }
        });
    }
}
