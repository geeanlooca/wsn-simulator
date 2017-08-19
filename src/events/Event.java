package events;

import java.awt.*;
import WSN.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Event {
    protected Node n;
    protected double time;
    private Color c;

    public Event(Node n, double time, Color c){
        this.n = n;
        this.time = time;
        this.c = c;
    }

    public String toString(){
        return "[" + time + "]" + " fired by node " + n.getId();
    }

    public Node getNode(){
        return this.n;
    }

    public void run(){
        n.setColor(c);
    }

    public double getTime(){
        return this.time;
    }
}


