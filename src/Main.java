import WSN.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Main {
    public static void main (String [] args){

        // network size and topology number
        int netW, netH, topologyID;
        netW = 1800;
        netH = 1800;
        topologyID = 1;

        double seconds = 1e6;
        double minutes = seconds * 60;
        double simulationTime =  5 * minutes;

        int nodeCount = 5;


        System.out.println("Starting simulation...");

        WSN netw = new WSN(nodeCount, netW, netH,topologyID);


        boolean gui = false;
        boolean debugging = false;
        int delay = 0;


        // panel to visualize the network nodes
        int panelW = 500;
        int panelH = 530;


        if (gui) {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.getContentPane().add(new WSNWindow(netw));
            f.setSize(panelW,panelH);
            f.setLocation(200,200);
            f.setVisible(true);

            debugging = true;
            delay = 500;
        }

        netw.debugging(debugging);
        netw.setAnimationDelay(delay);


        long startTime = System.currentTimeMillis();

        netw.run(simulationTime/5.0);
        WSN.printCollisionRate();

        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Simulation time: " + totalTime/1000.0 + "s");


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

            // find the panel dimension
            double panelH = getHeight();
            double panelW = getWidth();

            int topologyID = network.getTopologyID();
            double[] networkSize = network.getNetworkSize();
            double netW = networkSize[0];
            double netH = networkSize[1];

            // scaling factors only to draw the nodes
            double scaleX = 0.9 * panelW/netW;
            double scaleY = 0.9 * panelH/netH;

            double nodeX, nodeY, nodeSize;
            for (int i = 0; i < network.nodeCount(); i++) {

                Node n = network.getNodes().get(i);
                Ellipse2D e = n.getEllipse();

                nodeX = panelW/2 + (e.getX() - netW/2) * scaleX;
                nodeY = panelH/2 + (e.getY() - netH/2) * scaleY;
                nodeSize = n.getSize();

                e = new Ellipse2D.Double(nodeX,nodeY,nodeSize,nodeSize);
                g2.setPaint(n.getColor());
                g2.fill(e);

                Font font = new Font("Serif", Font.PLAIN, 18);
                g2.setFont(font);
                g2.setColor(Color.black);
                g2.drawString(String.valueOf(n.getId()), (int) nodeX, ((int) nodeY)-3);

            }

            g2.setPaint(Color.black);

            switch (topologyID){
                // circular cell
                case 0:
                    g2.setPaint(Color.black);
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

        repaint();
    }
}