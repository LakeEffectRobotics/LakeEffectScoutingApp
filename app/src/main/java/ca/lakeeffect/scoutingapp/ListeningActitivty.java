package ca.lakeeffect.scoutingapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

//contains the base of what an activity using ConnectionThread
// and ListenerThread should have
public class ListeningActitivty extends AppCompatActivity {
    ListenerThread listenerThread;
    Thread listenerThreadThreadClass;

    //the userIDSpinner on the alert menu
    //null if alert is not open
    Spinner userIDSpinner = null;

    //Robot schedule for each user (by user ID)
    //the username selection screen will show a spinner with all the names in this list
    //Add one to the index as there is one placeholder default value
    ArrayList<UserData> schedules = new ArrayList<>();

    int versionCode;

    TextView matchesLeftText; //text that shows the matches left until off

    ArrayList<String> unsentData = new ArrayList<>();

    String savedLabels = null; //generated at the beginning

    public void loadUnsentData() {
        //go through all saved pending messages and add them to the variable
        //called pendingMessages for legacy reasons
        SharedPreferences prefs = getSharedPreferences("pendingMessages", MODE_PRIVATE);
        int messageAmount = prefs.getInt("messageAmount", 0);
        for (int i = 0; i < messageAmount; i++) {
            if (prefs.getString("message" + i, null) == null) {
                messageAmount ++;
                i++;
                if(i > 150){
                    break;
                }
            } else {
                unsentData.add(prefs.getString("message" + i, ""));
            }
        }

        //reset the amount of pending messages
        SharedPreferences prefs2 = getSharedPreferences("pendingMessages", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        editor2.putInt("messageAmount", unsentData.size());
        editor2.apply();
    }

    public void loadSchedule() {
        //load the saved schedule
        SharedPreferences schedulePrefs = getSharedPreferences("userSchedule", Context.MODE_PRIVATE);
        int userAmount = schedulePrefs.getInt("userAmount", 0);

        for (int i = 0; i < userAmount; i++) {
            String[] robotNumbers = schedulePrefs.getString("robots" + i, "").split(",");
            String[] alliances = schedulePrefs.getString("alliances" + i, "").split(",");
            String userName = schedulePrefs.getString("userName" + i, "");

            UserData user = new UserData(i, userName);

            for (String robotNum : robotNumbers) {
                user.robots.add(Integer.parseInt(robotNum));
            }
            for (String alliance : alliances) {
                user.alliances.add(Boolean.parseBoolean(alliance));
            }

            //add the user data to the list of schedules
            schedules.add(user);
        }
    }

    public void updateUserIDSpinner() {
        String oldSelection = ((String) userIDSpinner.getSelectedItem());

        ArrayList<String> userNames = new ArrayList<>();

        if (schedules.size() > 0) {
            userNames.add("Please choose a name");
            for (UserData userData : schedules){
                userNames.add(userData.userName);
            }

            int selectedIndex = 0;

            for (int i = 0; i < userNames.size(); i++) {
                if (userNames.get(i).equals(oldSelection)) {
                    selectedIndex = i;
                    break;
                }
            }

            userIDSpinner.setSelection(selectedIndex);
        } else {
            userNames.add("Please ask the person running the scouting server to send a schedule");
        }

        ArrayAdapter<String> userIDAdapter = new ArrayAdapter<String>(ListeningActitivty.this, R.layout.spinner, userNames);
        userIDSpinner.setAdapter(userIDAdapter);
    }

    //this will return the match number when they have can stop scouting
    public int getNextMatchOff(int matchNumber, int userID) {
        int matchBack = -1;

        //there is no schedule
        if (userID == -1) return -1;

        //find next match number
        if (matchNumber <= 0) matchNumber = 1;
        for (int i = matchNumber - 1; i < schedules.get(userID).robots.size(); i++) {
            if (schedules.get(userID).robots.get(i) == -1) {
                matchBack = i + 1;
                break;
            }
        }

        return matchBack;
    }

    //this will return the match number when they have have to start scouting again
    public int getNextMatchOn(int matchNumber, int userID) {
        int matchBack = -1;

        //there is no schedule
        if (userID == -1) return -1;

        //find next match number
        if (matchNumber <= 0) matchNumber = 1;
        for (int i = matchNumber - 1; i < schedules.get(userID).robots.size(); i++) {
            if (schedules.get(userID).robots.get(i) != -1) {
                matchBack = i + 1;
                break;
            }
        }

        return matchBack;
    }

    public int getLocationInSharedMessages(String message) {
        SharedPreferences prefs = getSharedPreferences("pendingMessages", MODE_PRIVATE);
        for (int i = 0; i < prefs.getInt("messageAmount", 0); i++) {
            if (prefs.getString("message" + i, "").equals(message)) {
                return i;
            }
        }
        return -1;
    }

    public void openScheduleViewer() {
        ScrollView scheduleViewer = new ScrollView(this);

        final LinearLayout scheduleViewerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.schedule_viewer, null);

        //figure out when the schedule was last updated
        TextView lastUpdated = scheduleViewerLayout.findViewById(R.id.scheduleViewerLastUpdate);
        String lastUpdatedMessage = "";
        SharedPreferences lastUpdatedPrefs = getSharedPreferences("lastScheduleUpdate", MODE_PRIVATE);
        int year = lastUpdatedPrefs.getInt("year", -1);
        int month = lastUpdatedPrefs.getInt("month", -1);
        int day = lastUpdatedPrefs.getInt("day", -1);
        int hour = lastUpdatedPrefs.getInt("hour", -1);
        int minute = lastUpdatedPrefs.getInt("minute", -1);

        //get current dates to compare with
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        if (year == currentYear) {
            if (month == currentMonth) {
                if (day == currentDay) {
                    if (hour == currentHour) {
                        lastUpdatedMessage = (currentMinute - minute) + " minutes ago";
                    } else {
                        lastUpdatedMessage = (currentHour - hour) + " hours ago";
                    }
                } else {
                    lastUpdatedMessage = (currentDay - day) + " days ago";
                }
            } else {
                lastUpdatedMessage = (currentMonth - month) + " months ago";
            }
        } else {
            lastUpdatedMessage = (currentYear - year) + " years ago";
        }
        if (year == -1) {
            lastUpdatedMessage = "Never";
        }

        //set the message onto the label
        lastUpdated.setText("Last Updated " + lastUpdatedMessage);

        userIDSpinner = scheduleViewerLayout.findViewById(R.id.scheduleViewerUserIDSpinner);
        updateUserIDSpinner();

        SharedPreferences prefs = getSharedPreferences("userID", MODE_PRIVATE);
        int userID = prefs.getInt("userID", -1);

        if (userID != -1) {
            //set the spinner to this value
            userIDSpinner.setSelection(userID + 1);

            //update the schedule view
            updateScheduleDialog(scheduleViewerLayout, userID);
        }

        //add listener to the spinner
        userIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    //if it is not on the "Choose one" selection
                    updateScheduleDialog(scheduleViewerLayout, position - 1);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //add the layout to the scroll view (the entire view)
        scheduleViewer.addView(scheduleViewerLayout);

        //create the dialog box
        new android.app.AlertDialog.Builder(this)
                .setTitle("View Schedule")
                .setView(scheduleViewer)
                .setPositiveButton("Dismiss", null)
                .show();
    }

    public void updateScheduleDialog(View view, int userID) {
        TextView scheduleTextView = view.findViewById(R.id.scheduleViewerTextView);

        //show all the times the scout is switching on or off
        StringBuilder scheduleMessage = new StringBuilder();

        //the current match number being checked for
        int matchNumber = 0;

        while (matchNumber < schedules.get(userID).robots.size()) {
            if (!schedules.get(userID).isOff(matchNumber)) {
                //add the next match off
                int nextMatchOff = getNextMatchOff(matchNumber, userID);

                if (nextMatchOff == -1) {
                    //no more switches on or off, this user is done for the day
                    break;
                }

                scheduleMessage.append("Off at match " + nextMatchOff + "\n\n");

                //set the match number to check to the match number reached
                matchNumber = nextMatchOff;
            } else {
                //add the next match on
                int nextMatchOn = getNextMatchOn(matchNumber, userID);

                if (nextMatchOn == -1) {
                    //no more switches on or off, this user is done for the day
                    break;
                }

                scheduleMessage.append("On at match " + nextMatchOn + "\n\n");

                //set the match number to check to the match number reached
                matchNumber = nextMatchOn;
            }
        }

        scheduleTextView.setText(scheduleMessage.toString());
    }
}