package protocols.CONTI;

import events.Event;
import WSN.Scheduler;
import WSN.Node;
import WSN.WSN;


/**
 * Created by Gianluca on 23/08/2017.
 */
public class StartRound extends Event {

    public StartRound(Node n, Double time){ super(n, time, WSN.normColor); }

    public void run(){

        super.run();
        n.setColor(WSN.normColor);
        n.CONTIslotNumber = 0;
        Scheduler scheduler = Scheduler.getInstance();
        scheduler.schedule(new StartContentionSlot(n, time + WSN.DIFS));
        n.transmittingNeighbors = 0;
    }

    @Override
    public String toString(){
        return "[" + time + "][StartRound][Node " +  this.n.getId() + "]";
    }
}
