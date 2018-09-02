package ca.lakeeffect.scoutingapp;

import java.util.ArrayList;

public class UserData {
    int userID;

    String userName;

    ArrayList<Integer> robots;

    //red is false, true is blue
    //lists what alliance this robot is on in each match
    ArrayList<Boolean> alliances;

    public UserData(int userID, String userName, ArrayList<Boolean> alliances, ArrayList<Integer> robots){
        this.userID = userID;
        this.userName = userName;
        this.alliances = alliances;
        this.robots = robots;
    }

    //is the user able to go off duty (someone else has swapped in)
    public boolean isOff() {
        return false;
    }
}