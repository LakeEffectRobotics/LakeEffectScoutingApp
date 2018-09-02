package ca.lakeeffect.scoutingapp;

import java.util.ArrayList;

public class UserData {
    int userID;

    String userName;

    ArrayList<Integer> robots;

    public UserData(int userID, String userName, ArrayList<Integer> robots){
        this.userID = userID;
        this.userName = userName;
        this.robots = robots;
    }

    //is the user able to go off duty (someone else has swapped in)
    public boolean isOff() {
        return false;
    }
}