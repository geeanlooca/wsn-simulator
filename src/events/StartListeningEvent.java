package events;

import WSN.Node;
import WSN.WSN;
import WSN.Packet;

/**
 * Created by gianluca on 28/07/17.
 */
public class StartListeningEvent extends Event{


    public StartListeningEvent(Node n, double time, double eventIndex){ super(n, time, eventIndex, WSN.listenColor);
    }

    public void run(double currentEventIndex){
        super.run(currentEventIndex);

        System.out.println("Channel is: " + WSN.status + ". BO counter: " + n.getBOcounter());
        n.setStatus(WSN.NODE_STATUS.LISTENING);
        WSN.listeningNodes.add(n);

        if (WSN.status == WSN.CHANNEL_STATUS.FREE){
            n.freeChannel = true;
            WSN.eventList.add(new CheckChannelStatus(n,time+WSN.DIFS, currentEventIndex, WSN.DIFS));
        }
    }

    @Override
    public String toString(){
        return "[" + time + "][StartListeningEvent] from node " +  this.n;
    }
}
