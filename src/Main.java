import WSN.*;
import protocols.DCF.DCF;
import protocols.Protocol;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Main {
    public static void main (String [] args) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        // network size and topology number
        int netW, netH, topologyID, mobilityID;
        netW = 1800;
        netH = 1800;
        topologyID = 0;
        mobilityID = 0;


        double seconds = 1e6;
        double minutes = seconds * 60;
        double simulationTime =  1 * minutes;

        int nodeCount = 20;

        boolean gui = true;
        boolean debugging = false;
        int delay =50;

        Protocol p = new protocols.CONTI.CONTI();
        WSN netw = new WSN(nodeCount, netW, netH, p, topologyID, mobilityID, gui);
        netw.setPanelSize(600, 600);


        netw.debugging(debugging);
        netw.setAnimationDelay(delay);


        System.out.println("Starting simulation...");
        long startTime = System.currentTimeMillis();

        netw.run(simulationTime/5.0);
        WSN.printCollisionRate();

        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Simulation time: " + totalTime/1000.0 + "s");
    }
}

