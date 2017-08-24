package protocols.DCF;
import WSN.*;
import WSN.RNG;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends protocols.Event {


    private Packet p;

    public StopTxEvent(StartTxEvent e, double time){
        super(e.getNode(), time, WSN.normColor);
        this.p = e.getPacket();
    }

    @Override
    public String toString(){
        return "[" + time + "][StopTxEvent] from node " +  this.n;
    }

    public void run(){
        super.run();

        Scheduler scheduler = Scheduler.getInstance();

        this.n.setSize(WSN.normSize);

        //WSN.trasmittingNodes.remove(n);

        n.setStatus(WSN.NODE_STATUS.IDLING);

        RNG r = RNG.getInstance();

        this.n.addTX();             // add txTime to the total packet transmission time


        if (n.collided){

            if (WSN.debug){ System.out.println("Tranmission unsuccessful");};
            if (WSN.debug){ System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");};
            if (WSN.debug){ System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");};

            this.n.addCollision();               // increment collision counter for this node
            this.n.resetContSlot();              // reset the contention slot counter for this node (this round is finished)

            //       I'm not sure to put here the reset of the contention time slot counter. If a collision occurs the contention fails, thus we start a new contention. Right?

            int oldCW = n.getCW();
            int newCW = Math.min(2*(oldCW+1) - 1, WSN.CWmax);

            n.setCW(newCW);

            n.setBOcounter(r.nextInt(n.getCW() + 1));

            // start new round NOW
            scheduler.schedule(new StartListeningEvent(n, time));
        }else{

            if (WSN.debug){ System.out.println("->Tranmission successful!");};
            n.setCW(WSN.CWmin);
            n.setBOcounter(r.nextInt(n.getCW() + 1));

            this.n.storeContSlotNumber();           // save the contention slot counter (this round is successfully finished)
            this.n.setTotalTime(time);              // save the overall packet transmission time (useful to throughput and delay)

            // start new round after SIFS + tACK
            scheduler.schedule(new StartListeningEvent(n,time + WSN.tACK + WSN.SIFS));
        }

        // if the end of this transmission frees up the channel then notify all of the listening nodes
        // and make them start listening for DIFS seconds of silence

        ArrayList<Node> transmittingNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.TRANSMITTING);
        ArrayList<Node> listeningNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.LISTENING);


        if (transmittingNodes.isEmpty()){
            //WSN.status = WSN.CHANNEL_STATUS.FREE;           // Useless parameter, I can check the channel status looking at the number of transmitting nodes in the local range

            for (Node listening : listeningNodes) {
                scheduler.schedule(new CheckChannelStatus(listening, time + WSN.DIFS, WSN.DIFS));

                listening.freeChannel = true;
                listening.resetContSlot();      //  reset the contention slot counter for all the listening nodes (the round is finished)
            }
        }

       // n.setStatus(WSN.NODE_STATUS.IDLING);
    }

    public Packet getPacket(){
        return this.p;
    }
}
