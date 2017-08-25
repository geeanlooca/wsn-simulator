package protocols.CONTI;

import protocols.Event;
import WSN.*;

import java.awt.*;

/**
 * Created by Gianluca on 23/08/2017.
 */
public class StartContentionSlot extends Event {

    public StartContentionSlot(Node n, double time){ super(n, time, WSN.listenColor); }

    public void run(){

        // obtain Scheduler instance
        Scheduler scheduler = Scheduler.getInstance();

        // get probability to send a jam
        double p = n.CONTIp[n.CONTIslotNumber];

        RNG r = RNG.getInstance();
        if (r.nextDouble() < p){
            // node transmits a jam signal
            n.setStatus(WSN.NODE_STATUS.JAMMING);
            n.setColor(Color.RED);
            if (WSN.debug){
                System.out.println("\tNode " + n.getId() + " is jamming");
            }
        }else{
            // node listens
            n.setColor(Color.yellow);
            n.setStatus(WSN.NODE_STATUS.LISTENING);

            if (WSN.debug){
                System.out.println("\tNode " + n.getId() + " is listening");
            }
        }

        scheduler.schedule(new EndContentionSlot(n, time + WSN.CONTIslotTime));
    }

    @Override
    public String toString(){
        return "["+this.time+"][ContentionSlot " + (n.CONTIslotNumber+1) + "/" + n.CONTIp.length + "][Node " + n.getId() + "]";
    }
}
