package WSN;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import WSN.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Node {

    private double X, Y;
    private int id;
    private Ellipse2D e;
    private java.awt.Color c;
    private double size;

    private WSN.NODE_STATUS status;
    private java.util.Queue<Packet> buffer;
    private java.util.List<Packet> transmittedPackets;


    private int BOcounter;
    private int CW;

    public boolean freeChannel;
    public boolean collided;

    private int transmCounter = 0;
    private int collCounter = 0;

    private int slotCounter = 0;
    private ArrayList<Integer> slotCounterList;


    public Node(int id, double X, double Y){
        this.X = X;
        this.Y = Y;
        this.id = id;
        this.size = 10;
        c = Color.blue;
        e = new Ellipse2D.Double(X, Y, size, size);
        buffer = new LinkedList<Packet>();
        this.slotCounterList = new ArrayList<Integer>();

        Random r = new Random();
        CW = WSN.CWmin;

        //BOcounter = 4;
        BOcounter = r.nextInt(CW + 1);
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
        e = new Ellipse2D.Double(X, Y, size, size);
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

    public void addTransmission(){ this.transmCounter ++; }

    public void addCollision(){ this.collCounter ++; }

    public int[] getCollisionParam(){
        int[] param = new int[2];
        param[0] = this.collCounter;
        param[1] = this.transmCounter;
        return param;
    }

    public void addContSlot(){
        this.slotCounter ++;
        //System.out.println("Node " + this.id + "  "+ this.slotCounter);
    }

    public void resetContSlot(){ this.slotCounter=0; }

    public void storeSlotNumber() {
        this.slotCounterList.add(this.slotCounter);
        this.slotCounter = 0;
        //System.out.println("Node " + this.id + "  "+ this.slotCounterList);

    }
    public ArrayList<Integer> getSlotCounterList() {
        return this.slotCounterList;
    }


}
