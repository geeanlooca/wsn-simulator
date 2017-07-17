package events;

import WSN.*;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import events.Event;
import events.StartSleepEvent;

/**
 * Created by Gianluca on 17/07/2017.
 */
public class WakeUpEvent extends Event {
    public WakeUpEvent(StartSleepEvent e, double time){
        super(e.getNode(), time, WSN.normColor);
    }

    public void run(){
        super.run();
        n.setStatus(WSN.NODE_STATUS.IDLING);

        // what to do when the node wakes up?
        // sensing?

        if (n.backlogged()){

            Packet p = n.dequeue();
            // schedule transmission at time t0 + t_backoff
            WSN.eventList.add(new StartTxEvent(n, p, time + WSN.getPoisson(WSN.meanBackoff)));
        }
        else
        {
            WSN.eventList.add(new StartSleepEvent(n, time + WSN.sleepTime*4 + WSN.getPoisson(20)));
        }
    }

    @Override
    public String toString(){
        return "[" + time + "][events.WakeUpEvent] from node " +  this.n;
    }
}
