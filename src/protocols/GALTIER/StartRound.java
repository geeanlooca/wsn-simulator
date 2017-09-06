package protocols.GALTIER;

import WSN.Node;
import WSN.Scheduler;
import WSN.WSN;
import events.Event;


/**
 * Created by Gianluca on 23/08/2017.
 */
public class StartRound extends Event {

    public StartRound(Node n, Double time){ super(n, time, WSN.normColor); }

    public void run(){

        super.run();
        n.setColor(WSN.normColor);
        n.GALTIERcounter = 0;
        Scheduler scheduler = Scheduler.getInstance();
        scheduler.schedule(new StartContentionSlot(n, time + WSN.DIFS));
        n.transmittingNeighbors = 0;

        // clear list countaining sequence of bits
        n.GALTIERseq.clear();
        n.GALTIERidx = 0;
    }

    @Override
    public String toString(){
        return "[" + time + "][StartRound][Node " +  this.n.getId() + "]";
    }
}
