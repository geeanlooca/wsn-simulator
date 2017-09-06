package protocols.GALTIER;
import WSN.Node;
import protocols.GALTIER.StartRound;
import protocols.Protocol;

import java.lang.reflect.Constructor;

/**
 * Created by Gianluca on 25/08/2017.
 */
public class GALTIER implements Protocol {
    public Constructor<?> entryPoint() throws NoSuchMethodException {
        return StartRound.class.getConstructor(Node.class,Double.class);
    }
}
