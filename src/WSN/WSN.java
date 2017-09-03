package WSN;



import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import events.Event;
import events.UpdatePosition;
import protocols.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {

    // --------- MAIN SIMULATION PARAMETERS ----------//

    private static long runningTime = 0;
    private static Protocol p;
    private static double simulationTime;
    private int nodeCount;                       // number of nodes in the network
    private long sleepDelay = 0;                      // delay used to extract events
    public static boolean debug = false;            // printing extra information useful for debugging

    final static double maxAvailableThroughput = 11;    // Mb/s
    public static int frameSize = 1500;               // bytes

    private int windowSize = 1000;                 //  window size used in Fairness calculation

    public static double PrxThreshold = -82;        // threshold on received power (dBm)
    public static double Ptx = 20;                   // transmission power (dBm)
    public static boolean indoor = false;           // indoor or outdoor scenario
    // ------------------------------------//

    public enum NODE_STATUS {
      SLEEPING, TRANSMITTING, IDLING, RECEIVING, LISTENING, JAMMING
    };

    /***********************************************
     *                  GUI                        *
     ***********************************************/

    private boolean gui = false;
    public static Color txColor = Color.magenta;
    public static Color normColor = Color.blue;
    public static Color sleepColor = Color.pink;
    public static Color listenColor = Color.cyan;
    private WSNWindow guiWindow;
    private int panelW, panelH;
    private JFrame f;

    public void setPanelSize(int w, int h){
        if (gui){
            this.panelW = w;
            this.panelH = h;
            f.setSize(w, h);
        }
    }


    public static double meanInterarrivalTime = 20.0;
    public static double meanBackoff = 200.0;
    public static double sleepTime = 50.0;

    public static double normSize = 10;
    public static double txSize = 15;

    public static double tPLC = 192;
    public static double SIFS = 10;
    public static double DIFS = 50;
    public static double tSlot = 20;
    public static double tACK = 20 + tPLC;
    public static double txTime =  (double) Math.round((frameSize * 8)
            / (maxAvailableThroughput) * 100) / 100 + tPLC;

    public static int CWmin = 15;
    public static int CWmax = 1023;

    private static int topologyID;
    private static int mobilityID;

    private static double width, height;
    private static double maxRadius;

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

    public static List<Node> nodes;


    // Collison rates
    public static HashSet<Double> collided = new HashSet<>();
    public static HashSet<Double> attempted = new HashSet<>();
    public static ArrayList<Node> transmitting = new ArrayList<Node>();
    public static int access = 0;
    public static int collisions = 0;

    // log of nodes that have transmitted (useful to fairness calculation)
    public static ArrayList<Node> nodeTrace;

    //
    // CONTI
    //

    public static double CONTIslotTime = 20;

    //
    // Methods
    //

    /************************      CONSTRUCTOR      ***********************/
    public WSN(int nodeCount, double width, double height, Protocol p, int topologyID, int mobilityID, boolean gui) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        RNG r = RNG.getInstance();
        nodes = new LinkedList<>();
        this.nodeCount = nodeCount;

        WSN.width = width;
        WSN.height = height;

        WSN.topologyID = topologyID;
        WSN.mobilityID = mobilityID;
        this.gui = gui;


        Scheduler scheduler = Scheduler.getInstance();

        this.p = p;
        WSN.nodeTrace = new ArrayList<>();

        for (int i = 0; i < this.nodeCount; i++) {

            double[] coord = nodePosition();

            double X = coord[0];
            double Y = coord[1];

            Node n = new Node(i, X, Y);
            n.setStatus(WSN.NODE_STATUS.IDLING);
            nodes.add(n);

            Event e = (Event) p.entryPoint().newInstance(n,new Double(0));
            scheduler.schedule(e);
        }

        //scheduler.schedule(new UpdatePosition(1000, mobilityID));

        // create GUI window
        if (gui){
            panelW = 500;
            panelH = 530;
            f = new JFrame();
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            guiWindow = new WSNWindow(this);
            f.getContentPane().add(guiWindow);
            f.setSize(panelW,panelH);
            f.setLocation(200,200);
            f.setVisible(true);
        }
    }
    /**********************************************************************/

    public static double getWidth() {
        return width;
    }

    public static double getHeight() {
        return height;
    }

    public static double getMaxRadius() {
        return maxRadius;
    }

    public static int getTopologyID() {
        return topologyID;
    }

    private void setMaxRadius(double radius) {
        maxRadius = radius;
    }

    private double[] nodePosition()
    {
        RNG r = RNG.getInstance();
        double[] coord = new double[2];

        double a, theta;
        setMaxRadius(0.5 * Math.min(width, height));

        switch (topologyID) {

            // circular cell
            case 0:
                a = maxRadius * Math.sqrt(r.nextDouble());
                theta = 2 * Math.PI * r.nextDouble();
                coord[0] = a * Math.cos(theta);
                coord[1] = a * Math.sin(theta);
                break;

            // hexagonal cell
            case 1:
                double c = Math.cos(Math.PI / 6);

                do {
                    a = Math.sqrt(r.nextDouble());
                    theta = -Math.PI / 6 + Math.PI / 3 * r.nextDouble();
                } while ((a * Math.cos(theta)) > c);

                a = a * maxRadius;
                theta = theta + Math.PI / 3 * r.nextInt(6);

                coord[0] = a * Math.cos(theta);
                coord[1] = a * Math.sin(theta);
                break;
            default:
                coord[0] = width * r.nextDouble();
                coord[1] = height * r.nextDouble();
                break;
        }

        return coord;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public double[] getNetworkSize() {
        return new double[]{width, height};
    }

    public int nodeCount() {
        return WSN.nodes.size();
    }

    public void setAnimationDelay(int ms) {
        this.sleepDelay = ms;
    }

    public void debugging(boolean enable){
        debug = enable;
    }

    public void run(){
        this.run(Double.POSITIVE_INFINITY);
    }

    public void run(double maxTime) {

        System.out.println("tx time: "+txTime);

        setNeighborsList();
        printNeighbors();

        Scheduler scheduler = Scheduler.getInstance();
        double currentTime = 0;
        long startTime = System.currentTimeMillis();
        while ((!scheduler.isEmpty()) && (currentTime < maxTime)) {

            //System.in.read();
            if (sleepDelay > 0){
                try {
                    Thread.sleep(sleepDelay);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            Event e = scheduler.remove();
            currentTime = e.getTime();

            if (debug){
                System.out.println(e);
            }

            e.run();

            //if ((((currentTime/maxTime)*100.0) % 5) == 0) {System.out.format("Progress: %.2f %%\n", ((currentTime/maxTime)*100.0)); }

            //System.out.format("Progress: %.2f %%\n", ((currentTime/maxTime)*100.0));

            //if (((currentTime/maxTime)*100.0)>70) {WSN.debug = true;}

            if (debug){
                System.out.println("\n");
            }

            // repaint GUI panel
            if (gui){
                guiWindow.paint();
            }
        }

        simulationTime = currentTime;

//        WSN.printCollisionRate();
//        WSN.printContentionSlot2();
//        WSN.printThroughput();
//        WSN.CONTIprintThroughput();
//        WSN.printThroughput2(currentTime);
//        WSN.printDelay();
//        WSN.printFairness(windowSize);
//        WSN.printNoNeighbors();
        System.out.println("\n");
        System.out.println("Collision rate [%]: "+WSN.collisionRate());
        System.out.println("Alternate Collision rate [%]: "+WSN.alternateCollisionRate());
        System.out.println("Number of contention slot: "+WSN.contentionSlot());
        System.out.println("Throughput: "+WSN.throughput(currentTime));
        System.out.println("Delay [us]: "+WSN.delay());
        System.out.println("Normalized fairness: "+WSN.fairness(windowSize));      // there is also the trace size to be considered...
        System.out.println("No neighbors [%]: "+WSN.noNeighbors());

        long endTime   = System.currentTimeMillis();
        runningTime = (endTime-startTime)/1000;

    }

    public static void setNeighborsList(){

        for (Node nodeA : WSN.nodes) {
            for (Node nodeB : WSN.nodes) {
                if (nodeB.getId() != nodeA.getId()) {
                    Channel channel = new Channel(nodeA, nodeB, Ptx, indoor);

                    double Prx = channel.getPrx();
                    //System.out.println(Prx);

                    //System.out.println("Node "+nodeB.getId()+" has neighbor Node "+nodeA.getId()+" ? "+(nodeB.findNeighbor(nodeA)));

                    if (Prx >= PrxThreshold && !(nodeB.findNeighbor(nodeA))) {
                        //System.out.println(nodeA.getId() + "\t"+nodeB.getId());
                        nodeA.addNeighbor(nodeB);
                        nodeB.addNeighbor(nodeA);
                    }
                }
            }
        }
    }

    public static void printNeighbors() {

        for(Node node :WSN.nodes){
            ArrayList<Node> neighborsList = node.getNeighborList();
            System.out.print("\n \nNode " + node.getId() + " neighbors list:\t");
            for (Node entry : neighborsList) {
                System.out.print(entry.getId() + "\t");
            }
        }
        System.out.println("\n");
    }

    public static LinkedList<Node> getNeighborsStatus(Node n, NODE_STATUS status ){

        LinkedList<Node> list = n.neighborStatus;
        list.clear();

        for (Node neighbor : n.getNeighborList()){
            if (neighbor.getStatus() == status){ list.add(neighbor); }
        }
        if(WSN.debug){ System.out.println(status+": "+list.size()); }

        return list;
    }



    // output parameters

    public static void printCollisionRate(){

        int transmissions =0 ;
        int collisions = 0;
        System.out.println("\n[ DCF / CONTI ] ");
        System.out.println(" Node ||  Coll/Transm  ||  Normalized Collision Rate ");

        for (Node node : WSN.nodes) {
            collisions += node.getCollisionParam()[0];
            transmissions += node.getCollisionParam()[1];
            System.out.println(node.getId() + "\t\t\t" + node.getCollisionParam()[0] + " / " + node.getCollisionParam()[1] + "\t\t\t\t"+ ((double)node.getCollisionParam()[0])/((double)node.getCollisionParam()[1]));

        }
        double collPerc = (double) collisions / (double) transmissions * 100;
        System.out.println("\n Average Collision Rate = " + Math.round(collPerc * 100.0)/100.0 + " [%]");
    }

    public static double alternateCollisionRate(){

        double rate;
        if (p.getClass().getSimpleName().equals("CONTI")){
            double coll = collided.size();
            double att = attempted.size();
            rate = coll/att;
        }else{
            rate = (double) collisions/access;
        }

        return rate*100;
    }

    public static double collisionRate(){
        int transmissions = 0;
        int collisions = 0;
        for (Node node : WSN.nodes) {
            collisions += node.getCollisionParam()[0];
            transmissions += node.getCollisionParam()[1];
        }
        double collPerc = (double) collisions / (double) transmissions * 100;
        return  Math.round(collPerc * 100.0)/100.0;
    }


    public static void printContentionSlot(){
        // calculate the average number of contention slot
        //      (# of transmission slots that a node spends in a contention).

        ArrayList<Integer> slotNumberList;
        double allAverageSlotNumber = 0;
        double numb = WSN.nodes.size();
        System.out.println("\n[ DCF ] ");
        System.out.println(" Node ||  Average # of Contention Slots ");

        for (Node node : WSN.nodes) {
            slotNumberList = node.getSlotCounterList();
            //if(debug){System.out.println(node.getId() + "\t\t" + slotNumberList.toString());}
            System.out.println(node.getId() + "\t\t" + slotNumberList.toString());

            double avSlotNumber = calculateAverage(slotNumberList);
            allAverageSlotNumber +=  avSlotNumber / numb;

            System.out.println(node.getId() + "\t\t\t\t" + avSlotNumber);
        }
        System.out.println("\n Total Average Number of Contention Slot = " +allAverageSlotNumber);
    }



    public static void printContentionSlot2(){
        // calculate the average number of contention slot (# of transmission slots that a node spends in a contention).
        // new method
        ArrayList<Integer> list;
        ArrayList<Integer> slotCounterList = new ArrayList<Integer>();

        double numb = WSN.nodes.size();
        System.out.println("\n[ DCF ] ");
        System.out.println(" Node ||  Average # of Contention Slots ");
        for (Node node : WSN.nodes) {
            list = node.getSlotCounterList();
            //System.out.println(node.getId() + "\t\t" + list.toString());

            slotCounterList.addAll(list);
            double avSlotNumber = calculateAverage(list);
            System.out.println(node.getId() + "\t\t\t\t" + avSlotNumber);
        }
        double allAverageSlotCounter =calculateAverage(slotCounterList);
        System.out.println("\n Total Average Number of Contention Slot = " +allAverageSlotCounter);
    }

    public static double contentionSlot(){
        // calculate the average number of contention slot (# of transmission slots that a node spends in a contention).
        // new method
        ArrayList<Integer> list;
        ArrayList<Integer> slotCounterList = new ArrayList<Integer>();
        double numb = WSN.nodes.size();
        for (Node node : WSN.nodes) {
            list = node.getSlotCounterList();
            slotCounterList.addAll(list);
        }
        double allAverageSlotCounter =calculateAverage(slotCounterList);
        return allAverageSlotCounter;
    }



    public static double throughput2() {
        // [OLD] used in presence of multiple parallels channels

        // [(total successfully transmitted packets * frameSize) / total simulation time  ]* maxAvailableThroughput
        double avThroughput = 0;
        double numb = WSN.nodes.size();

        for (Node node : WSN.nodes) {
            int collisions = node.getCollisionParam()[0];
            int transmissions =  node.getCollisionParam()[1];
            ArrayList<Double> delayList = node.getDelayList();

            double totalTime = 0;
            for (double delay : delayList){ totalTime = Math.floor((totalTime + delay)*100)/100; }
            avThroughput += ((((double)(transmissions - collisions)) * (double) (frameSize * 8)) / totalTime ) / numb;
        }
        double normThroughput = avThroughput / maxAvailableThroughput;
        return normThroughput;
    }


    public static double throughput(double currentTime) {
        // [(total successfully transmitted packets * frameSize) / total simulation time  ]* maxAvailableThroughput
        int collisions = 0;
        int transmissions = 0;

        for (Node node : WSN.nodes) {
            collisions += node.getCollisionParam()[0];
            transmissions +=  node.getCollisionParam()[1];
        }
        double avThroughput = (((double)(transmissions - collisions)) * (double) (frameSize * 8)) / currentTime;
        double normThr = avThroughput / maxAvailableThroughput;
        return normThr;
    }




    public static void printDelay(){
    // (average) time passed between the beginning of the contention to transmit a packet and its successfully delivery
        ArrayList<Double> delayList;
        double allAvDelay =0;
        double numb = WSN.nodes.size();

        System.out.println("\n[ DCF ] ");
        System.out.println(" Node\t ||\t Av. Delays   ");

        for (Node node : WSN.nodes) {

            delayList = node.getDelayList();
            double avDelay = calculateAverageDouble(delayList);
            allAvDelay +=  avDelay / numb;
            System.out.println(node.getId() + "\t\t\t\t" +  avDelay);
        }
        System.out.println("\n Total Average Delay = " +allAvDelay+" [us] ( "+allAvDelay/1000+" [ms] )");
    }


    public static double delay(){
        // (average) time passed between the contention beginning and the successful transmission
        ArrayList<Double> delayList;
        double allAvDelay = 0;
        double numb = WSN.nodes.size();

        for (Node node : WSN.nodes) {
            delayList = node.getDelayList();
            double avDelay = calculateAverageDouble(delayList);
            allAvDelay +=  avDelay / numb;
        }
        // delay in us
        return allAvDelay;
    }



    private static void printFairness(int windowSize){
        // fairness calculation with Jain's fairness index and sliding windows (like into the 2011 paper)

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
            System.out.println("\n"+ e + "\nFairness Error!! More simulation time is needed with windowSize = " + windowSize + "\nSystem exit... ");
            System.exit(1);
        }
        if(debugFairness){ System.out.println("\n \n Node Trace "); }

        // initialize an iterator to scan the nodeLog list of the node
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
                windowsFairness[i+1 - windowSize] = Math.pow(num, 2) / (den * nodes.size());

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
        System.out.println("\n[ DCF / CONTI ] ");
        System.out.println(" Average Fairness [trace size: "+WSN.nodeTrace.size()+"]: "+sum/(windowsFairness.length)+"\n");
    }


    private static double fairness(int windowSize){
        // fairness calculation with Jain's fairness index and sliding windows (like into the 2011 paper)
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
            System.out.println("\n"+ e + "\nFairness Error!! More simulation time is needed with windowSize = " + windowSize + "\nSystem exit... ");
            System.exit(1);
        }
        if(debugFairness){ System.out.println("\n \n Node Trace "); }


        for (Node node : WSN.nodes) { node.setListIterator(); }     // initialize an iterator to scan the nodeLog list of the node

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
                windowsFairness[i+1 - windowSize] = Math.pow(num, 2) / (den * nodes.size());

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

        return sum/(windowsFairness.length);
    }


    private static void printNoNeighbors(){
        // percentage of no neighbors events over the total transmission attempts
        int transmissions = 0;
        int noNeighborEvents = 0;
        System.out.println("\n[ DCF / CONTI ] ");
        System.out.println(" No-Neighbors events:    [%] \n");
        for (Node node : WSN.nodes) {
            transmissions += node.getCollisionParam()[1];
            noNeighborEvents += node.getNoNeighbor();
            System.out.println(" Node "+node.getId()+ " has "+node.getNoNeighbor()+" events");
        }
        double avNoNeighbors = (double) (noNeighborEvents * 100) /(double)  (transmissions + noNeighborEvents);
        System.out.println("\n  Average number of No Neighbors events:  = " +avNoNeighbors+" [%] (max is 100) \n");
    }


    private static double noNeighbors(){
        // percentage of no neighbors events over the total transmission attempts
        int transmissions = 0;
        int noNeighborEvents = 0;
        for (Node node : WSN.nodes) {
            transmissions += node.getCollisionParam()[1];
            noNeighborEvents += node.getNoNeighbor();
        }
        double avNoNeighbors = (double) (noNeighborEvents * 100) /(double)  (transmissions + noNeighborEvents);
        return avNoNeighbors;
    }



    private static double calculateAverage(List <Integer> list) {
        Integer sum = 0;
        if(!list.isEmpty()) {
            for (Integer entry : list) {
                sum += entry;
            }
            return (((double) sum )/( (double) list.size()));
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

    public static void saveToFile(String filename) throws IOException{

        File f = new File(filename);
        String protocol = WSN.p.getClass().getSimpleName();

        // automatically checks if file exists or not.
        // if the file does not exist, it automatically creates it
        f.getParentFile().mkdirs();
        boolean created = f.createNewFile();
        boolean printColumns = created;

        // if the file aleady existed, check wheter it's empty
        if (!created){
            // determine if file is empty. if it is save column names
            BufferedReader br = new BufferedReader(new FileReader(filename));
            printColumns = br.readLine() == null;
            br.close();
        }

        // open file
        FileWriter fw = new FileWriter(filename, true);

        // print column names
        if (printColumns){
            fw.append(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", "protocol", "running-time", "nodecount", "simulation-time", "framesize", "width",
                    "height", "tx-time", "collision-rate", "alternate-rate", "throughput"));
        }

        // save simulation parameters and results
        int seconds = new Double(simulationTime/1e6).intValue();
        fw.append(String.format("\n" +
                        "%s;" +
                        "%d;%d;%d;%d;" +
                        "%.2f;%.2f;" +
                        "%.2f;" +
                        "%.3f;%.3f;" +
                        "%.3f",
                protocol,
                runningTime, nodes.size(), seconds , frameSize,
                width, height,
                txTime,
                WSN.collisionRate(), WSN.alternateCollisionRate(),
                WSN.throughput(simulationTime)));

        // close file
        fw.close();
    }

    /*****************************
     *          GUI              *
     *****************************/
    class WSNWindow extends JPanel{

        Color selectedColor;

        private WSN network;

        private WSNWindow(WSN network){
            setBackground(Color.white);
            selectedColor = Color.blue;
            this.network = network;
        }

        private void paint(){
            repaint();
        }

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // find the panel dimension
            double panelH = getHeight();
            double panelW = getWidth();

            double[] networkSize = network.getNetworkSize();
            double netW = networkSize[0];
            double netH = networkSize[1];

            // scaling factors only to draw the nodes
            double scaleX = 0.9 * panelW/netW;
            double scaleY = 0.9 * panelH/netH;

            double nodeX, nodeY, nodeSize;
            for (int i = 0; i < network.nodeCount(); i++) {

                Node n = network.getNodes().get(i);

                nodeX = panelW/2 + n.getX() * scaleX;
                nodeY = panelH/2 + n.getY() * scaleY;
                nodeSize = n.getSize();

                try {
                    for (Node neigh : n.getNeighborList()) {
                        g2.setColor(n.getLineColor());

                        double neighX = panelW / 2 + neigh.getX() * scaleX;
                        double neighY = panelH / 2 + neigh.getY() * scaleY;
                        g2.draw(new Line2D.Double(nodeX, nodeY, neighX, neighY));
                    }
                }catch (ConcurrentModificationException exc){

                }

                Ellipse2D e = new Ellipse2D.Double(nodeX-nodeSize/2,nodeY-nodeSize/2,nodeSize,nodeSize);
                g2.setPaint(n.getColor());
                g2.fill(e);

                if(mobilityID == 1) { // draw mobility range in case of Gauss-Markov model
                    double X0 = panelW / 2 + n.X0 * scaleX;
                    double Y0 = panelH / 2 + n.Y0 * scaleY;
                    double range = WSN.getMaxRadius() / 4;

                    e = new Ellipse2D.Double(X0 - range / 2, Y0 - range / 2, range, range);
                    g2.setPaint(Color.red);
                    g2.draw(e);
                }

                Font font = new Font("Serif", Font.PLAIN, 18);
                g2.setFont(font);
                g2.setColor(Color.black);
                g2.drawString(String.valueOf(n.getId()), (int) nodeX, ((int) nodeY)-3);
            }

            g2.setPaint(Color.black);

            switch (topologyID){
                // circular cell
                case 0:
                    g2.draw(new Ellipse2D.Double(0.05 * panelW,0.05 * panelH,0.9 * panelW, 0.9 * panelH));
                    break;

                // hexagonal cell
                case 1:
                    Path2D hexagon = new Path2D.Double();
                    Point2D center = new Point2D.Double(panelW/2, panelH/2);
                    double r = 0.48 * Math.min(panelH, panelW);

                    // initial point
                    hexagon.moveTo(center.getX() + r * Math.cos(Math.PI/6), center.getY() + r * Math.sin(Math.PI/6));

                    for(int i=1; i<6; i++) {
                        hexagon.lineTo(center.getX() + r * Math.cos((2*i+1)*Math.PI/6), center.getY() + r * Math.sin((2*i+1)*Math.PI/6));
                    }
                    hexagon.closePath();

                    g2.draw(hexagon);
                    break;

                default:
                    g2.setPaint(Color.black);
                    g2.draw(new Rectangle2D.Double(1, 1,panelW - 2 , panelH - 3));
                    break;
            }
        }
    }

}


