import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {


    public static Color txColor = Color.magenta;
    public static Color normColor = Color.blue;
    public static Color sleepColor = Color.pink;

    public static double txTime = 5; // microseconds
    public static double meanInterarrivalTime = 100;
    public static double meanBackoff = 200;
    public static double sleepTime = 50;

    public static double normSize = 10;
    public static double txSize = 20;
    public static long sleepDelay = 0;

    public static int getPoisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }


    private List<Node> nodes;
    private List<Event> eventList;
    private List<Node> trasmittingNodes;

    public WSN(int nodeCount, double width, double height){

        Random r = new Random();
        this.nodes = new LinkedList<>();
        this.eventList = new LinkedList<>();
        this.trasmittingNodes = new LinkedList<>();

        for (int i = 0; i < nodeCount; i++) {
            double X = width * r.nextDouble();
            double Y = height * r.nextDouble();
            Node n = new Node(i,X,Y);
            nodes.add(n);

            StartTxEvent e = new StartTxEvent(i, n , getPoisson(meanInterarrivalTime));
            eventList.add(e);
            eventList.add(new StopTxEvent(i, e, e.time+WSN.txTime));
            eventList.sort((a,b) -> a.getTime() < b.getTime() ? -1 : a.getTime() == b.getTime() ? 0 : 1);
        }

    }

    public List<Node> getNodes(){
        return nodes;
    }

    public int nodeCount(){
        return this.nodes.size();
    }


    public void run(){
        Random r = new Random();

        while (!eventList.isEmpty()){

            try
            {
                Thread.sleep(sleepDelay);
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }

            Event e = eventList.get(0);
            eventList.remove(e);

            System.out.println(e);
            e.run();


            if (e instanceof StartTxEvent){
                trasmittingNodes.add(e.getNode());
            }

            if (e instanceof StopTxEvent ){
                Node n = e.getNode();

                trasmittingNodes.remove(n);

                StartTxEvent newTx = new StartTxEvent(0, n, e.time + getPoisson(meanInterarrivalTime));
                StopTxEvent stopTx = new StopTxEvent(0, newTx, newTx.time + WSN.txTime);

                eventList.add(newTx);
                eventList.add(stopTx);
                eventList.sort((a,b) -> a.getTime() < b.getTime() ? -1 : a.getTime() == b.getTime() ? 0 : 1);
            }

            System.out.println("Number of transmitting nodes: " + trasmittingNodes.size());
        }

    }
}
