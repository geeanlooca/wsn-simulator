import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Node {
    private double X, Y;
    private int id;
    private Ellipse2D e;
    private java.awt.Color c;
    private double size;

    public Node(int id, double X, double Y){
        this.X = X;
        this.Y = Y;
        this.id = id;
        this.size = 10;

        c = Color.blue;

        e = new Ellipse2D.Double(X, Y, size, size);
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
}
