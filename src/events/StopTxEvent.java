package events;
import WSN.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends events.Event {

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
        n.setStatus(WSN.NODE_STATUS.IDLING);
        Random r = new Random();

        this.n.addTXtime();             // add txTime to the total packet transmission time


        if (n.collided){

            if (WSN.debug){ System.out.println("Tranmission unsuccessful");
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); }

            this.n.addCollision();               // increment collision counter of this node
            this.n.resetContSlot();              // reset the contention slot counter of this node (this round is finished)

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

            n.dequeue();

            this.n.storeContSlotNumber();           // save the contention slot counter (this round is successfully finished)
            this.n.setTotalTime(time);              // save the overall packet transmission time (useful to throughput and delay)

            // start new round after SIFS + tACK
            scheduler.schedule(new StartListeningEvent(n,time + WSN.tACK + WSN.SIFS));
        }

        LinkedList<Node> transmittingNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.TRANSMITTING);
        LinkedList<Node> listeningNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.LISTENING);

        if (transmittingNodes.isEmpty()) {
            //WSN.status = WSN.CHANNEL_STATUS.FREE;           // Useless parameter, I can check the channel status looking at the number of transmitting nodes in the local range

            if (WSN.debug) {
                for (Node entry : listeningNodes) {
                    System.out.println("(this) listening Node " + entry.getId());
                }
            }

            // procedure to track the listening nodes that were stopped during a collided packet transmission. If there was a collision I have to reschedule events (resume)
            //  for all the nodes that were listening in all the collision events.

            if (!n.collidedNodes.isEmpty()) {

                if (WSN.debug) {
                    for (Node entry : n.collidedNodes) {
                        System.out.println("-> Collision with Node: " + entry.getId());
                    }
                }

                ArrayList<Node> collidedNodeSave = new ArrayList<Node>();
                collidedNodeSave.addAll(n.collidedNodes);

                while (!n.collidedNodes.isEmpty()) {

                    ArrayList<Node> resumingNodes = n.collidedNodes.remove(0).resumingNodes;

                    while (!resumingNodes.isEmpty()) {
                        Node listeningNode = resumingNodes.remove(0);
                        listeningNodes.add(listeningNode);
                        if (WSN.debug) {
                            System.out.println("Old listening Node: " + listeningNode.getId());
                        }
                    }
                }

                listeningNodes = removeDuplicate(listeningNodes);
                listeningNodes = removeOldCollided(listeningNodes, collidedNodeSave);

                if (WSN.debug) {
                    System.out.println("Final listening nodes to be resumed: ");
                    for (Node entry : listeningNodes) {
                        System.out.println(" Node " + entry.getId());
                    }
                }
            }
            // reschedule CheckChannelStatus event for all the stopped listening nodes. Pay attention to the order on which events are rescheduled.
            for (Node listening : listeningNodes) {

                    if (!listening.freeChannel) {       //if true means already scheduled by a previous Node.

                        if ((!n.collided) && (listening.lastBOstopped.getId() == this.n.getId())) {     // If transmission succeeds only the last Node that stops the BO counter for this node can resume it
                                                                                                                // in order to avoid that resuming happen before necessary.
                            scheduler.schedule(new CheckChannelStatus(listening, time + WSN.DIFS, WSN.DIFS));
                            listening.freeChannel = true;
                            listening.resetContSlot();      //  reset the contention slot counter for all the listening nodes (the round is finished)
                            if(WSN.debug) { System.out.println("->CheckChStatus rescheduled for Node "+listening.getId()); }
                        }
                        else if (n.collided){       // if collision occurs the BO resuming is already handled by the previous procedure

                            scheduler.schedule(new CheckChannelStatus(listening, time + WSN.DIFS, WSN.DIFS));
                            listening.freeChannel = true;
                            listening.resetContSlot();
                            if(WSN.debug) { System.out.println("->CheckChStatus rescheduled for Node "+listening.getId()); }
                        }
                    }
            }
        }
        else{
            this.n.resumingNodes.addAll(listeningNodes);
            for (Node entry : listeningNodes){
                if (WSN.debug) { System.out.println("To be resumed, Node: "+entry.getId()); }
            }
        }

       // n.setStatus(WSN.NODE_STATUS.IDLING);
    }

    public Packet getPacket(){
        return this.p;
    }

    private LinkedList<Node> removeDuplicate (LinkedList<Node> list){

        for (int i=0; i<list.size(); i++){
           for (int j =i+1; j<list.size(); j++){
               if (list.get(j).getId()==list.get(i).getId()){
                   if (WSN.debug) { System.out.println("Removed from the list because duplicated, Node "+list.get(j).getId()); }
                   list.remove(j);
               }
           }
        }
        return list;
    }

    private LinkedList<Node> removeOldCollided (LinkedList<Node> list, ArrayList<Node> collidedNodes) {

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < collidedNodes.size(); j++) {
                if (list.get(i).getId() == collidedNodes.get(j).getId() || list.get(i).getId() == this.n.getId()) {
                    if (WSN.debug) { System.out.println("Removed from the list because collided, Node "+list.get(i).getId()); }
                    list.remove(i);
                    break;
                }
            }
        }
        return list;
    }
}
