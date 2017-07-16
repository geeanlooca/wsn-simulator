package events;

import WSN.*;
import events.Event;
import events.StartSleepEvent;

/**
 * Created by Gianluca on 17/07/2017.
 */
public class WakeUpEvent extends Event {
    public WakeUpEvent(int id, StartSleepEvent e, double time){
        super(id, e.getNode(), time, WSN.normColor);
    }

    public void run(){
        super.run();
    }

    @Override
    public String toString(){
        return "[" + time + "][events.WakeUpEvent] from node " +  this.n;
    }
}
