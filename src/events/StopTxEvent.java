package events;
import WSN.*;

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
        this.n.setSize(WSN.normSize);
        WSN.trasmittingNodes.remove(n);
        n.setStatus(WSN.NODE_STATUS.IDLING);

        // go to sleep
        WSN.eventList.add(new StartSleepEvent(n, time + WSN.getPoisson(5)));
    }

    public Packet getPacket(){
        return this.p;
    }
}
