package protocols.DCF;

import WSN.Node;
import WSN.WSN;
import WSN.Scheduler;
import WSN.Packet;
import events.Event;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by gianluca on 28/07/17.
 */
public class StartListeningEvent extends Event {


    public StartListeningEvent(Node n, Double time){
        super(n, time, WSN.listenColor);
    }

    public void run(){

        super.run();

        Scheduler scheduler = Scheduler.getInstance();

        ArrayList<Node> neighbors = this.n.getNeighborList();
        if (neighbors.isEmpty()){
            // this node has no neighbors
            if(WSN.debug) { System.out.println("WARNING: Node "+this.n.getId()+ " hasn't neighbors! "); }
            this.n.increaseNoNeighbor();

            scheduler.schedule(new StartListeningEvent(n,time + WSN.DIFS + WSN.txTime+ WSN.tACK + WSN.SIFS));
        }
        else {
            // there is at least one neighbor
            Random rand = new Random();
            Packet p = new Packet(n, neighbors.get(rand.nextInt(neighbors.size())));
            n.enqueuePacket(p);
            if (WSN.debug) { System.out.println("Next packet destination: Node " + n.getNextPacket().getDestination().getId()); }

            LinkedList<Node> transmittingNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.TRANSMITTING);

            if(WSN.debug){
                for (Node entry: transmittingNodes) {
                    System.out.println(" Node "+entry.getId()+" is transmitting");
                }
                System.out.println("Node status: "+ (n.getStatus()) );
            }

            if (transmittingNodes.isEmpty() && n.getStatus() == WSN.NODE_STATUS.IDLING) {
                // channel free
                if( WSN.debug) { System.out.println("Channel is free. BO counter: " + n.getBOcounter()); }
                n.setStatus(WSN.NODE_STATUS.LISTENING);
                n.freeChannel = true;
                scheduler.schedule(new CheckChannelStatus(n, time + WSN.DIFS, WSN.DIFS));
                // mark the contention initial time (useful to Delay)
                this.n.startTXTime(this.time);

            }
            else if(n.getStatus() == WSN.NODE_STATUS.IDLING){
                // channel busy (caused by an update of the neighbors). Reschedule this event
                if( WSN.debug) { System.out.println("Channel is busy. BO counter: " + n.getBOcounter()+"\nReschedule StartListening Event..."); }
                scheduler.schedule(new StartListeningEvent(n, time + WSN.DIFS));

            }

        }
    }

    @Override
    public String toString(){
        return "[" + time + "][StartListeningEvent] from node " +  this.n;
    }
}
