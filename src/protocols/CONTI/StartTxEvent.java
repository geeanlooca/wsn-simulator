package protocols.CONTI;

import WSN.Node;
import WSN.Packet;
import WSN.Scheduler;
import WSN.WSN;
import events.Event;
import protocols.CONTI.StopTxEvent;

import java.util.LinkedList;

/**
 * Created by Gianluca on 24/08/2017.
 */
public class StartTxEvent extends Event {

    private Packet p;

    public StartTxEvent(Node n, Packet p, double time){
        super(n, time, WSN.txColor);
        this.p = p;
    }

    @Override
    public String toString(){
        return "[" + time + "][StartTxEvent][Node " +  this.n.getId()+"]";
    }



    public void run() {
        super.run();

        Scheduler scheduler = Scheduler.getInstance();

        // keep track of the nodes that start a transmission (useful to Fairness calculation)
        WSN.nodeTrace.add(this.n);
        n.addTransmission();

        LinkedList<Node> transmitting = WSN.getNeighborsStatus(n, WSN.NODE_STATUS.TRANSMITTING);
        if (transmitting.size() > 0){
            // collision occurs
            n.collided = true;
        }

        scheduler.schedule(new StopTxEvent(this, time + WSN.txTime));

    }

    public Packet getPacket(){
        return this.p;
    }
}
