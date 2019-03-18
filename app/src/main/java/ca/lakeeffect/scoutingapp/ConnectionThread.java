package ca.lakeeffect.scoutingapp;

import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Ajay on 02/10/2017.
 * 
 * This thread runs once the client is connected to the sever, it waits for the server to ask for the data
 */

public class ConnectionThread implements Runnable {

    ListeningActitivty listeningActitivty;

    BluetoothSocket bluetoothSocket;
    BluetoothServerSocket bss;

    OutputStream out = null;
    InputStream in = null;

    ArrayList<String> sentPendingMessages = new ArrayList<>();

    final String endSplitter = "{e}";

    public ConnectionThread(ListeningActitivty listeningActitivty, BluetoothSocket bluetoothSocket, OutputStream out, InputStream in, BluetoothServerSocket bss) {
        this.listeningActitivty = listeningActitivty;
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

                //decode this data from base 64 to normal data
                message = new String(Base64.decode(message, Base64.DEFAULT), Charset.forName("UTF-8"));

                if (message.contains("SEND SCHEDULE")) { //received data about the schedule
                    listeningActitivty.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(listeningActitivty, "Received schedule",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    loadSchedule(message);

                    //send that this message was received, convert to base 64 and add the end splitter first
                    this.out.write((toBase64("RECEIVED") + endSplitter).getBytes(Charset.forName("UTF-8")));
                } else if (message.contains("REQUEST DATA")) { //received a request
                    listeningActitivty.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(listeningActitivty, "Sending Data",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    sendData();
                } else if (message.contains("REQUEST LABELS")) { //received a request
                    listeningActitivty.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(listeningActitivty, "Sending Labels",
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
                    new Thread(listeningActitivty.listenerThread).start();
                    break;
                }

                //message has been fully sent and dealt with, reset data
                data = "";

            } catch (IOException e) {
                e.printStackTrace();
                listeningActitivty.listenerThread = new ListenerThread(listeningActitivty);
                new Thread(listeningActitivty.listenerThread).start();
                break;
            }


        }

    }

    public void loadSchedule(String schedule) {
        String allUserSchedules = schedule.split(":::")[1];
        allUserSchedules = new String(Base64.decode(allUserSchedules, Base64.DEFAULT), Charset.forName("UTF-8"));
        String[] userSchedules = allUserSchedules.split("::");


        String matchSchedule = schedule.split(":::")[2];
        matchSchedule = new String(Base64.decode(matchSchedule, Base64.DEFAULT), Charset.forName("UTF-8"));
        String[] matches = matchSchedule.split("::");

        //reset schedules
        listeningActitivty.schedules = new ArrayList<>();

        //go through the user schedule and assign robots based on the match schedule
        for (int userID = 0; userID < userSchedules.length; userID++) {
            String name = userSchedules[userID].split(":")[0];
            String[] userSchedule = userSchedules[userID].split(":")[1].split(",");

            UserData currentUserData = new UserData(userID, name);

            listeningActitivty.schedules.add(currentUserData);

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

        if (listeningActitivty.userIDSpinner != null) {
            listeningActitivty.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //update the userIDSpinner if the alert is open
                    listeningActitivty.updateUserIDSpinner();

                    //update the UI with the time remaining
                    if (listeningActitivty instanceof MainActivity) {
                        ((MainActivity) listeningActitivty).updateMatchesLeft();
                    }
                }
            });
        }

        //update the shared preferences
        SharedPreferences prefs = listeningActitivty.getSharedPreferences("userSchedule", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //set size
        editor.putInt("userAmount", listeningActitivty.schedules.size());

        //go through each user and add the data
        for (int i = 0; i < listeningActitivty.schedules.size(); i++) {
            UserData user = listeningActitivty.schedules.get(i);

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

        //get current dates to store the last date updated
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        SharedPreferences lastUpdatedPrefs = listeningActitivty.getSharedPreferences("lastScheduleUpdate", Activity.MODE_PRIVATE);
        SharedPreferences.Editor lastUpdatedEditor = lastUpdatedPrefs.edit();

        //store it in the shared preferences
        lastUpdatedEditor.putInt("year", currentYear);
        lastUpdatedEditor.putInt("month", currentMonth);
        lastUpdatedEditor.putInt("day", currentDay);
        lastUpdatedEditor.putInt("hour", currentHour);
        lastUpdatedEditor.putInt("minute", currentMinute);

        lastUpdatedEditor.apply();
    }

    public void sendLabels() {
        try {
            String outString = listeningActitivty.versionCode + ":::" + listeningActitivty.savedLabels;
            //convert to base 64 bytes
            String outBase64 = Base64.encodeToString(outString.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT) + endSplitter;

            this.out.write(outBase64.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData() {
        try {
            String fullMessage = listeningActitivty.versionCode + ":::";
            for (String message : listeningActitivty.pendingMessages) {
                if (!fullMessage.equals(listeningActitivty.versionCode + ":::")) {
                    fullMessage += "::";
                }
                fullMessage += message;

                sentPendingMessages.add(message);
            }

            if (listeningActitivty.pendingMessages.isEmpty()) {
                fullMessage += "nodata";
            }

            this.out.write((toBase64(fullMessage) + endSplitter).getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteData() { //deleted items that are in sent pending messages (because they now have been sent)

        SharedPreferences prefs2 = listeningActitivty.getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        if (prefs2.getInt("messageAmount", 0) - sentPendingMessages.size() >= 0) {
            editor2.putInt("messageAmount", prefs2.getInt("messageAmount", 0) - sentPendingMessages.size());
        } else {
            editor2.putInt("messageAmount", 0);
        }
        editor2.apply();

        for (String message : new ArrayList<>(sentPendingMessages)) {
            listeningActitivty.pendingMessages.remove(message);
            sentPendingMessages.remove(message);

            int loc = listeningActitivty.getLocationInSharedMessages(message);

            if (loc != -1) {
                SharedPreferences prefs = listeningActitivty.getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("message" + loc, null);
                editor.apply();
            }
        }

        //set pending messages number on ui
        listeningActitivty.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listeningActitivty instanceof MainActivity) {
                    ((TextView) (listeningActitivty.findViewById(R.id.numberOfPendingMessagesLayout)).findViewById(R.id.numberOfPendingMessages)).setText(listeningActitivty.pendingMessages.size() + "");
                } else if (listeningActitivty instanceof StartActivity) {
                    //for the start screen layout
                    ((TextView) listeningActitivty.findViewById(R.id.numberOfPendingMessages)).setText("Unsent Data: " + listeningActitivty.pendingMessages.size());
                }
            }
        });
    }

    public String toBase64(String string) {
        return Base64.encodeToString(string.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT);
    }
}
