package events;

import WSN.Node;
import WSN.WSN;
import WSN.Packet;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

/**
 * Created by gianluca on 28/07/17.
 */

public class CheckChannelStatus extends Event{

    private double duration;

    public CheckChannelStatus(Node n, double time, int eventIndex, double duration){
        super(n, time, eventIndex, WSN.listenColor);
        this.duration = duration;
    }

    public int run(int currentEventIndex){
        super.run(currentEventIndex);

        if (n.freeChannel){
            if (WSN.print){ System.out.println("Channel has been free for: " + duration);}
            if (duration == WSN.tSlot){

                this.n.addContSlot();
                this.n.addtSlot();

                // decrease BO counter
                int bo = n.decreaseCounter();
                if (WSN.print){ System.out.println("BO Counter decreased: " + bo);};

                if (bo > 0){
                    WSN.eventList.add(new CheckChannelStatus(n,time + WSN.tSlot, currentEventIndex, WSN.tSlot));


                }else{
                    // transmit
                    if (WSN.print){ System.out.println("-> This node will now start transmitting.");};

                    n.addTransmission();

                    WSN.listeningNodes.remove(n);
                    Packet p = new Packet(n, n);
                    WSN.eventList.add(new StartTxEvent(n, p, time, currentEventIndex));
                }
            }
            else if (duration == WSN.DIFS){
                // restart BO counter
                if (n.getBOcounter() == 0){
                    if (WSN.print){ System.out.println("-> This node will now start transmitting.");};
                    // transmit

                    n.addTransmission();
                    n.addDIFS();

                    WSN.listeningNodes.remove(n);
                    Packet p = new Packet(n, n);
                    WSN.eventList.add(new StartTxEvent(n, p, time, currentEventIndex));
                }else {
                    WSN.eventList.add(new CheckChannelStatus(n, time + WSN.tSlot, currentEventIndex, WSN.tSlot));
                }
            }
        }
        else{ this.n.remExtra(time);}

        return 0;
    }

    @Override
    public String toString(){
        return "[" + time + "][CheckChannelStatus] from node " +  this.n;
    }
}
