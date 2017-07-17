package events;

import events.Event;
import WSN.*;

/**
 * Created by Gianluca on 17/07/2017.
 */
public class StartSleepEvent extends Event {
    public StartSleepEvent(Node n, double time){
        super(n, time, WSN.sleepColor);
    }

    public void run(){
        super.run();
        n.setStatus(WSN.NODE_STATUS.SLEEPING);

        // schedule wakeup event
        WSN.eventList.add(new WakeUpEvent(this, getTime() + WSN.sleepTime));
    }


    @Override
    public String toString(){
        return "[" + time + "][StartSleepEvent] from node " +  this.n;
    }
}
