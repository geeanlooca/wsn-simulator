package events;
import WSN.*;

import java.util.LinkedList;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StartTxEvent extends events.Event {

    private Packet p;

    public StartTxEvent(Node n, Packet p, double time){
        super(n, time, WSN.txColor);
        this.p = p;
    }

    @Override
    public String toString(){
        return "[" + time + "][StartTxEvent] from node " +  this.n;
    }


    public void run(){
        super.run();

        n.setStatus(WSN.NODE_STATUS.TRANSMITTING);


        Scheduler scheduler = Scheduler.getInstance();
        this.n.setSize(WSN.txSize);

        LinkedList<Node> transmittingNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.TRANSMITTING);
        if (transmittingNodes.isEmpty()){
            // no collision
            n.collided = false;
        }else{
            // collision
            n.collided = true;
            for (Node t : transmittingNodes) {
                t.collided = true;
                while (!transmittingNodes.isEmpty()){       // save the collided nodes
                    Node node = transmittingNodes.remove();
                    n.collidedNodes.add(node);
                    if (WSN.debug){ System.out.println("Collision Node "+node.getId()); }
                }
            }
        }


        n.setStatus(WSN.NODE_STATUS.TRANSMITTING);
        scheduler.schedule(new StopTxEvent(this, time + WSN.txTime));


        LinkedList<Node> listeningNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.LISTENING);

        for (Node listening : listeningNodes) {

            if (WSN.debug){ System.out.println("\tNode " + listening.getId() + " stopped its B0 counter.");}
            listening.freeChannel = false;

            listening.lastBOstopped = this.n;       // save the Node that freezes the backoff to discriminate among multiple backoff resumes

        }
    }

    public Packet getPacket(){
        return this.p;
    }
}
