package protocols.CONTI;

import WSN.Packet;
import WSN.Scheduler;
import WSN.Node;
import WSN.WSN;
import events.Event;
import protocols.CONTI.StopTxEvent;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends Event {


    private Packet p;

    public StopTxEvent(StartTxEvent e, double time){
        super(e.getNode(), time, WSN.txColor);
        this.p = e.getPacket();
    }

    @Override
    public String toString(){
        return "[" + time + "][StopTxEvent] from node " +  this.n;
    }

    public void run(){
        //super.run();

        // retrieve destination of packet
        Node dest = p.getDestination();

        WSN.nodeTrace.add(this.n);

        n.addTransmission();

        if (dest == null){
            // collision -> lost packet
            //n.addCollision();
            n.CONTIaddRound();
        }else{

            //n.addTransmission();

            // number of neighbors of destination that were transmitting during this packet transmission
            int transmittingNeighbors = dest.transmittingNeighbors;

            if (transmittingNeighbors > 1){
                // interference at receiver -> collision
                if (WSN.debug) { System.out.println("  Transmission unsuccessful!!"); }
                n.addCollision();
                // the round is finished, next transmission attempt in the next round
                n.CONTIaddRound();
            }else{
                if (WSN.debug) { System.out.println("  Transmission successful!"); }
                n.CONTIsetTotalTime();
            }
        }

        Scheduler scheduler = Scheduler.getInstance();
        scheduler.schedule(new StartRound(n, time + WSN.SIFS + WSN.tACK));
        this.n.setSize(WSN.normSize);
        this.n.setColor(WSN.normColor);
    }

    public Packet getPacket(){
        return this.p;
    }
}
