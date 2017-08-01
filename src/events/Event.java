package events;

import java.awt.*;
import WSN.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Event {
    protected Node n;
    protected double time;
    protected double eventIndex;
    protected Color c;

    public Event(Node n, double time, double eventIndex, Color c){
        this.n = n;
        this.time = time;
        this.eventIndex = eventIndex;
        this.c = c;
    }

    public String toString(){
        return "[" + time + "]" + "[EventIndex: " +  eventIndex + "]" + " fired by node " + n.getId();
    }

    public Node getNode(){
        return this.n;
    }

    public void run(double currentEventIndex){
        n.setColor(c);
        System.out.println(this);
    }

    public double getTime(){
        return this.time;
    }

    public double getEventIndex(){
        return this.eventIndex;
    }
}


