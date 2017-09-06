package protocols.GALTIER;

import WSN.Node;
import WSN.RNG;
import WSN.Scheduler;
import WSN.WSN;
import events.Event;

import java.awt.*;

/**
 * Created by Gianluca on 23/08/2017.
 */
public class StartContentionSlot extends Event {

    public StartContentionSlot(Node n, double time){ super(n, time, WSN.listenColor); }

    public void run(){

        // obtain Scheduler instance
        Scheduler scheduler = Scheduler.getInstance();

        RNG r = RNG.getInstance();
        double p = WSN.galtierP.get(n.GALTIERcounter).get(n.GALTIERidx);
        if (WSN.debug){
            System.out.println("Probability: " + p);
        }

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
        return "["+this.time+"][ContentionSlot " + (n.GALTIERcounter+1) + "/" + WSN.galtierP.size() + "][Node " + n.getId() + "]";
    }
}
