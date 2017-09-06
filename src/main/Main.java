package main;

import WSN.*;
import protocols.*;
import protocols.CONTI.CONTI;
import protocols.DCF.DCF;
import protocols.GALTIER.GALTIER;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by Gianluca on 16/07/2017.
 */
public class Main {
    public static void main (String [] args) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        // set locale to US to format decimal numbers with a dot instead of comma
        Locale.setDefault(Locale.US);

        // network size and topology number
        int networkWidth, networkHeight, topologyID, mobilityID, delay, nodeCount, framesize;

        double seconds = 1e6;
        double minutes = seconds * 60;
        double simulationTime =  2 * minutes;

        boolean gui;
        boolean debugging;

        Protocol p;

        /* command line parsing */

        // network size
        try{
            networkHeight = Integer.parseInt(System.getProperty("height"));
            networkWidth = Integer.parseInt(System.getProperty("width"));

            if (networkHeight <= 0 || networkWidth <= 0){
                System.out.println("Invalid network size.");
                System.exit(1);
            }

        }catch (Exception e){
            networkWidth = 100;
            networkHeight = 100;
        }

        // node count
        try{
            nodeCount = Integer.parseInt(System.getProperty("nodes"));
            if (nodeCount < 2){
                System.out.println("Invalide number of nodes.");
                System.exit(1);
            }
        }catch (Exception e){
            nodeCount = 15;
        }

        // mobility
        try{
            mobilityID = Integer.parseInt(System.getProperty("mobility"));
            if (mobilityID < 0 || mobilityID > 2){
                System.out.println("Invalid mobility model.");
                System.exit(1);
            }
        }catch (Exception e){
            mobilityID = 2;
        }

        // topology
        try{
            topologyID = Integer.parseInt(System.getProperty("topology"));
            if (topologyID < 0 || mobilityID > 1){
                System.out.println("Invalid topology model.");
                System.exit(1);
            }
        }catch (Exception e){
            topologyID = 0;
        }

        // framesize
        framesize = 1500;
        try{
            framesize = Integer.parseInt(System.getProperty("framesize"));
            if (framesize <= 0){
                System.out.printf("Invalid frame size.");
                System.exit(1);
            }

        }catch (Exception e){
            framesize = 1500;
        }

        // simulation time
        try{
            simulationTime = Integer.parseInt(System.getProperty("time"));
            simulationTime *= seconds;

            if (simulationTime <= 0){
                System.out.println("Invalid simulation time.");
                System.exit(1);
            }
        }catch (Exception e){
            simulationTime = 2*minutes;
        }

        // debugging
        String debugString = "";
        try{
            debugString = System.getProperty("debug");
            debugging = debugString.equals("true");
        }catch (Exception e){
            debugging = false;
        }

        // delay
        try{
            delay = Integer.parseInt(System.getProperty("delay"));
            if (delay < 0){
                System.out.println("Invalid delay.");
                System.exit(1);
            }
        }catch (Exception e){
            delay = 0;
        }

        // gui
        String guiString = "";
        try{
            guiString = System.getProperty("gui");
            gui = guiString.equals("true");
        }catch (Exception e){
            gui = false;
        }

        // protocol
        String protoc = "";
        try{
            protoc = System.getProperty("protocol");
            switch (protoc) {
                case "DCF":
                    p = new DCF();
                    break;
                case "CONTI":
                    p = new CONTI();
                    break;
                case "GALTIER":
                    p = new GALTIER();
                    break;
                default:
                    p = new DCF();
                    break;
            }
        }catch (Exception e){
            p = new DCF();
        }

        // output file
        String file = "";
        file = System.getProperty("output");
        if (file == null){
            file = "./results/simulations/default.csv";
        }

        WSN netw = new WSN(nodeCount, networkWidth, networkHeight, p, topologyID, mobilityID, gui);
        netw.setFrameSize(framesize);
        netw.setPanelSize(600, 600);

        netw.debugging(debugging);
        netw.setAnimationDelay(delay);

        System.out.println("Starting simulation...");
        long startTime = System.currentTimeMillis();

        netw.run(simulationTime);
        WSN.saveToFile(file);

        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Simulation time: " + totalTime/1000.0 + "s");
    }
}

