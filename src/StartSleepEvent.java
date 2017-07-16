/**
 * Created by Gianluca on 17/07/2017.
 */
public class StartSleepEvent extends Event {
    public StartSleepEvent(int id, Node n, double time){
        super(id, n, time, WSN.sleepColor);
    }

    public void run(){
        super.run();
    }


    @Override
    public String toString(){
        return "[" + time + "][StartSleepEvent] from node " +  this.n;
    }
}
