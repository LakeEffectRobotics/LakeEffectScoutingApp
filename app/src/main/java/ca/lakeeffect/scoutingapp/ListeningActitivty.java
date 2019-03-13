package ca.lakeeffect.scoutingapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

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
    int matchNumber = -1;

    //the id of the user currently scouting. This decides when they must switch on and off from scouting
    int userID = -1;

    TextView matchesLeftText; //text that shows the matches left until off

    ArrayList<String> pendingMessages = new ArrayList<>();

    String savedLabels = null; //generated at the beginning

    public void updateUserIDSpinner() {
        String oldSelection = ((String) userIDSpinner.getSelectedItem());

        ArrayList<String> userNames = new ArrayList<>();

        if (userNames.size() > 0) {
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

    //updates the view showing the matches left until this scout is off
    public void updateMatchesLeft() {
        int nextMatchOff = getNextMatchOff();
        int matchesLeft = nextMatchOff - matchNumber;

        if (matchesLeftText != null) {
            if (nextMatchOff == -1) {
                matchesLeftText.setText("Never");
            } else {
                matchesLeftText.setText(matchesLeft + "");
            }
        }
    }

    //this will return the match number when they have can stop scouting
    public int getNextMatchOff() {
        int matchBack = -1;

        //there is no schedule
        if (userID == -1) return -1;

        //find next match number
        int matchNumber = this.matchNumber;
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
    public int getNextMatchOn() {
        int matchBack = -1;

        //there is no schedule
        if (userID == -1) return -1;

        //find next match number
        int matchNumber = this.matchNumber;
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
}