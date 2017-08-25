package WSN;

import java.util.Random;

/**
 * Created by Gianluca on 24/08/2017.
 */


public class RNG {
    private static RNG instance;
    private Random rnd;

    private RNG() {
        rnd = new Random();
    }

    public static RNG getInstance() {
        if(instance == null) {
            instance = new RNG();
        }
        return instance;
    }

    public double nextDouble() {
        return rnd.nextDouble();
    }

    public int nextInt(){
        return rnd.nextInt();
    }

    public double nextGaussian(){
        return rnd.nextGaussian();
    }

    public int nextInt(int max){
        return rnd.nextInt(max);
    }
}
