package events;

import WSN.*;
/**
 * Created by Gianluca on 17/07/2017.
 */
public class PacketArrivalEvent extends Event {

    private Node destination;

    public PacketArrivalEvent(Node n, Node destination, double time, double eventIndex){
        super(n, time, eventIndex, WSN.normColor);
        this.destination = destination;
    }

    public String toString(){
        return "[" + time + "][PacketArrival] from node " +  this.n;
    }

    public  void run(double currentEventIndex){

        super.run(currentEventIndex);

        // add packet to queue if sensor is awake
        if (n.getStatus() != WSN.NODE_STATUS.SLEEPING){
            Packet p = new Packet(n, destination);
            n.enqueuePacket(p);
        }

        // get new destination

        // schedule new packet arrival at time given by new Poisson RV
        PacketArrivalEvent e = new PacketArrivalEvent(this.n, this.n, time + WSN.getPoisson(WSN.meanInterarrivalTime), currentEventIndex );
        WSN.eventList.add(e);
    }

    public Node getDestination(){
        return this.destination;
    }
}
