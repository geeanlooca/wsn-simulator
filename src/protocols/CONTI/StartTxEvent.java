package protocols.CONTI;

import WSN.Node;
import WSN.Packet;
import WSN.Scheduler;
import WSN.WSN;
import WSN.Packet;
import events.Event;
import WSN.RNG;
import protocols.CONTI.StopTxEvent;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Gianluca on 24/08/2017.
 */
public class StartTxEvent extends Event {

    private Packet p;

    public StartTxEvent(Node n, double time){
        super(n, time, WSN.txColor);
        this.p = p;
    }

    @Override
    public String toString(){
        return "[" + time + "][StartTxEvent][Node " +  this.n.getId()+"]";
    }



    public void run() {
        super.run();

        n.setColor(WSN.txColor);

        Scheduler scheduler = Scheduler.getInstance();
        RNG r = RNG.getInstance();

        // pick random neighbor as destination
        ArrayList<Node> neighbors = n.getNeighborList();
        int neighSize = neighbors.size();

        Node dest;
        try{
            dest = neighbors.get(r.nextInt(neighSize));
        }catch (IllegalArgumentException exc){
            dest = null;
        }

        // create packet
        p = new Packet(n, dest);

        // for collision detection at receiver, record how many neighbors of a certain node are transmitting
        for (Node neig:
             neighbors) {
            neig.transmittingNeighbors++;
        }

        // keep track of the nodes that start a transmission (useful to Fairness calculation)
        WSN.nodeTrace.add(this.n);

        scheduler.schedule(new StopTxEvent(this, time + WSN.txTime));
    }

    public Packet getPacket(){
        return this.p;
    }
}
