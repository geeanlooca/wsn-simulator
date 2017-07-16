package events;

import java.awt.*;
import WSN.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Event {
    protected int id;
    protected Node n;
    protected double time;
    protected Color c;

    public Event(int id, Node n, double time, Color c){
        this.id = id;
        this.n = n;
        this.time = time;
        this.c = c;
    }

    public String toString(){
        return "[" + time + "][" + id + "]" + " fired by node " + n.getId();
    }


    public int getId(){
        return this.id;
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
