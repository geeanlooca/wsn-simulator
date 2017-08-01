package WSN;

import events.*;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {

    public enum NODE_STATUS {
      SLEEPING, TRANSMITTING, IDLING, RECEIVING, LISTENING
    };

    public enum CHANNEL_STATUS{
        FREE, BUSY
    };

    public static CHANNEL_STATUS status;

    public static Color txColor = Color.magenta;
    public static Color normColor = Color.blue;
    public static Color sleepColor = Color.pink;
    public static Color listenColor = Color.cyan;

    public static double txTime = 200; // microseconds
    public static double meanInterarrivalTime = 20.0;
    public static double meanBackoff = 200.0;
    public static double sleepTime = 50.0;

    public static double normSize = 10;
    public static double txSize = 20;
    public static long sleepDelay = 1000;

    public static double SIFS = 10;
    public static double DIFS = 50;
    public static double tSlot = 20;
    public static double tACK = 20;

    public static int CWmin = 15;
    public static int CWmax = 1023;
    public static double tPLC = 192;

    public static double currentEventIndex;


    public static double getPoisson(double mean) {
        Random r = new Random();
        double L = Math.exp(-mean);
        double k = 0.0;
        double p = 1.0;
        do {
            p = p * r.nextDouble();
            k++;
        } while (p > L);
        return k - 1;

    }


    private List<Node> nodes;
    public static Queue<events.Event> eventList;
    public static List<Node> trasmittingNodes;
    public static List<Node> listeningNodes;

    public WSN(int nodeCount, double width, double height){

        Random r = new Random();
        this.nodes = new LinkedList<>();

        Comparator<events.Event> comparator = new EventComparator();

//        WSN.eventList = new PriorityQueue<>((a,b) -> a.getTime() < b.getTime() ? -1 : a.getTime() == b.getTime() ? 0 : 1);
        WSN.eventList = new PriorityQueue<>(comparator);

        WSN.trasmittingNodes = new LinkedList<>();
        WSN.listeningNodes = new LinkedList<>();
        WSN.status = CHANNEL_STATUS.FREE;
        WSN.currentEventIndex = 0;


        for (int i = 0; i < nodeCount; i++) {
            double X = width * r.nextDouble();
            double Y = height * r.nextDouble();
            Node n = new Node(i,X,Y);
            nodes.add(n);

            //PacketArrivalEvent e = new PacketArrivalEvent(n, n, getPoisson(meanInterarrivalTime));

            currentEventIndex ++;
            //WSN.printEventIndex();

            eventList.add(new StartListeningEvent(n,0, currentEventIndex));


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

            currentEventIndex ++;

            events.Event e = eventList.remove();
            e.run(WSN.currentEventIndex);
            //WSN.printEventIndex();

            System.out.println("Number of transmitting nodes: " + trasmittingNodes.size() + "\n\n");
        }

    }

    public static void printEventIndex(){
        System.out.println("Current Event Index: " + WSN.currentEventIndex);
    }


    public class EventComparator implements Comparator<events.Event>
    {
        //a.getTime() < b.getTime() ? -1 : a.getTime() == b.getTime() ? 0 : 1);

        @Override
        public int compare(events.Event a, events.Event b) {

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
        }



    }
    }


