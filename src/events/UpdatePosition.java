package events;

import WSN.WSN;
import WSN.Node;
import WSN.RNG;
import WSN.Scheduler;
import WSN.Channel;


/**
 * Created by Gianluca on 25/08/2017.
 */
public class UpdatePosition extends Event {

    public UpdatePosition(double time){ super(time); }

    public void run(){
        RNG r = RNG.getInstance();
        Scheduler sc = Scheduler.getInstance();

        for (Node n :
                WSN.nodes) {

            double X0 = n.getX();
            double Y0 = n.getY();

            // random displacement
            double sX = 50*r.nextGaussian();
            double sY = 50*r.nextGaussian();

            double mag = Math.sqrt(sX*sX + sY*sY);

            double newX = X0 + sX;
            double newY = Y0 + sY;
            double dist = Math.sqrt(X0*X0 + Y0*Y0);
            double newDist = Math.sqrt(newX*newX + newY*newY);
            double radius = WSN.getMaxRadius();

            if (newDist > radius){
                // get direction to center of the cell
                double dirX = X0 / (dist*dist);
                double dirY = Y0 / (dist*dist);

                newX = X0 + mag * dirX;
                newY = Y0 + mag * dirY;

            }

            n.setX(newX);
            n.setY(newY);

            n.clearNeighbors();
        }

        for (Node nodeA : WSN.nodes) {
            for (Node nodeB : WSN.nodes) {
                if (nodeB.getId() != nodeA.getId()) {
                    Channel channel = new Channel(nodeA, nodeB, WSN.Ptx);

                    double Prx = channel.getPrx();
                    //System.out.println(Prx);

                    if (Prx >= WSN.PrxThreshold && !(nodeB.findNeighbor(nodeA))) {
                        nodeA.addNeighbor(nodeB);
                        nodeB.addNeighbor(nodeA);
                    }
                }
            }
        }

        sc.schedule(new UpdatePosition(time + 1000));
    }

    @Override
    public String toString(){
        return "[" + time + "][UpdatePosition]";
    }
}