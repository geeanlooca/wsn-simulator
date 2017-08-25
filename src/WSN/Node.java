package WSN;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import WSN.RNG;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Node {

    // coordinates, color and size of the node
    private double X;
    private double Y;
    private int id;
    private Ellipse2D e;
    private java.awt.Color c;
    private double size;

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


    //
    //  Methods
    //

    public Node(int id, double X, double Y){
        setX(X);
        setY(Y);
        this.id = id;
        this.size = 10;
        c = Color.blue;
        e = new Ellipse2D.Double(X-size/2, Y-size/2, size, size);
        buffer = new LinkedList<Packet>();
        collided = false;

        collidedNodes = new ArrayList<Node>();
        resumingNodes = new ArrayList<Node>();
        this.neighborList = new ArrayList<Node>();

        transCounter = 0;
        collCounter = 0;
        slotCounter = 0;
        startTX =0;
        this.slotCounterList = new ArrayList<Integer>();
        this.totalTimeList = new ArrayList<Double>();
        this.delayList = new ArrayList<Double>();
        this.nodeLog = new ArrayList<Boolean>();

        this.neighborList = new ArrayList<Node>();
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

    public Ellipse2D getEllipse(){
        return this.e;
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
        e = new Ellipse2D.Double(X-size/2, Y-size/2, size, size);
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
        return (neighborList.indexOf(node) > 0);
    }

    // output parameters

    // methods to calculate Collision Rate and Fairness

    public void addTransmission(){
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

}
