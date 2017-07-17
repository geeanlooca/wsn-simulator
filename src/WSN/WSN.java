package WSN;

import events.PacketArrivalEvent;
import events.StartSleepEvent;
import events.StartTxEvent;
import events.StopTxEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import WSN.Node;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {

    public static enum NODE_STATUS {
      SLEEPING, TRANSMITTING, IDLING, RECEIVING
    };

    public static Color txColor = Color.magenta;
    public static Color normColor = Color.blue;
    public static Color sleepColor = Color.pink;

    public static double txTime = 2; // microseconds
    public static double meanInterarrivalTime = 20;
    public static double meanBackoff = 200;
    public static double sleepTime = 50;

    public static double normSize = 10;
    public static double txSize = 20;
    public static long sleepDelay = 50;

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
    public static Queue<events.Event> eventList;
    public static List<Node> trasmittingNodes;

    public WSN(int nodeCount, double width, double height){

        Random r = new Random();
        this.nodes = new LinkedList<>();
        this.eventList = new PriorityQueue<>((a,b) -> a.getTime() < b.getTime() ? -1 : a.getTime() == b.getTime() ? 0 : 1);

        this.trasmittingNodes = new LinkedList<>();

        for (int i = 0; i < nodeCount; i++) {
            double X = width * r.nextDouble();
            double Y = height * r.nextDouble();
            Node n = new Node(i,X,Y);
            nodes.add(n);

            PacketArrivalEvent e = new PacketArrivalEvent(n, n, getPoisson(meanInterarrivalTime));

            eventList.add(e);
            eventList.add(new StartSleepEvent(n, 0));
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

            events.Event e = eventList.remove();
            e.run();

            System.out.println("Number of transmitting nodes: " + trasmittingNodes.size());
        }

    }
}
