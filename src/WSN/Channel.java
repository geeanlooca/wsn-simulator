package WSN;

import java.util.*;

public class Channel {

    private RNG rnd = RNG.getInstance();

    private double Ptx;         // transmitted power (dBm)
    private double d;           // distance between nodes
    private boolean indoor;

    private double f = 2.4 * Math.pow(10,6);    // frequency in Hz
    private double lambda = 2 * Math.PI / f;



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
        double L,X;

        if(indoor)
        {
            double L0 = 20 * Math.log10(4 * Math.PI/lambda);
            double n = 3;
            double sigma_dB = 8;
            //double sigma = Math.pow(10,sigma_dB/10);
            X = Math.sqrt(sigma_dB) * rnd.nextGaussian();

            L = L0 + 10 * n * Math.log10(d) + X;
        }
        else {
            double dbr = 32;      // distance breakpoint
            double n1 = 1.5;
            double n2 = 3.7;
            double L1 = 68;
            double L2 = 65;
            double sigma1_dB = 2.8;
            //double sigma1 = Math.pow(10,sigma1_dB/10);
            double sigma2_dB = 1.6;
            //double sigma2 = Math.pow(10,sigma2_dB/10);

            if (d <= dbr){
                X = Math.sqrt(sigma1_dB) * rnd.nextGaussian();
                L = L1 + 10 * n1 * Math.log10(d/dbr) + X;
            }
            else {
                X = Math.sqrt(sigma2_dB) * rnd.nextGaussian();
                L = L2 + 10 * n2 * Math.log10(d/dbr) + X;
            }
        }

        return Ptx - L;
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
