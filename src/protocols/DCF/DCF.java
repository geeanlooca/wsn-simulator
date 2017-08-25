package protocols.DCF;
import WSN.Node;
import protocols.Protocol;

import java.lang.reflect.Constructor;

/**
 * Created by Gianluca on 25/08/2017.
 */
public class DCF implements Protocol {
    public Constructor<?> entryPoint() throws NoSuchMethodException {
        return StartListeningEvent.class.getConstructor(Node.class,Double.class);
    }
}
