package protocols.GALTIER;

import WSN.*;
import events.Event;

import java.util.ArrayList;

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

        WSN.attempted.add(time);

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
        WSN.nodeTraceTimes.add(this.time);
        // increase transmission counter for this node
        n.addTransmission();

        scheduler.schedule(new StopTxEvent(this, time + WSN.txTime));
    }

    public Packet getPacket(){
        return this.p;
    }
}
