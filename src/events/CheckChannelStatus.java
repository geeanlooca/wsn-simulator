package events;

import WSN.Node;
import WSN.WSN;
import WSN.Packet;
import WSN.Scheduler;
import java.util.*;


/**
 * Created by gianluca on 28/07/17.
 */

public class CheckChannelStatus extends Event{

    private double duration;

    public CheckChannelStatus(Node n, double time, double duration){
        super(n, time, WSN.listenColor);
        this.duration = duration;
    }

    public void run(){
        super.run();

        Scheduler scheduler = Scheduler.getInstance();

        // if the sender node (this) and the destination node sense the channel free new events are scheduled

        if (n.freeChannel && (n.getNextPacket().getDestination().freeChannel)){

            if (WSN.debug){ System.out.println("Channel has been free for: " + duration);}
            if (duration == WSN.tSlot){

                // decrease BO counter
                int bo = n.decreaseCounter();
                if (WSN.debug){ System.out.println("BO Counter decreased: " + bo);};

                if (bo > 0){
                    scheduler.schedule(new CheckChannelStatus(n,time + WSN.tSlot, WSN.tSlot));

                }else{
                    // transmit
                    if (WSN.debug){ System.out.println("-> This node (" + this.n.getId() + ") will now start transmitting.");};

                    n.addTransmission();             // increment transmissions counter
                    WSN.nodeTrace.add(this.n);          // keep track of the nodes that start a transmission (useful to Fairness calculation)


                    scheduler.schedule(new StartTxEvent(n, n.getNextPacket(), time));
                }

                this.n.addContSlot();       // increment contention slot counter
                this.n.addSlotTime();          // add a tSLOT to the packet transmission time
            }
            else if (duration == WSN.DIFS){

                // restart BO counter
                if (n.getBOcounter() == 0){
                    if (WSN.debug){ System.out.println("-> This node (" + this.n.getId() + ") will now start transmitting.");};
                    // transmit

                    n.addTransmission();               // increment transmissions counter
                    WSN.nodeTrace.add(this.n);         // keep track of the nodes that start a transmission (useful to Fairness calculation)

                    scheduler.schedule(new StartTxEvent(n,  n.getNextPacket(), time));
                }else {

                    scheduler.schedule(new CheckChannelStatus(n, time + WSN.tSlot, WSN.tSlot));
                }

                n.addDIFStime();            // add a DIFS time to the packet transmission time
            }
        }
    }

    @Override
    public String toString(){
        return "[" + time + "][CheckChannelStatus] from node " +  this.n;
    }
}
