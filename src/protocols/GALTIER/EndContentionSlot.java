package protocols.GALTIER;

import WSN.Node;
import WSN.Scheduler;
import WSN.WSN;
import events.Event;

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

        n.GALTIERcounter++;
        int maxSlots = WSN.galtierP.size();

        // determine if node goes on with the contention or defers
        if (n.getStatus() == WSN.NODE_STATUS.LISTENING){
            LinkedList<Node> jammingNodes = WSN.getNeighborsStatus(n, WSN.NODE_STATUS.JAMMING);

            if (WSN.debug){
                System.out.println("\tNode " + n.getId() + " is listening.");
            }

            if (jammingNodes.size() > 0){
                // defer
                n.GALTIERseq.clear();

                if (WSN.debug){
                    System.out.println("\tNode " + n.getId() + " will exit contention.");
                }
                // the round is finished for this node, next transmission attempt in the next round
                n.CONTIaddRound();

                n.setColor(Color.blue);
                // determine time until start of new transmission round
                double remainingTime =  (maxSlots - n.GALTIERcounter) * WSN.CONTIslotTime + WSN.txTime + WSN.SIFS + WSN.tACK;
                // schedule beginning of a new tx round
                scheduler.schedule(new StartRound(n, time + remainingTime));
            }
            else{
                n.GALTIERseq.add(0);
                int length = n.GALTIERseq.size();
                int counter = 0;
                for (int i = 0; i < length; i++) {
                    int v = n.GALTIERseq.get(i);
                    counter += v * (int) Math.pow(2, length-i-1);
                }
                n.GALTIERidx = counter;
                if (WSN.debug){
                    System.out.println("Galtier sequence = "+ n.GALTIERseq);
                    System.out.println("Galtier index: " + n.GALTIERidx );
                }

                // no other node is listening...
                if (n.GALTIERcounter == maxSlots){

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


            n.GALTIERseq.add(1);
            int length = n.GALTIERseq.size();
            int counter = 0;
            for (int i = 0; i < length; i++) {
                int v = n.GALTIERseq.get(i);
                counter += v * (int) Math.pow(2, length-i-1);
            }
            n.GALTIERidx = counter;

            if (WSN.debug){
                System.out.println("Galtier sequence = "+ n.GALTIERseq);
                System.out.println("Galtier index: " + n.GALTIERidx );
            }


            if (WSN.debug){
                System.out.println("\tNode " + n.getId() + " is jamming.");
            }

            if (n.GALTIERcounter == maxSlots){
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
        return "["+this.time+"][EndContentionSlot " + (n.GALTIERcounter+1) + "/" + WSN.galtierP.size() + "][Node " + n.getId() + "]";
    }
}
