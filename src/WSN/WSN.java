package WSN;

import events.Event;
import events.UpdatePosition;
import protocols.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;


/**
 * Created by Gianluca on 16/07/2017.
 */
public class WSN {

    // --------- MAIN SIMULATION PARAMETERS ----------//

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

    //public static double txTime = 200; // microseconds
    public static double txTime =  (double) Math.round((frameSize * 8) / (maxAvailableThroughput) * 100) / 100;; // txTime in microsecond

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

    // log of nodes that have transmitted (useful to fairness calculation)
    public static ArrayList<Node> nodeTrace;

    //
    // CONTI
    //

    public static double CONTIslotTime = 50;

    //
    // Methods
    //

    /************************      CONSTRUCTOR      ***********************/
    public WSN(int nodeCount, double width, double height, Protocol p, int topologyID, int mobilityID, boolean gui) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        RNG r = RNG.getInstance();
        nodes = new LinkedList<>();
        this.nodeCount = nodeCount;

        this.width = width;
        this.height = height;

        this.topologyID = topologyID;
        this.mobilityID = mobilityID;
        this.gui = gui;


        Scheduler scheduler = Scheduler.getInstance();

        //WSN.trasmittingNodes = new LinkedList<>();
        //WSN.listeningNodes = new LinkedList<>();
        //WSN.status = CHANNEL_STATUS.FREE;

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

        scheduler.schedule(new UpdatePosition(1000, mobilityID));

        // create GUI window
        if (gui){
            panelW = 500;
            panelH = 530;
            f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        this.maxRadius = radius;
    }

    private double[] nodePosition()
    {
        RNG r = RNG.getInstance();
        double[] coord = new double[2];

        double a, theta;
        setMaxRadius(0.5 * Math.min(width, height));

        switch (this.topologyID) {

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
        double[] size = {width, height};
        return size;
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

        setNeighborsList();
        printNeighbors();


        Scheduler scheduler = Scheduler.getInstance();
        double currentTime = 0;

        while ((!scheduler.isEmpty()) && (currentTime < maxTime)) {

            //System.in.read();
            try {
                Thread.sleep(sleepDelay);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Event e = scheduler.remove();
            currentTime = e.getTime();

            if (debug){
                System.out.println(e);
            }

            e.run();

            System.out.format("Progress: %.2f %%\n", (currentTime/maxTime*100.0));
            if (debug){
                System.out.println("\n");
            }

            // repaint GUI panel
            if (gui){
                guiWindow.paint();
            }
        }

        WSN.printCollisionRate();
        WSN.printSlotNumber();
//        WSN.printThroughput();
//        WSN.CONTIprintThroughput();
//        WSN.printDelay();
//        WSN.CONTIprintDelay();
//        WSN.printFairness(windowSize);
//        WSN.printNoNeighbors();
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
        //printNeighbors();
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

        LinkedList<Node> list = new LinkedList<Node>();
        for (Node neighbor : n.getNeighborList()){
            if (neighbor.getStatus() == status){ list.add(neighbor); }
        }
        if(WSN.debug){ System.out.println(status+": "+list.size()); }

        return list;
    }



    // output parameters

    public static void printCollisionRate(){

        double collRate;
        double avCollRate =0;
        double numb = WSN.nodes.size();
        System.out.println("\n[ DCF / CONTI ] ");
        System.out.println(" Node ||  Coll/Transm  ||  Normalized Collision Rate ");

        for (Node node : WSN.nodes) {
            collRate = ((double)node.getCollisionParam()[0])/((double)node.getCollisionParam()[1]);
            avCollRate = avCollRate + collRate / numb;
            System.out.println(node.getId() + "\t\t\t" + node.getCollisionParam()[0] + " / " + node.getCollisionParam()[1] + "\t\t\t\t"+ collRate);
        }

        double collPerc = avCollRate * 100;

        System.out.println("\n Average Collision Rate = " + Math.round(collPerc * 100.0)/100.0 + " [%]");
    }

    public static void printSlotNumber(){
        // calculate the average number of contention slot
        //      (# of transmission slots that a node spends in a contention).

        ArrayList<Integer> slotNumberList;
        double allAverageSlotNumber =0;
        double numb = WSN.nodes.size();
        System.out.println("\n[ DCF ] ");
        System.out.println(" Node ||  Average # of Contention Slots ");

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
        // ratio between time needed to successfully deliver a packet with a free channel (theoretical) and the overall time needed to successfully delivery (simulated)

        ArrayList<Double> totalTimeList;
        double allAvThroughput =0;
        double numb = WSN.nodes.size();
        System.out.println("\n[ DCF ] ");
        System.out.println(" Node ||  Av. Total Time to success. delivery || Av. Nomalized Throughput  ");

        for (Node node : WSN.nodes) {
            totalTimeList = node.getTotalTimeList();
            double avTotalTime = calculateAverageDouble(totalTimeList);
            double avThroughput = (WSN.DIFS + WSN.txTime + WSN.tACK + WSN.SIFS)/ avTotalTime;
            allAvThroughput +=  avThroughput / numb;
            System.out.println(node.getId() + "\t\t\t\t" +  avTotalTime+ "\t\t\t\t" +  avThroughput);
        }
        System.out.println("\n Total Average Normalized Throughput = " +allAvThroughput);
    }

    public static void CONTIprintThroughput(){
        // ratio between time needed to successfully deliver a packet with a free channel (theoretical) and the overall time needed to successfully delivery (simulated)

        ArrayList<Double> totalTimeList;
        int cSlots;
        double allAvThroughput =0;
        double numb = WSN.nodes.size();
        System.out.println("\n[ CONTI ] ");
        System.out.println(" Node || Av. Total Time to success. delivery || Av. Nomalized Throughput  ");

        for (Node node : WSN.nodes) {
            cSlots = node.CONTIp.length;        // to be removed...
            totalTimeList = node.getTotalTimeList();
            double avTotalTime = calculateAverageDouble(totalTimeList);
            double avThroughput = ((double)(cSlots * WSN.CONTIslotTime + WSN.txTime))/ avTotalTime;
            allAvThroughput +=  avThroughput / numb;
            System.out.println(node.getId()  + "\t\t\t\t" +  avTotalTime+ "\t\t\t\t" +  avThroughput);
        }
        System.out.println("\n Total Average Normalized Throughput = " +allAvThroughput);
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

    public static void CONTIprintDelay(){
        // (average) time passed between the beginning of the contention to transmit a packet and its successfully delivery
        ArrayList<Double> totalTimeList;
        int cSlots;
        double allAvDelay =0;
        double numb = WSN.nodes.size();

        System.out.println("\n[ CONTI ] ");
        System.out.println(" Node\t ||\t Av. Number of Rounds ||\t Av. Delays  ");

        for (Node node : WSN.nodes) {
            cSlots = node.CONTIp.length;        // to be removed...
            totalTimeList = node.getTotalTimeList();
            double avDelay = calculateAverageDouble(totalTimeList);
            allAvDelay +=  avDelay / numb;
            System.out.println(node.getId() + "\t\t\t\t" +  avDelay / (double) (cSlots * WSN.CONTIslotTime + WSN.txTime) + "\t\t\t" +  avDelay);

        }
        System.out.println("\n Total Average Delay = " +allAvDelay+" [us] ( "+allAvDelay/1000+" [ms] )");

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
        System.out.println("\n[ DCF / CONTI ] ");
        System.out.println(" Average Fairness [trace size: "+WSN.nodeTrace.size()+"]: "+sum/(windowsFairness.length)+"\n");
    }



    private static void printNoNeighbors(){
        // percentage of no neighbors events over the total transmission attempts
        System.out.println("\n[ DCF / CONTI ] ");
        System.out.println(" No-Neighbors events:    [%] \n");
        double percentage;
        double mean =0;
        for (Node node : WSN.nodes) {
            percentage  = node.getNoNeighbor()*100;
            mean += percentage / (double) WSN.nodes.size();
            System.out.println(" Node "+node.getId()+ ":\t" + percentage);
        }
        System.out.println("\n  Average number of No Neighbors events:  = " +mean+" [%] (max is 100) \n");
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

    public static void saveToFile(String filename) throws IOException{

        File f = new File(filename);

        // automatically checks if file exists or not.
        // if the file does not exist, it automatically creates it
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
            fw.append(String.format("%s;%s;%s;%s;%s", "nodecount", "framesize", "width", "height", "tx-time"));
        }

        // save simulation parameters and results
        fw.append(String.format("\n%d;%d;%f;%f;%.2f", nodes.size(), frameSize, width, height, txTime));

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


