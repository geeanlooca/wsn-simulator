package protocols.CONTI;

import protocols.Event;
import WSN.*;

import java.awt.*;
import java.util.Random;

/**
 * Created by Gianluca on 23/08/2017.
 */
public class StartContentionSlot extends Event {

    public StartContentionSlot(Node n, double time){ super(n, time, WSN.listenColor); }

    public void run(){


        if (WSN.debug){
            System.out.println("[ContentionSlot " + (n.CONTIslotNumber+1) + "][Node " + n.getId() + "]");
        }

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
                System.out.println("Node " + n.getId() + "");
            }
        }else{
            // node listens
            n.setColor(Color.yellow);
            n.setStatus(WSN.NODE_STATUS.LISTENING);
        }

        scheduler.schedule(new EndContentionSlot(n, time + WSN.CONTIslotTime));
    }

    @Override
    public String toString(){
        return "";
    }
}
