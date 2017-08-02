package events;

import java.awt.*;
import WSN.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Event {
    protected Node n;
    protected double time;
    protected int eventIndex;
    protected Color c;

    public Event(Node n, double time, int eventIndex, Color c){
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

    public int run(int currentEventIndex){
        n.setColor(c);
        if (WSN.print) {
            System.out.println("Event index: " + this.eventIndex);
            System.out.println(this);
        }
        else if ((eventIndex % 1000)==0){
            System.out.println("Event index: " + this.eventIndex);
        }
        return 0;       // shift on currentEventIndex needed in case of StopTxEvent adds more than one event in the queue
    }

    public double getTime(){
        return this.time;
    }

    public double getEventIndex(){
        return this.eventIndex;
    }
}


