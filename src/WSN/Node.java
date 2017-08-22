package WSN;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Node {

    // coordinates, color and size of the node
    private double X, Y;
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

    public boolean freeChannel;
    public boolean collided;

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


    private ArrayList<Node> neighborList;


    public Node(int id, double X, double Y){
        this.X = X;
        this.Y = Y;
        this.id = id;
        this.size = 10;
        c = Color.blue;
        e = new Ellipse2D.Double(X-size/2, Y-size/2, size, size);
        buffer = new LinkedList<Packet>();
        collided = false;

        transCounter = 0;
        collCounter = 0;
        slotCounter = 0;
        startTX =0;
        this.slotCounterList = new ArrayList<Integer>();
        this.totalTimeList = new ArrayList<Double>();
        this.delayList = new ArrayList<Double>();
        this.nodeLog = new ArrayList<Boolean>();

        this.neighborList = new ArrayList<Node>();


        Random r = new Random();
        CW = WSN.CWmin;

        BOcounter = r.nextInt(CW + 1);
        //BOcounter = 3;
    }

    public double getX(){
        return this.X;
    }

    public double getY(){
        return this.Y;
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
        this.CW = CW;
    }

    public void addNeighbor( Node node){
        this.neighborList.add(node);
    }

    public ArrayList<Node> getNeighborList(){
        return this.neighborList;
    }


    // methods to calculate Collision Rate and Fairness

    public void addTransmission(){
        this.transCounter++;
        this.nodeLog.add(true);
    }

    public void addCollision(){
        this.collCounter ++;
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
    }

    public Boolean getLog() {
        return this.iterator.next();
    }




    // methods to calculate the Average Number of Contention Slots

    public void addContSlot(){
        this.slotCounter ++;
        if (WSN.debug){ System.out.println("Slot Counter: \t"+ this.slotCounter);}
    }

    public void resetContSlot(){ this.slotCounter=0; }

    public void storeContSlotNumber() {
        this.slotCounterList.add(this.slotCounter);
        this.slotCounter = 0;
        if (WSN.debug){ System.out.println("Slot Counter List: \t"+ this.slotCounterList);}

    }
    public ArrayList<Integer> getSlotCounterList() { return this.slotCounterList; }


    // methods to calculate the packet total transmission time useful for Throughput and Delay

    public void addDIFS(){ this.totalTime += WSN.DIFS; }
    public void addtSlot(){ this.totalTime += WSN.tSlot; }
    public void addTX(){ this.totalTime += WSN.txTime; }

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
