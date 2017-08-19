import WSN.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Main {
    public static void main (String [] args){


        int W, H, topologyID;
        W = 500;
        H = 500;
        topologyID = 1;

        double seconds = 1e7;
        double minutes = seconds * 60;
        double simulationTime =  60 * minutes;


        System.out.println("Starting simulation...");

        WSN netw = new WSN(15, W, H,topologyID);
        netw.debugging(false);
        netw.setAnimationDelay(0);




        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new WSNWindow(netw));
        f.setSize(W,H);
        f.setLocation(200,200);
        f.setVisible(true);


        netw.run(simulationTime);
    }
}


class WSNWindow extends JPanel{

    Color selectedColor;

    private WSN network;

    public WSNWindow(WSN network){
        setBackground(Color.white);
        selectedColor = Color.blue;
        this.network = network;
    }

    protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < network.nodeCount(); i++) {

                Node n = network.getNodes().get(i);
                Ellipse2D e = n.getEllipse();
                g2.setPaint(n.getColor());
                g2.fill(e);
            }

            int topologyID = network.getTopologyID();
            double[] networkSize = network.getNetworkSize();
            double width = networkSize[0];
            double height = networkSize[1];

            g2.setPaint(Color.black);

            switch (topologyID){
                // circular cell
                case 0:
                    g2.setPaint(Color.black);
                    g2.draw(new Ellipse2D.Double(0.05 * width,0.05 * height,0.9 * width, 0.9 * height));
                    break;

                // hexagonal cell
                case 1:
                    Path2D hexagon = new Path2D.Double();
                    Point2D center = new Point2D.Double(width/2, height/2);
                    double r = 0.45 * Math.min(height, width);

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
                    g2.draw(new Rectangle2D.Double(1, 1, width - 2 , height - 3));
                    break;

            }

        repaint();
    }
}