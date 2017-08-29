package protocols.CONTI;

import WSN.Packet;
import WSN.Scheduler;
import WSN.WSN;
import events.Event;
import protocols.CONTI.StopTxEvent;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends Event {


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

        if (n.collided){
            if (WSN.debug) { System.out.println("  Transmission unsuccessful!!"); }
            n.addCollision();
            n.CONTIaddRound();

        }else{
            if (WSN.debug) { System.out.println("  Transmission successful!"); }
            n.CONTIsetTotalTime();
        }
        n.collided = false;

        Scheduler scheduler = Scheduler.getInstance();
        scheduler.schedule(new StartRound(n, time));
        this.n.setSize(WSN.normSize);
    }

    public Packet getPacket(){
        return this.p;
    }
}
