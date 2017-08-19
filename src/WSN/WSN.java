package WSN;

import events.*;
import events.Event;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {

    // --------- MAIN SIMULATION PARAMETERS ----------//

    private int nodeCount;                       // number of nodes in the network
    private long sleepDelay = 0;                      // delay used to extract events
    final double maxIndex = Math.pow(10, 6);        // max available number of events; used to exit the script and debug results (use Double.POSITIVE_INFINITY to never exit) 1000000000
    public static boolean debug = true;            // printing extra information useful for debugging

    final static double maxAvailableThroughput = 11;    // Mb/s
    final static double frameSize = 250;               // bytes

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

    //public static double txTime = 200; // microseconds
    public static double txTime = (frameSize * 8) / (maxAvailableThroughput); // txTime in microsecond

    public static double meanInterarrivalTime = 20.0;
    public static double meanBackoff = 200.0;
    public static double sleepTime = 50.0;

    public static double normSize = 10;
    public static double txSize = 15;

    public static double SIFS = 10;
    public static double DIFS = 50;
    public static double tSlot = 20;
    public static double tACK = 20;

    public static int CWmin = 15;
    public static int CWmax = 1023;
    public static double tPLC = 192;

    private int topologyID;
    private double width, height;

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
    private static List<Node> nodes;

    public static List<Node> trasmittingNodes;
    public static List<Node> listeningNodes;

    public WSN(int nodeCount, double width, double height, int topologyID){

        Random r = new Random();
        nodes = new LinkedList<>();
        this.nodeCount = nodeCount;

        this.width = width;
        this.height = height;

        this.topologyID = topologyID;


        Scheduler scheduler = Scheduler.getInstance();

        WSN.trasmittingNodes = new LinkedList<>();
        WSN.listeningNodes = new LinkedList<>();
        WSN.status = CHANNEL_STATUS.FREE;

        for (int i = 0; i < this.nodeCount; i++) {

            /**
             * changed by William on 14/08/2017.
             * default topology and circular topology added
             */
            double[] coord = nodePosition();

            double X = coord[0];
            double Y = coord[1];

            Node n = new Node(i,X,Y);
            nodes.add(n);

            scheduler.schedule(new StartListeningEvent(n,0));
        }
    }

    private double[] nodePosition()
    {
        Random r = new Random();
        double[] coord = new double[2];

        double a, theta;
        double maxRadius = 0.45 * Math.min(width,height);

        switch (this.topologyID){

            // circular cell
            case 0:
                a = maxRadius * Math.sqrt(r.nextDouble());
                theta = 2 * Math.PI * r.nextDouble();
                coord[0] = width/2 + a * Math.cos(theta);
                coord[1] = height/2 + a * Math.sin(theta);
                break;

            // hexagonal cell
            case 1:
                double c = Math.cos(Math.PI/6);

                do {
                    a = Math.sqrt(r.nextDouble());
                    theta = - Math.PI/6 + Math.PI/3 * r.nextDouble();
                }while((a * Math.cos(theta)) > c);

                a = a * maxRadius;
                theta = theta + Math.PI/3 * r.nextInt(6);

                coord[0] = width/2 + a * Math.cos(theta);
                coord[1] = height/2 + a * Math.sin(theta);
                break;
            default:
                coord[0] = width * r.nextDouble();
                coord[1] = height * r.nextDouble();
                break;
        }

        return coord;
    }
    public List<Node> getNodes(){
        return nodes;
    }

    public int getNodeCount(){
        return nodes.size();
    }

    public int getTopologyID() { return topologyID; };

    public double[] getNetworkSize() {
        double[] size = {width, height};
        return size;
    }

    public int nodeCount(){
        return WSN.nodes.size();
    }

    public void setAnimationDelay(int ms){
        this.sleepDelay = ms;
    }

    public void debugging(boolean enable){
        debug = enable;
    }

    public void run(){
        this.run(Double.POSITIVE_INFINITY);
    }
    public void run(double maxTime){
        Random r = new Random();

        Scheduler scheduler = Scheduler.getInstance();
        double currentTime = 0;

        while ((!scheduler.isEmpty()) && (currentTime < maxTime)){
            try
            {
                Thread.sleep(sleepDelay);
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }


            Event e = scheduler.remove();
            currentTime += e.getTime();

            if (debug){
                System.out.println(e);
                //System.out.println("Number of transmitting nodes: " + trasmittingNodes.size());
            }
            e.run();

            if (debug){
                System.out.println("\n");
            };

        }

        WSN.printCollisionRate();
        WSN.printSlotNumber();
        WSN.printThroughput();
        WSN.printDelay();


        System.exit(0);
    }

    public static void printCollisionRate(){

        double collRate = 0;
        double avCollRate =0;
        double numb = WSN.nodes.size();

        System.out.println("\n Node ||  Coll/Transm  ||  Collision Rate [%] ");

        for (Node node : WSN.nodes) {
            collRate = ((double)node.getCollisionParam()[0])/((double)node.getCollisionParam()[1]);
            avCollRate = avCollRate + collRate / numb;
            System.out.println(node.getId() + "\t\t\t" + node.getCollisionParam()[0] + " / " + node.getCollisionParam()[1] + "\t\t\t\t"+ collRate);
        }
        System.out.println("\n Average Collision Rate = " +avCollRate+" [%]");

    }

    public static void printSlotNumber(){

        ArrayList<Integer> slotNumberList;
        double allAverageSlotNumber =0;
        double numb = WSN.nodes.size();

        System.out.println("\n Node ||  Average # of Contention Slots to successful transmit ");

        for (Node node : WSN.nodes) {
            slotNumberList = node.getSlotCounterList();
            if(debug){System.out.println(node.getId() + "\t\t" + slotNumberList.toString());}
            double avSlotNumber = calculateAverage(slotNumberList);
            allAverageSlotNumber +=  avSlotNumber / numb;

            System.out.println(node.getId() + "\t\t\t\t" + avSlotNumber);
        }
        System.out.println("\n Total Average Number of Contention Slot = " +allAverageSlotNumber);

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
        System.out.println("\n Total Average Throughput = " +allAvThroughput);

    }

    public static void printDelay(){

        ArrayList<Double> delayList;
        double allAvDelay =0;
        double numb = WSN.nodes.size();

        System.out.println("\n Node\t ||\t Av. Delays   ");

        for (Node node : WSN.nodes) {

            delayList = node.getDelayList();
            double avDelayTime = calculateAverageDouble(delayList);
            allAvDelay +=  avDelayTime / numb;
            System.out.println(node.getId() + "\t\t\t\t" +  avDelayTime);

        }
        System.out.println("\n Total Average Delay = " +allAvDelay+" [us]");

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


