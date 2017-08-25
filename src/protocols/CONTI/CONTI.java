package protocols.CONTI;
import protocols.CONTI.StartRound;
import protocols.Protocol;
import WSN.Node;

import java.lang.reflect.Constructor;

/**
 * Created by Gianluca on 25/08/2017.
 */
public class CONTI implements Protocol {
    public Constructor<?> entryPoint() throws NoSuchMethodException {
        return StartRound.class.getConstructor(Node.class, Double.class);
    }
}
