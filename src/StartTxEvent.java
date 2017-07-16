import java.awt.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class StartTxEvent extends Event {

    public StartTxEvent(int id, Node n, double time){
        super(id, n, time, WSN.txColor);
    }

    @Override
    public String toString(){
        return "[" + time + "][StartTxEvent] from node " +  this.n;
    }


    public void run(){
        super.run();
        this.n.setSize(WSN.txSize);
    }
}
