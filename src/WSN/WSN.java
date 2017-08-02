package WSN;

import events.*;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {

    // --------- MAIN SIMULATION PARAMETERS ----------//

    final int nodeCount = 5;                // number of nodes in the network
    final long sleepDelay = 0;              // delay used to extract events
    final double maxIndex = 100000000;        // max available number of events; used to exit the script and print results (use Double.POSITIVE_INFINITY to never exit) 1000000000
    public static boolean print = false;    // printing of extra information useful for debugging


    // ------------------------------------//



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

    public static double SIFS = 10;
    public static double DIFS = 50;
    public static double tSlot = 20;
    public static double tACK = 20;

    public static int CWmin = 15;
    public static int CWmax = 1023;
    public static double tPLC = 192;

    public static int currentEventIndex;

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

    private static List<Node> nodes;        // why not static? it is a problem if it is set to static?
    public static Queue<events.Event> eventList;
    public static List<Node> trasmittingNodes;
    public static List<Node> listeningNodes;

    public WSN(double width, double height){

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

        while ((!eventList.isEmpty()) && (currentEventIndex < maxIndex)){

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
            int shift = e.run(WSN.currentEventIndex);

            currentEventIndex += shift;
           // WSN.printEventIndex();

            if (print){ System.out.println("Number of transmitting nodes: " + trasmittingNodes.size() + "\n\n"); }

        }

        WSN.printCollisionRate();
        WSN.printSlotNumber();
        WSN.printThroughput();

        System.exit(0);
    }

    public class EventComparator implements Comparator<events.Event>
    {
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

    public static void printEventIndex(){
        System.out.println("Current Event Index: " + WSN.currentEventIndex);
    }


    public static void printCollisionRate(){

        double collRate = 0;
        double avCollRate =0;
        double numb = WSN.nodes.size();

        System.out.println("\n Node ||  Coll/Transm  ||  Collision Rate ");

        for (Node node : WSN.nodes) {
            collRate = ((double)node.getCollisionParam()[0])/((double)node.getCollisionParam()[1]);
            avCollRate = avCollRate + collRate / numb;
            System.out.println(node.getId() + "\t\t\t" + node.getCollisionParam()[0] + " / " + node.getCollisionParam()[1] + "\t\t\t\t"+ collRate);
        }
        System.out.println("\n Average Collision Rate = " +avCollRate);

    }

    public static void printSlotNumber(){

        ArrayList<Integer> slotNumberList;
        double allAverageSlotNumber =0;
        double numb = WSN.nodes.size();

        System.out.println("\n Node ||  Average # of Contention Slots to successful transmit ");

        for (Node node : WSN.nodes) {
            slotNumberList = node.getSlotCounterList();
            if(print){System.out.println(node.getId() + "\t\t" + slotNumberList.toString());}
            double avSlotNumber = calculateAverage(slotNumberList);
            allAverageSlotNumber +=  avSlotNumber / numb;

            System.out.println(node.getId() + "\t\t\t\t" + avSlotNumber);
        }
        System.out.println("\n Average Number of Contention Slot = " +allAverageSlotNumber);

    }

    public static void printThroughput(){

        ArrayList<Double> totalTimeList;
        double allAvThroughput =0;
        double numb = WSN.nodes.size();

        System.out.println("\n Node ||  Av. Total Time to success. delivery || Av. Throughput  ");

        for (Node node : WSN.nodes) {

            totalTimeList = node.getTotalTimeList();
            //System.out.println(node.getId() + "\t\t" + totalTimeList.toString());
            double avTotalTime = calculateAverageDouble(totalTimeList);
            double avThroughput = (WSN.DIFS + WSN.txTime + WSN.tACK + WSN.SIFS)/ avTotalTime;
            allAvThroughput +=  avThroughput / numb;
            System.out.println(node.getId() + "\t\t\t\t" +  avTotalTime+ "\t\t\t\t" +  avThroughput);

        }
        System.out.println("\n Average Throughput = " +allAvThroughput);

    }


    private static double calculateAverage(List <Integer> list) {
        Integer sum = 0;
        if(!list.isEmpty()) {
            for (Integer entry : list) {
                sum += entry;
            }
            return sum.doubleValue() / list.size();
        }
        return sum;
    }

    private static double calculateAverageDouble(List <Double> list) {
        double sum = 0;
        if(!list.isEmpty()) {
            for (double entry : list) {
                sum += entry;
            }
            return sum / list.size();
        }
        return sum;
    }

}


