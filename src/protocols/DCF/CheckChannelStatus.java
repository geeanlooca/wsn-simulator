package protocols.DCF;

import WSN.Node;
import WSN.WSN;
import WSN.Scheduler;
import protocols.Event;

import java.util.*;


/**
 * Created by gianluca on 28/07/17.
 */

public class CheckChannelStatus extends Event {

    private double duration;

    public CheckChannelStatus(Node n, double time, double duration){
        super(n, time, WSN.listenColor);
        this.duration = duration;
    }

    public void run(){
        super.run();

        Scheduler scheduler = Scheduler.getInstance();

        // if both the sender node (this) and the destination node sense the channel free new events are scheduled

        // if (n.freeChannel && (n.getNextPacket().getDestination().freeChannel)){          -> last modified
        if (n.freeChannel && (!checkDestNeighbors())){

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

                    // increment transmissions counter
                    n.addTransmission();
                    // keep track of the nodes that start a transmission (useful to Fairness calculation)
                    WSN.nodeTrace.add(this.n);

                    scheduler.schedule(new StartTxEvent(n, time));
                }
                // increment contention slot counter
                this.n.addContSlot();
                // add a tSLOT to the packet transmission time
                this.n.addSlotTime();
            }
            else if (duration == WSN.DIFS){

                // restart BO counter
                if (n.getBOcounter() == 0){
                    if (WSN.debug){ System.out.println("-> This node (" + this.n.getId() + ") will now start transmitting.");};
                    // transmit

                    // increment transmissions counter
                    n.addTransmission();
                    // keep track of the nodes that start a transmission (useful to Fairness calculation)
                    WSN.nodeTrace.add(this.n);

                    scheduler.schedule(new StartTxEvent(n,  time));
                }else {

                    scheduler.schedule(new CheckChannelStatus(n, time + WSN.tSlot, WSN.tSlot));
                }

                // add a DIFS time to the packet transmission time
                n.addDIFStime();
            }
        }
    }

    private boolean checkDestNeighbors(){
        // check if the destination node is already the destination node of a neighbor of him (surely I have to exclude (this) node from the neighbors)
        boolean statement = false;
        Node dest = n.getNextPacket().getDestination();
        LinkedList<Node> destNeighbors = WSN.getNeighborsStatus(dest, WSN.NODE_STATUS.TRANSMITTING);

        if (!dest.freeChannel) {
            for (Node entry : destNeighbors) {
                if (entry.getNextPacket().getDestination().getId() == dest.getId() && (entry.getId() != this.n.getId())) {
                    statement = true;
                    break;
                }
            }
        }
        return statement;
    }

    @Override
    public String toString(){
        return "[" + time + "][CheckChannelStatus] from node " +  this.n;
    }
}
