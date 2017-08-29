package events;

import WSN.WSN;
import WSN.Node;
import WSN.RNG;
import WSN.Scheduler;
import WSN.Channel;


/**
 * Created by Gianluca on 25/08/2017.
 */
public class UpdatePosition extends Event {

    private int mobilityID;

    public UpdatePosition(double time, int mobilityID){
        super(time);
        this.mobilityID = mobilityID;
    }

    public void run(){

        Scheduler sc = Scheduler.getInstance();

        // set the type of mobility
        // 0 - gaussian noise
        // 1 - Gauss-Markov model
        // 2 - NO mobility


        // update the node position and clear current neighbor list
        for (Node n : WSN.nodes) {
            n.move(mobilityID);
            n.clearNeighbors();

        }

        // update the node neighbors
        for (Node nodeA : WSN.nodes) {
            // find the neighbors based on the received power level
            for (Node nodeB : WSN.nodes) {
                if (nodeB.getId() != nodeA.getId()) {
                    Channel channel = new Channel(nodeA, nodeB, WSN.Ptx, WSN.indoor);

                    double Prx = channel.getPrx();
                    //System.out.println(Prx);

                    if (Prx >= WSN.PrxThreshold && !(nodeB.findNeighbor(nodeA))) {
                        nodeA.addNeighbor(nodeB);
                        nodeB.addNeighbor(nodeA);
                    }
                }
            }
        }

        if (WSN.debug) { WSN.printNeighbors(); }

        // schedule the new position update
        sc.schedule(new UpdatePosition(time + 1000, mobilityID));
    }

    @Override
    public String toString(){
        return "[" + time + "][UpdatePosition]";
    }
}