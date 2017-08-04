package events;

import WSN.Node;
import WSN.WSN;
import WSN.Packet;

import static WSN.WSN.currentEventIndex;

/**
 * Created by gianluca on 28/07/17.
 */
public class StartListeningEvent extends Event{



    public StartListeningEvent(Node n, double time, int eventIndex){ super(n, time, eventIndex, WSN.listenColor);
    }

    public int run(int currentEventIndex){

        super.run(currentEventIndex);

        if (WSN.print){ System.out.println("Channel is: " + WSN.status + ". BO counter: " + n.getBOcounter());}
        n.setStatus(WSN.NODE_STATUS.LISTENING);
        WSN.listeningNodes.add(n);

        if (WSN.status == WSN.CHANNEL_STATUS.FREE){
            n.freeChannel = true;
            WSN.eventList.add(new CheckChannelStatus(n,time+WSN.DIFS, currentEventIndex, WSN.DIFS));
            this.n.startTXTime(time);
        }
        return 0;

    }

    @Override
    public String toString(){
        return "[" + time + "][StartListeningEvent] from node " +  this.n;
    }
}
