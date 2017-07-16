import java.awt.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StopTxEvent extends Event {

    public StopTxEvent(int id, StartTxEvent e, double time){
        super(id, e.getNode(), time, WSN.normColor);
    }

    @Override
    public String toString(){
        return "[" + time + "][StopTxEvent] from node " +  this.n;
    }


    public void run(){
        super.run();
        this.n.setSize(WSN.normSize);
    }

}
