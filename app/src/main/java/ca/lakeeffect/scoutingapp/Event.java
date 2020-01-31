package ca.lakeeffect.scoutingapp;

/**
 * Created by ramachandra on 12/02/2018.
 */

public class Event {

    float[] eventType = {-1, -1, -1, -1, -1, -1};

    float[] location = {-1, -1};

    long timestamp = -1;

    int metadata = -1; //high or low for the switch

    public Event(float[] eventType, float[] location, long timestamp, int metadata){
        this.eventType = eventType;
        this.location = location;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
}
