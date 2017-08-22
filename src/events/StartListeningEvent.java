package events;

import WSN.Node;
import WSN.WSN;
import WSN.Scheduler;

import java.util.LinkedList;

/**
 * Created by gianluca on 28/07/17.
 */
public class StartListeningEvent extends Event{



    public StartListeningEvent(Node n, double time){ super(n, time, WSN.listenColor);
    }

    public void run(){

        super.run();

        Scheduler scheduler = Scheduler.getInstance();

        if (WSN.debug){ System.out.println("Channel is: " + WSN.status + ". BO counter: " + n.getBOcounter());}
        n.setStatus(WSN.NODE_STATUS.LISTENING);
        //WSN.listeningNodes.add(n);

        LinkedList<Node> transmittingNodes = WSN.getNeighborsStatus(this.n, WSN.NODE_STATUS.TRANSMITTING);

       // if (WSN.status == WSN.CHANNEL_STATUS.FREE){
        if (transmittingNodes.isEmpty()){
            n.freeChannel = true;
            scheduler.schedule(new CheckChannelStatus(n,time+WSN.DIFS, WSN.DIFS));

            this.n.startTXTime(time);            // save transmission initial time (useful to Delay)

        }
    }

    @Override
    public String toString(){
        return "[" + time + "][StartListeningEvent] from node " +  this.n;
    }
}
