package WSN;

import java.util.*;

public class Channel {

    private RNG rnd = RNG.getInstance();

    private double Ptx;         // transmitted power (dBm)
    private double d;           // distance between nodes
    private boolean indoor;

    private double sigma_dB = 8;   // variance for shadowing in dB (dB spread in literature)
    private double sigma = 0.1*Math.log(10)*sigma_dB;   // variance for shadowing (relation from the paper)

    private double f = 2.4 * Math.pow(10,6);    // frequency in Hz
    private double lambda = 2 * Math.PI / f;
    private double d0 = 50;      // reference distance



    public Channel(Node nodeA, Node nodeB, double Ptx, boolean indoor)
    {

        this.Ptx = Ptx;

        this.indoor = indoor;

        double xA = nodeA.getX();
        double yA = nodeA.getY();
        double xB = nodeB.getX();
        double yB = nodeB.getY();

        d = Math.sqrt(Math.pow(xB-xA,2) + Math.pow(yB-yA,2));
    }

    public double getPrx()
    {
        double Prx = 0;

        if(indoor)
        {

        }
        else {
            double dbr = 35;
            double n1 = 1.5;
            double n2 = 3.7;
            double L1 = 68;
            double L2 = 65;
            double sigma1_dB = 2.8;
            double sigma1 = Math.pow(10,sigma1_dB/10);
            double sigma2_dB = 1.6;
            double sigma2 = Math.pow(10,sigma2_dB/10);

            double x,L;

            if (d <= dbr){
                x = sigma1 * rnd.nextGaussian();
                L = L1 + 10 * n1 * Math.log10(d/dbr) + x;
            }
            else{
                x = sigma2 * rnd.nextGaussian();
                L = L2 + 10 * n2 * Math.log10(d/dbr) + x;
            }

            Prx = Ptx - L;

        }

        return Prx;
    }

    public void setPtx(double Ptx)
    {
        this.Ptx = Ptx;
    }

    public double getDistance()
    {
        return d;
    }
}
