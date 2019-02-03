package ca.lakeeffect.scoutingapp;

import java.util.ArrayList;

public class UserData {
    int userID;

    String userName;

    ArrayList<Integer> robots = new ArrayList<>();

    //red is false, true is blue
    //lists what alliance this robot is on in each match
    ArrayList<Boolean> alliances = new ArrayList<>();

    public UserData(int userID, String userName, ArrayList<Boolean> alliances, ArrayList<Integer> robots){
        this.userID = userID;
        this.userName = userName;
        this.alliances = alliances;
        this.robots = robots;
    }

    public UserData(int userID, String userName){
        this.userID = userID;
        this.userName = userName;
    }

    //is the user able to go off duty (someone else has swapped in)
    public boolean isOff(int matchNum) {
        return robots.get(matchNum) == -1;
    }
}