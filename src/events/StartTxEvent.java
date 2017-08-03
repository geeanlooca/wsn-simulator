package events;
import WSN.*;
/**
 * Created by Gianluca on 16/07/2017.
 */
public class StartTxEvent extends events.Event {

    private Packet p;

    public StartTxEvent(Node n, Packet p, double time, int  eventIndex){
        super(n, time, eventIndex, WSN.txColor);
        this.p = p;
    }

    @Override
    public String toString(){
        return "[" + time + "][StartTxEvent] from node " +  this.n;
    }


    public int run(int currentEventIndex){
        super.run(currentEventIndex);
        this.n.setSize(WSN.txSize);

        if (WSN.trasmittingNodes.isEmpty()){
            // no collision
            WSN.trasmittingNodes.add(n);
            n.collided = false;
        }else{
            WSN.trasmittingNodes.add(n);
            for (Node t : WSN.trasmittingNodes) {
                t.collided = true;

            }
        }

        WSN.listeningNodes.remove(n);
        n.setStatus(WSN.NODE_STATUS.TRANSMITTING);
        WSN.eventList.add(new StopTxEvent(this, time + WSN.txTime, currentEventIndex));


        // there's a bug here. if another node's BO counter reaches 0 at the same time and this event is
        // extracted from the queue before the CheckChannelStatus event from the node with BO = 0 then
        // the collision will not occur since that node's freeChannel will be set to false and the BO will not be
        // decreased (1 -> 0) and a new transmission will not start
        // SOLUTION: modify the priority queue in order to extract older events if more than one event with the
        // same time is present. In this way we first decrease all the BO counters and then start the transmission
        // for all those with BO = 0. -> SOLVED
        for (Node listening : WSN.listeningNodes) {

            if (WSN.print){ System.out.println("\tNode " + listening.getId() + " stopped its B0 counter.");}
            listening.freeChannel = false;

        }
        return 0;
    }

    public Packet getPacket(){
        return this.p;
    }
}
