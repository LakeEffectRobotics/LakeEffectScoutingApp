package ca.lakeeffect.scoutingapp;

/**
 * Created by ramachandra on 12/02/2018.
 */

public class Event {

    //pickup, missed, lower, outer, inner
    float[] eventData = {-1, -1, -1, -1, -1};

    float[] location = {-1, -1};

    long[] timestamp = {-1, -1};

    int metadata = -1; //for the spinny wheel, the eventData will all be -1 and this will be 1 for successful rotation, 2 for failed rotation, 3 for successful colour, 4 for failed colour

    public Event(float[] eventData, float[] location, long[] timestamp, int metadata){
        this.eventData = eventData;
        this.location = location;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
}
