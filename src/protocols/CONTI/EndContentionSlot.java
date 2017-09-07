package protocols.CONTI;

import WSN.Node;
import WSN.WSN;
import WSN.Packet;
import events.Event;
import WSN.Scheduler;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created by Gianluca on 23/08/2017.
 */
public class EndContentionSlot extends Event {

    public EndContentionSlot(Node n, double time){ super(n, time, WSN.listenColor); }

    public void run(){
        // obtain Scheduler instance
        Scheduler scheduler = Scheduler.getInstance();

        n.CONTIslotNumber++;
        int maxSlots = WSN.CONTIp.size();


        // determine if node goes on with the contention or defers
        if (n.getStatus() == WSN.NODE_STATUS.LISTENING){
            LinkedList<Node> jammingNodes = WSN.getNeighborsStatus(n, WSN.NODE_STATUS.JAMMING);

            if (WSN.debug){
                System.out.println("\tNode " + n.getId() + " is listening.");
            }

            if (jammingNodes.size() > 0){
                // defer

                if (WSN.debug){
                    System.out.println("\tNode " + n.getId() + " will exit contention.");
                }
                // the round is finished for this node, next transmission attempt in the next round
                n.CONTIaddRound();

                n.setColor(Color.blue);
                // determine time until start of new transmission round
                double remainingTime =  (maxSlots - n.CONTIslotNumber) * WSN.CONTIslotTime + WSN.txTime + WSN.SIFS + WSN.tACK;
                // schedule beginning of a new tx round
                scheduler.schedule(new StartRound(n, time + remainingTime));
            }
            else{
                // no other node is listening...
                if (n.CONTIslotNumber == maxSlots){

                    if (WSN.debug){
                        System.out.println("\tEnd of the last round. Node " + n.getId() + " will transmit.");
                    }

                    // but this is the last contention slot: transmit!
                    n.setStatus(WSN.NODE_STATUS.TRANSMITTING);
                    scheduler.schedule(new StartTxEvent(n, time));
                    n.transmittingNeighbors++; // add myself, if node picks me as destination it will result in collision

                }else{
                    scheduler.schedule(new StartContentionSlot(n, time));
                }
            }
        }else if (n.getStatus() == WSN.NODE_STATUS.JAMMING){

            if (WSN.debug){
                System.out.println("\tNode " + n.getId() + " is jamming.");
            }

            if (n.CONTIslotNumber == maxSlots){
                // transmission starts if node is still in contention
                n.transmittingNeighbors++; // add myself, if node picks me as destination it will result in collision
                scheduler.schedule(new StartTxEvent(n, time));

                if (WSN.debug){
                    System.out.println("\tEnd of the last round. Node " + n.getId() + " will transmit.");
                }

            }else{
                scheduler.schedule(new StartContentionSlot(n, time));
            }

        }
    }

    @Override
    public String toString(){
        return "["+this.time+"][EndContentionSlot " + (n.CONTIslotNumber+1) + "/" + WSN.CONTIp.size() + "][Node " + n.getId() + "]";
    }
}
