package protocols.DCF;
import WSN.*;
import events.Event;

import java.util.LinkedList;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StartTxEvent extends Event {


    public StartTxEvent(Node n, double time){
        super(n, time, WSN.txColor);
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
                t.collidedNodes.add(this.n);
            }
                // save the collided nodes
            while (!transmittingNodes.isEmpty()){
                Node node = transmittingNodes.remove();
                n.collidedNodes.add(node);
                if (WSN.debug){ System.out.println("Collision Node "+node.getId()); }
            }

        }



        n.setStatus(WSN.NODE_STATUS.TRANSMITTING);

        LinkedList<Node> listeningNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.LISTENING);

        scheduler.schedule(new StopTxEvent(this, time + WSN.txTime, listeningNodes));



        for (Node listening : listeningNodes) {

            if (WSN.debug){ System.out.println("\tNode " + listening.getId() + " stopped its B0 counter.");}
            listening.freeChannel = false;
            // save the node that force the backoff freezing to discriminate among multiple backoff resumes
            listening.lastBOstopped = this.n;

        }
    }

}
