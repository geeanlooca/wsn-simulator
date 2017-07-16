import WSN.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Main {
    public static void main (String [] args){

        int W, H;
        W = 500;
        H = 500;

        int nodeCount = 60;

        WSN netw = new WSN(nodeCount, W, H);
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new WSNWindow(netw));
        f.setSize(W,H);
        f.setLocation(200,200);
        f.setVisible(true);


        netw.run();

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

        repaint();
    }
}