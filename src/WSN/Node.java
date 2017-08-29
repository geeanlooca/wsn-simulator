package WSN;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.*;
import WSN.RNG;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Node {

    // coordinates, color and size of the node
    private RNG rng = RNG.getInstance();
    public double X, X0;
    public double Y, Y0;
    private int id;
    private java.awt.Color c;
    private double size;

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    private Color lineColor;

    private WSN.NODE_STATUS status;
    private java.util.Queue<Packet> buffer;
    private java.util.List<Packet> transmittedPackets;

    // congestion window and backoff counter
    private int BOcounter;
    private int CW;

    // channel access parameters
    public boolean freeChannel;
    public boolean collided;
    public ArrayList<Node> collidedNodes = new ArrayList<Node>();
    public ArrayList<Node> resumingNodes = new ArrayList<Node>();
    public Node lastBOstopped;

    private ArrayList<Node> neighborList;
    private int noNeighborCounter;

    // mobility
    double maxSpeed = 20;   // speed in m/s
    double minSpeed = 10;   // speed in m/s
    double avgSpeed = 15;   // speed in m/s
    double avgDir = 80;     // direction in degree
    double alphaS = 0.7;     // sensitivity factor
    double alphaD = 0.7;     // sensitivity factor

    double speed = minSpeed + rng.nextDouble() * (maxSpeed-minSpeed);       // initial value for the speed
    double dir = rng.nextInt(360);         // initial value for the direction

    // output parameters
    private int transCounter;
    private int collCounter;

    private int slotCounter;
    private ArrayList<Integer> slotCounterList;

    private double totalTime;
    private ArrayList<Double> totalTimeList;

    private double startTX;
    private ArrayList<Double> delayList;

    private ArrayList<Boolean> nodeLog;
    private ListIterator<Boolean> iterator;

    public ArrayList<Integer> windows;


    // CONTI
    public int CONTIslotNumber = 0;
    public double[] CONTIp = {0.2563, 0.36715, 0.4245, 0.4314, 0.5};

    public int CONTIroundCounter =0;


    //
    //  Methods
    //

    public Node(int id, double X, double Y){

        // current position of the node
        this.X = X;
        this.Y = Y;

        // save the initial position to control the mobility range
        this.X0 = X;
        this.Y0 = Y;

        this.id = id;
        this.size = 10;
        c = Color.blue;

        RNG random = RNG.getInstance();

        setLineColor(Color.lightGray);
        buffer = new LinkedList<Packet>();
        collided = false;

        collidedNodes = new ArrayList<Node>();
        resumingNodes = new ArrayList<Node>();
        this.neighborList = new ArrayList<Node>();

        noNeighborCounter = 0;

        transCounter = 0;
        collCounter = 0;
        slotCounter = 0;
        startTX =0;
        this.slotCounterList = new ArrayList<Integer>();
        this.totalTimeList = new ArrayList<Double>();
        this.delayList = new ArrayList<Double>();
        this.nodeLog = new ArrayList<Boolean>();

        this.windows = new ArrayList<>();


        RNG r = RNG.getInstance();
        this.setCW(WSN.CWmin);

        BOcounter = r.nextInt(CW + 1);
    }

    public double getX(){
        return this.X;
    }
    public double getY(){
        return this.Y;
    }
    public void setX(double x) {
        X = x;
    }

    public void setY(double y) {
        Y = y;
    }

    public int getId(){
        return this.id;
    }

    public void setColor(Color newColor){
        c = newColor;
    }

    public Color getColor(){
        return c;
    }

    public String toString(){
        return this.id + ", (" + this.X + ", " + this.Y + ")";
    }

    public double getSize(){
        return this.size;
    }

    public void setSize(double size){
        this.size = size;
    }

    public void enqueuePacket(Packet p){
        this.buffer.add(p);
    }

    public Packet dequeue(){
        return this.buffer.remove();
    }

    public Packet getNextPacket() {return this.buffer.element(); }

    public boolean backlogged(){
        return !this.buffer.isEmpty();
    }

    public WSN.NODE_STATUS getStatus(){
        return this.status;
    }

    public void setStatus(WSN.NODE_STATUS status){
        this.status = status;
    }

    public int decreaseCounter(){
        BOcounter--;
        return BOcounter;
    }

    public int getBOcounter(){
        return BOcounter;
    }

    public void setBOcounter(int BOcounter){
        this.BOcounter = BOcounter;
    }

    public int getCW(){
        return CW;
    }

    public void setCW(int CW){
        windows.add(CW);
        this.CW = CW;
    }

    // methods to handle neighbors

    public void addNeighbor( Node node){
        this.neighborList.add(node);
    }

    public ArrayList<Node> getNeighborList(){
        return this.neighborList;
    }

    public boolean findNeighbor(Node node){
        for (Node neighbor : this.neighborList){
            if (node.getId() == neighbor.getId()){ return true;}
            }
        return false;
    }

    public void clearNeighbors(){ this.neighborList.clear();}

    public void increaseNoNeighbor() {this.noNeighborCounter ++; }

    public double getNoNeighbor() {
        //System.out.println(this.noNeighborCounter+ "  "+ this.transCounter);
        return (((double)this.noNeighborCounter)/((double)(this.transCounter + this.noNeighborCounter)));
    }

    // output parameters

    // methods to calculate Collision Rate and Fairness

    public void addTransmission(){
        //System.out.println("qui!!");
        this.transCounter++;
        // keep track of the result of the transmissions for this node
        this.nodeLog.add(true);
    }

    public void addCollision(){
        this.collCounter ++;
        // keep track of the result of the transmissions for this node
        this.nodeLog.set(this.nodeLog.size()-1, false);
    }

    public int[] getCollisionParam(){
        int[] param = new int[2];
        param[0] = this.collCounter;
        param[1] = this.transCounter;
        return param;
    }

    public void setListIterator(){
        this.iterator = this.nodeLog.listIterator();
    }  // iterator needed to scan the nodeLog list at different times

    public Boolean getLog() {
        return this.iterator.next();
    }


    // methods to calculate the Average Number of Contention Slots

    public void addContSlot(){
        this.slotCounter ++;
        if (WSN.debug){ System.out.println("Contention Slot Counter: \t"+ this.slotCounter);}
    }

    public void resetContSlot(){ this.slotCounter=0; }

    public void storeContSlotNumber() {
        this.slotCounterList.add(this.slotCounter);
        this.slotCounter = 0;
        if (WSN.debug){ System.out.println("Contention Slot Counter List: \t"+ this.slotCounterList);}

    }
    public ArrayList<Integer> getSlotCounterList() { return this.slotCounterList; }


    // methods to calculate the packet total transmission time useful for Throughput and Delay

    public void addDIFStime(){ this.totalTime += WSN.DIFS; }
    public void addSlotTime(){ this.totalTime += WSN.tSlot; }
    public void addTXtime(){ this.totalTime += WSN.txTime; }
    // catch the current time when contention begins
    public void startTXTime(double time){
        if (!collided){ this.startTX = time; }
    }

    public void setTotalTime( double time){
        // throughput
        this.totalTime += WSN.SIFS + WSN.tACK;
        this.totalTimeList.add(this.totalTime);
        this.totalTime=0;

        // delay
        double delay = (time + WSN.SIFS + WSN.tACK) - this.startTX;
        this.delayList.add(delay);
        this.startTX = 0;
        if (WSN.debug){ System.out.println("Delay = "+ delay); }

    }

    public ArrayList<Double> getTotalTimeList() { return this.totalTimeList; }
    public ArrayList<Double> getDelayList() { return this.delayList; }


    public void CONTIaddRound(){ this.CONTIroundCounter ++;}

    public void CONTIsetTotalTime(){
        CONTIaddRound();
        if (WSN.debug) {System.out.println("Node "+this.getId()+" used "+this.CONTIroundCounter+" rounds to succeeds ");}
        double totalTime = this.CONTIroundCounter * (CONTIp.length * WSN.CONTIslotTime  + WSN.txTime) ;
        this.totalTimeList.add(totalTime);

        this.CONTIroundCounter = 0;

    }


        /********** mobility ******/
    public void move(int mobilityID)
    {
        double netRadius = WSN.getMaxRadius();
        double newX, newY, newDist;

        switch (mobilityID){
            case 0: // Gaussian noise

                // random displacement
                double sX = 50 * rng.nextGaussian();
                double sY = 50 * rng.nextGaussian();

                double mag = Math.sqrt(sX*sX + sY*sY);

                // compute new candidate position and new distance from center
                newX = X + sX;
                newY = Y + sY;
                newDist = Math.sqrt(newX*newX + newY*newY);

                if (newDist > netRadius){
                    // get direction to center of the cell
                    double dist = Math.sqrt(X*X + Y*Y);
                    double dirX = X / (dist*dist);
                    double dirY = Y / (dist*dist);

                    // move the position towards the center
                    newX = X + mag * dirX;
                    newY = Y + mag * dirY;
                }

                X = newX;
                Y = newY;

                break;
            case 1: // Gauss-Markov model

                // compute new speed and direction
                speed = alphaS * speed + (1-alphaS) * avgSpeed + Math.sqrt(1-Math.pow(alphaS,2)) * rng.nextGaussian();
                dir = alphaD * dir + (1-alphaD) * avgDir + Math.sqrt(1-Math.pow(alphaD,2)) * rng.nextGaussian();

                if (Math.abs(dir) > 360) {
                    dir = dir % 360;
                }

                //System.out.print(id + " " + speed + " " + dir + "\n");
                // compute new candidate position
                newX = X + speed * Math.cos(Math.toRadians(dir));
                newY = Y + speed * Math.sin(Math.toRadians(dir));

                // compute the displacement of the node from the initial position
                double range = Math.sqrt(Math.pow(newX-X0,2) + Math.pow(newY-Y0,2));
                newDist = Math.sqrt(newX * newX + newY * newY);

                double maxRange = netRadius/4;

                // check if the new position is inside the network
                switch (WSN.getTopologyID()) {

                    case 0:     // circular topology

                        if (range > maxRange || newDist > netRadius) { // if point exceeds range of movement or network I move in the opposite direction

                            do{
                                dir = rng.nextInt(360);
                                newX = X + speed * Math.cos(Math.toRadians(dir));
                                newY = Y + speed * Math.sin(Math.toRadians(dir));

                                range = Math.sqrt(Math.pow(newX-X0,2) + Math.pow(newY-Y0,2));
                                newDist = Math.sqrt(newX * newX + newY * newY);

                            }while(!(range <= maxRange && newDist <= netRadius));

                            avgDir -= 180;
                            if (Math.abs(avgDir) > 360) {
                                avgDir = avgDir % 360;
                            }
                        }

                        X = newX;
                        Y = newY;

                        break;

                    case 1:     // hexagonal topology

                        Path2D hexagon = new Path2D.Double();
                        Point2D newPos = new Point2D.Double(newX, newY);

                        // initial point
                        hexagon.moveTo(netRadius * Math.cos(Math.PI / 6), netRadius * Math.sin(Math.PI / 6));

                        for (int i = 1; i < 6; i++) {
                            hexagon.lineTo(netRadius * Math.cos((2 * i + 1) * Math.PI / 6), netRadius * Math.sin((2 * i + 1) * Math.PI / 6));
                        }
                        hexagon.closePath();

                        if (range > maxRange || !hexagon.contains(newPos)) { // if point exceeds range of movement or network I move in the opposite direction

                            do{
                                dir = rng.nextInt(360);
                                newX = X + speed * Math.cos(Math.toRadians(dir));
                                newY = Y + speed * Math.sin(Math.toRadians(dir));

                                range = Math.sqrt(Math.pow(newX-X0,2) + Math.pow(newY-Y0,2));

                                hexagon = new Path2D.Double();
                                newPos = new Point2D.Double(newX, newY);

                                hexagon.moveTo(netRadius * Math.cos(Math.PI / 6), netRadius * Math.sin(Math.PI / 6));

                                for (int i = 1; i < 6; i++) {
                                    hexagon.lineTo(netRadius * Math.cos((2 * i + 1) * Math.PI / 6), netRadius * Math.sin((2 * i + 1) * Math.PI / 6));
                                }
                                hexagon.closePath();

                            }while(!(range <= maxRange && hexagon.contains(newPos)));

                            avgDir -= 180;
                            if (Math.abs(avgDir) > 360) {
                                avgDir = avgDir % 360;
                            }
                        }

                        X = newX;
                        Y = newY;

                        break;
                    }

                break;

            case 2: // NO change in the position
                break;
        }




    }

}
