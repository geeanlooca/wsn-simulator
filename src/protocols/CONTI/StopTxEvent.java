package protocols.CONTI;

import WSN.Packet;
import WSN.Scheduler;
import WSN.WSN;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends protocols.Event {


    private Packet p;

    public StopTxEvent(protocols.CONTI.StartTxEvent e, double time){
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
        scheduler.schedule(new StartRound(n, time));
        this.n.setSize(WSN.normSize);
    }

    public Packet getPacket(){
        return this.p;
    }
}
