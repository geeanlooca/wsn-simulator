package WSN;

import protocols.Event;
import protocols.DCF.StartListeningEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import WSN.RNG;


/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {

    // --------- MAIN SIMULATION PARAMETERS ----------//

    private int nodeCount;                       // number of nodes in the network
    private long sleepDelay = 0;                      // delay used to extract events
    final double maxIndex = Math.pow(10, 6);        // max available number of events; used to exit the script and debug results (use Double.POSITIVE_INFINITY to never exit) 1000000000
    public static boolean debug = false;            // printing extra information useful for debugging

    final static double maxAvailableThroughput = 11;    // Mb/s
    final static double frameSize = 1500;               // bytes

    private int windowSize = 1000;                 //  window size used in Fairness calculation
    // ------------------------------------//

    public enum NODE_STATUS {
      SLEEPING, TRANSMITTING, IDLING, RECEIVING, LISTENING, JAMMING
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
        RNG r = RNG.getInstance();
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

    //public static List<Node> trasmittingNodes;
    //public static List<Node> listeningNodes;

    public static ArrayList<Node> nodeTrace;



    //
    // CONTI
    //

    public static double CONTIslotTime = 50;

    //
    // Methods
    //

    public WSN(int nodeCount, double width, double height, int topologyID){

        RNG r = RNG.getInstance();
        nodes = new LinkedList<>();
        this.nodeCount = nodeCount;

        this.width = width;
        this.height = height;

        this.topologyID = topologyID;


        Scheduler scheduler = Scheduler.getInstance();

        //WSN.trasmittingNodes = new LinkedList<>();
        //WSN.listeningNodes = new LinkedList<>();
        //WSN.status = CHANNEL_STATUS.FREE;

        WSN.nodeTrace = new ArrayList<>();

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

            scheduler.schedule(new protocols.CONTI.StartRound(n,0));
        }
    }

    private double[] nodePosition()
    {
        RNG r = RNG.getInstance();
        double[] coord = new double[2];

        double a, theta;
        double maxRadius = 0.5 * Math.min(width,height);

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

        setNeighborsList();

        RNG r = RNG.getInstance();

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
            currentTime = e.getTime();

            if (debug){
                System.out.println(e);
                //System.out.println("Number of transmitting nodes: " + trasmittingNodes.size());
            }


            e.run();

            if (debug){
                System.out.println("\n");
            };

            System.out.format("Progress: %f %%\n", currentTime/maxTime);
        }

/*        WSN.printCollisionRate();
        WSN.printSlotNumber();
        WSN.printThroughput();
        WSN.printDelay();
        WSN.printFairness(windowSize);*/

        //System.exit(0);
    }

    public void setNeighborsList(){

        double PrxThreshold = 0;
        double Ptx = 10000;

        for (Node nodeA : WSN.nodes){
            for (Node nodeB : WSN.nodes){
                if (nodeB.getId() != nodeA.getId()){
                    Channel channel = new Channel(nodeA, nodeB, Ptx);

                    double Prx = channel.getPrx();
                    //System.out.println(Prx);

                    if (Prx >= PrxThreshold && !(nodeB.findNeighbor(nodeA))){
                        nodeA.addNeighbor(nodeB);
                        nodeB.addNeighbor(nodeA);
                    }
                }
            }
            ArrayList<Node> neighborsList = nodeA.getNeighborList();
            System.out.print("\n \nNode "+nodeA.getId()+" neighbors list:\t");
            for (Node entry : neighborsList){
                System.out.print(entry.getId()+"\t");
            }
        }
        System.out.println("\n");
    }

    public static ArrayList<Node> getNeighborsStatus(Node n, NODE_STATUS status ){

        ArrayList<Node> list = new ArrayList<Node>();
        for (Node neighbor : n.getNeighborList()){
            if (neighbor.getStatus() == status){ list.add(neighbor); }
        }
        if(WSN.debug){ System.out.println(status+": "+list.size()); }

        return list;
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


    private static void printFairness(int windowSize){
        // fairness calculation with Jain's fairness index and sliding windows

        // -- -- -- --
        boolean debugFairness = false;      // if true more useful information are displayed
        // -- -- -- --
        double[] windowResults = new double[nodes.size()];
        List<Boolean>  tempWindow = new ArrayList<Boolean>();
        double[] windowsFairness =  new double[0];

        try {
            windowsFairness = new double[WSN.nodeTrace.size() - windowSize + 1];
        }
        catch (Exception e){
            System.out.println("\n"+ e + "\nERROR!! More simulation time is needed with windowSize = " + windowSize + "\nSystem exit... ");
            System.exit(1);
        }
        if(debugFairness){ System.out.println("\n \n Node Trace "); }
        for (Node node : WSN.nodes) { node.setListIterator(); }

        for (int i=0; i<WSN.nodeTrace.size(); i++  ) {
            if(debugFairness){  System.out.println("\n[i= " + i+"]"); }

            Node node = WSN.nodeTrace.get(i);
            boolean res = node.getLog();
            int id = node.getId();
            if(debugFairness){ System.out.println("Node "+id + "\ttransmission result: " + res);}
            tempWindow.add(res);

            if (res) {
                windowResults[id] += (double) 1 / (double) windowSize;
                if(debugFairness){ System.out.println("tempResult  " + windowResults[id]); }
            }
            if (((i + 1) == windowSize) || ((i + 1) > windowSize)){
                double num = 0;
                for (double entry : windowResults) {
                    num += entry;
                }
                double den = 0;
                for (double entry : windowResults) {
                    den += Math.pow(entry, 2);
                }
                windowsFairness[i+1 - windowSize] = Math.pow(num, 2) / (den * nodes.size());            ///!!!

                if(debugFairness){ System.out.println("Fairness of window " + (i+1 - windowSize)+":\t" + windowsFairness[(i + 1) - windowSize]); }

                Node headNode = WSN.nodeTrace.get(i+1 - windowSize);
                int headId = headNode.getId();
                if (tempWindow.remove(0)) {
                    windowResults[headId] -= (double) 1 / (double) windowSize;
                }
                if(debugFairness){  System.out.println(tempWindow); }
            }
        }
        double sum = 0;
        for (double entry : windowsFairness) {
            sum += entry;
            if(debugFairness){  System.out.println(entry); }
        }
        System.out.println("\n \nAverage Fairness [trace size: "+WSN.nodeTrace.size()+"]: "+sum/(windowsFairness.length)+"\n");
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


