package protocols.DCF;
import WSN.*;
import events.Event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends Event {


    public StopTxEvent(StartTxEvent e, double time){
        super(e.getNode(), time, WSN.normColor);
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
        // add txTime to the total packet transmission time
        this.n.addTXtime();


        if (n.collided){

            if (WSN.debug){ System.out.println("Tranmission unsuccessful");
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); }

            // increment collision counter of this node
            this.n.addCollision();
            // reset the contention slot counter of this node (this round is finished)
            this.n.resetContSlot();

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

            // save the contention slot counter (this round is successfully finished)
            this.n.storeContSlotNumber();
            // save the overall packet transmission time (useful to throughput and delay)
            this.n.setTotalTime(time);

            // start new round after SIFS + tACK
            scheduler.schedule(new StartListeningEvent(n,time + WSN.tACK + WSN.SIFS));
        }

        LinkedList<Node> transmittingNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.TRANSMITTING);
        LinkedList<Node> listeningNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.LISTENING);

        // At the end of the StopTxEvent new CheckChannelEvents must be rescheduled for all the listening nodes (that have stopped the BO during the startTXEvent). However
        //  if a collision occurs the whole rescheduling has to happen during the stopTXEvent associated to the last collided node, in order to avoid duplicated events and unwanted behaviors.
        if (transmittingNodes.isEmpty()) {

            if (WSN.debug) {
                for (Node entry : listeningNodes) {
                    System.out.println("(this) listening Node " + entry.getId());
                }
            }

            // procedure to track the listening nodes that were stopped during a collided packet transmission and resume them in the correct way.
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

                    // check if the rescheduled is already happened.
                    if (!listening.freeChannel) {
                        // If the transmission succeeds only the last Node that stops the BO counter for this node can resume it.
                        if ((!n.collided) && (listening.lastBOstopped.getId() == this.n.getId())) {

                            scheduler.schedule(new CheckChannelStatus(listening, time + WSN.DIFS, WSN.DIFS));
                            listening.freeChannel = true;
                            //  reset the contention slot counter for all the listening nodes (the round is finished)
                            listening.resetContSlot();
                            if(WSN.debug) { System.out.println("->CheckChStatus rescheduled for Node "+listening.getId()); }
                        }
                        // if collision occurs the BO resuming is already handled by the previous procedure thus I can schedule for the current listening nodes list.
                        else if (n.collided){

                            scheduler.schedule(new CheckChannelStatus(listening, time + WSN.DIFS, WSN.DIFS));
                            listening.freeChannel = true;
                            //  reset the contention slot counter for all the listening nodes (the round is finished)
                            listening.resetContSlot();
                            if(WSN.debug) { System.out.println("->CheckChStatus rescheduled for Node "+listening.getId()); }
                        }
                    }
            }
        }
        else{
            // if this is not the StopTxEvent of the last node that collided its listening nodes are saved to be resumed in the last StopTxEvent
            this.n.resumingNodes.addAll(listeningNodes);
            for (Node entry : listeningNodes){
                if (WSN.debug) { System.out.println("To be resumed, Node: "+entry.getId()); }
            }
        }

    }


    private LinkedList<Node> removeDuplicate (LinkedList<Node> list){
        // merging the previous listening nodes and the current listening nodes (that both need rescheduling) it is necessary to remove possible duplicates
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
        // remove from the whole listening nodes possible collided nodes for which it has already been scheduled a CheckChannelEvent.
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
