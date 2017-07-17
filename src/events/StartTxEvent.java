package events;
import WSN.*;
/**
 * Created by Gianluca on 16/07/2017.
 */
public class StartTxEvent extends events.Event {

    private Packet p;

    public StartTxEvent(Node n, Packet p, double time){
        super(n, time, WSN.txColor);
        this.p = p;
    }

    @Override
    public String toString(){
        return "[" + time + "][StartTxEvent] from node " +  this.n;
    }


    public void run(){
        super.run();
        this.n.setSize(WSN.txSize);
        WSN.trasmittingNodes.add(n);
        n.setStatus(WSN.NODE_STATUS.TRANSMITTING);

        WSN.eventList.add(new StopTxEvent(this, time + WSN.txTime));

    }

    public Packet getPacket(){
        return this.p;
    }
}
