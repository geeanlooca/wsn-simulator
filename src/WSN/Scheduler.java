package WSN;

import events.Event;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by gianluca on 18/08/17.
 */
public final class Scheduler {

    private static volatile Scheduler instance = null;
    private Queue<ExtendedEvent> queue;
    private long currentIndex;

    class ExtendedEvent{
        Event e;
        long index;

        ExtendedEvent(Event e, long index){
            this.e = e;
            this.index = index;
        }

        double getTime(){
            return e.getTime();
        }

        double getEventIndex(){
            return this.index;
        }

        Event getEvent(){
            return this.e;
        }
    }

    private Scheduler(){

        currentIndex = 0;
        queue = new PriorityQueue<>((a,b)->{
            if (a.getTime() < b.getTime()) {
                return -1;
            }
            else if (a.getTime() > b.getTime()) {
                return 1;
            }
            else {
                if (a.getEventIndex() < b.getEventIndex()) {
                    return -1;
                } else if (a.getEventIndex() > b.getEventIndex()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public static Scheduler getInstance(){
        if (instance == null){
            synchronized (Scheduler.class){
                if (instance == null){
                    instance = new Scheduler();
                }
            }
        }

        return instance;
    }

    public void schedule(Event e){
        this.queue.add(new ExtendedEvent(e, this.currentIndex++));
    }

    public Event remove(){
        return this.queue.remove().getEvent();
    }

    public boolean isEmpty(){
        return this.queue.isEmpty();
    }

    public int size(){
        return this.queue.size();
    }
}
