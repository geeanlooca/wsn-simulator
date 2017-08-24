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

        //System.out.println("n.freeChannel "+n.freeChannel+ " - (n.getNextPacket().getDestination().freeChannel) "+ (n.getNextPacket().getDestination().freeChannel));

        if (n.freeChannel && (n.getNextPacket().getDestination().freeChannel)){
            if (WSN.debug){ System.out.println("Channel has been free for: " + duration);}
            if (duration == WSN.tSlot){

                this.n.addContSlot();       // increment contention slot counter
                this.n.addtSlot();          // add a tSLOT to the packet transmission time

                // decrease BO counter
                int bo = n.decreaseCounter();
                if (WSN.debug){ System.out.println("BO Counter decreased: " + bo);};

                if (bo > 0){
                    scheduler.schedule(new CheckChannelStatus(n,time + WSN.tSlot, WSN.tSlot));

                }else{
                    // transmit
                    if (WSN.debug){ System.out.println("-> This node (" + this.n.getId() + ") will now start transmitting.");};

                    n.addTransmission();    // increment transmissions counter
                    WSN.nodeTrace.add(this.n);      // add transmitting node to the trace (useful to Fairness calculation)

                    //WSN.listeningNodes.remove(n);


                    // NEW! the packet has a destination node

                    //Random rand = new Random();
                    //ArrayList<Node> neighbors = n.getNeighborList();

                    //Packet p = new Packet(n, neighbors.get(rand.nextInt(neighbors.size())));
                    Packet p = new Packet(n, n);
                    scheduler.schedule(new StartTxEvent(n, p, time));
                }
            }
            else if (duration == WSN.DIFS){

                n.addDIFS();            // add a DIFS to the packet trasmission time

                // restart BO counter
                if (n.getBOcounter() == 0){
                    if (WSN.debug){ System.out.println("-> This node (\"+n.getId()+\") will now start transmitting.");};
                    // transmit

                    n.addTransmission();    // increment transmissions counter
                    WSN.nodeTrace.add(this.n);


                    //WSN.listeningNodes.remove(n);
                    Packet p = new Packet(n, n);
                    scheduler.schedule(new StartTxEvent(n, p, time));
                }else {

                    scheduler.schedule(new CheckChannelStatus(n, time + WSN.tSlot, WSN.tSlot));
                }
            }
        }
    }

    @Override
    public String toString(){
        return "[" + time + "][CheckChannelStatus] from node " +  this.n;
    }
}
