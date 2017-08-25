package WSN;

import java.util.*;

public class Channel {

    private RNG rnd = RNG.getInstance();

    private double Ptx;         // transmitted power
    private double eta = 4;     // path loss exponent
    private double K;           // free space loss
    private double d;           // distance between nodes

    private double sigma_dB = 8;   // variance for shadowing in dB (dB spread in literature)
    private double sigma = 0.1*Math.log(10)*sigma_dB;   // variance for shadowing (relation from the paper)

    private double f = 2.4 * Math.pow(10,6);    // frequency in Hz
    private double lambda = 2 * Math.PI / f;
    private double d0 = 1;      // reference distance



    public Channel(Node nodeA, Node nodeB, double Ptx)
    {

        this.Ptx = Ptx;

        double xA = nodeA.getX();
        double yA = nodeA.getY();
        double xB = nodeB.getX();
        double yB = nodeB.getY();

        d = Math.sqrt(Math.pow(xB-xA,2) + Math.pow(yB-yA,2));

        K = lambda/(4 * Math.PI * d0);

    }

    public double getPrx()
    {
        // generate shadowing term
        double xi = sigma * rnd.nextGaussian();
        double shad = 1; //Math.exp(xi);

        // generate Rayleigh fading
        double R2 = - Math.log(rnd.nextDouble());

        //compute the received power
        double Prx = Ptx * R2 * shad * K * Math.pow(d/d0,-eta);

        return Prx;
    }

    public void setPtx(double Ptx)
    {
        this.Ptx = Ptx;
    }

    public void setEta(double eta)
    {
        this.eta = eta;
    }

    public double getDistance()
    {
        return d;
    }
}
