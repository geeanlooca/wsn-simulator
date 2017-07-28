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

    public CheckChannelStatus(Node n, double time, double duration){
        super(n, time, WSN.listenColor);
        this.duration = duration;
    }

    public void run(){
        super.run();

        if (n.freeChannel){
            System.out.println("Channel has been free for: " + duration);
            if (duration == WSN.tSlot){
                // decrease BO counter
                int bo = n.decreaseCounter();
                System.out.println("BO Counter decreased: " + bo);

                if (bo > 0){
                    WSN.eventList.add(new CheckChannelStatus(n, time + WSN.tSlot, WSN.tSlot));
                }else{
                    // transmit
                    System.out.println("This node will now start transmitting.");
                    WSN.listeningNodes.remove(n);
                    Packet p = new Packet(n, n);
                    WSN.eventList.add(new StartTxEvent(n, p, time));
                }
            }
            else if (duration == WSN.DIFS){
                // restart BO counter
                if (n.getBOcounter() == 0){
                    System.out.println("This node will now start transmitting.");
                    // transmit
                    WSN.listeningNodes.remove(n);
                    Packet p = new Packet(n, n);
                    WSN.eventList.add(new StartTxEvent(n, p, time));
                }else {
                    WSN.eventList.add(new CheckChannelStatus(n, time + WSN.tSlot, WSN.tSlot));
                }
            }
        }
    }

    @Override
    public String toString(){
        return "[" + time + "][CheckChannelStatus] from node " +  this.n;
    }
}
