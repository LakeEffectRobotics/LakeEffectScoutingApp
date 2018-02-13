package ca.lakeeffect.scoutingapp;

/**
 * Created by ramachandra on 12/02/2018.
 */

public class Event {

    int eventType = -1;

    int location = -1;

    long timestamp = -1;

    int metadata = -1; //high or low for the switch

    public Event(int eventType, int location, long timestamp, int metadata){
        this.eventType = eventType;
        this.location = location;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
}
