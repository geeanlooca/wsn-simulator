package protocols.CONTI;

import WSN.Node;
import WSN.WSN;
import WSN.Packet;
import protocols.Event;
import WSN.Scheduler;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Gianluca on 23/08/2017.
 */
public class EndContentionSlot extends Event {

    public EndContentionSlot(Node n, double time){ super(n, time, WSN.listenColor); }

    public void run(){
        // obtain Scheduler instance
        Scheduler scheduler = Scheduler.getInstance();

        n.CONTIslotNumber++;
        int maxSlots = n.CONTIp.length;


        // determine if node goes on with the contention or defers
        if (n.getStatus() == WSN.NODE_STATUS.LISTENING){
            ArrayList<Node> jammingNodes = WSN.getNeighborsStatus(n, WSN.NODE_STATUS.JAMMING);

            if (jammingNodes.size() > 0){
                // defer

                n.setColor(Color.blue);
                // determine time until start of new transmission round
                double remainingTime =  (maxSlots - n.CONTIslotNumber) * WSN.CONTIslotTime + WSN.txTime;
                // schedule beginning of a new tx round
                scheduler.schedule(new StartRound(n, time + remainingTime));
            }
            else{
                // no other node is listening...
                if (n.CONTIslotNumber == maxSlots){
                    // but this is the last contention slot: transmit!
                    n.setStatus(WSN.NODE_STATUS.TRANSMITTING);
                    scheduler.schedule(new StartTxEvent(n, new Packet(n,n), time));

                }else{
                    scheduler.schedule(new StartContentionSlot(n, time));
                }
            }
        }else if (n.getStatus() == WSN.NODE_STATUS.JAMMING){
            if (n.CONTIslotNumber == maxSlots){
                // transmission starts if node is still in contention
                scheduler.schedule(new StartTxEvent(n, new Packet(n,n), time));
            }else{
                scheduler.schedule(new StartContentionSlot(n, time));
            }

        }
    }

    @Override
    public String toString(){
        return "";
    }
}
