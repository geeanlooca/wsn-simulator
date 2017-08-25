package protocols;

import java.lang.reflect.Constructor;

/**
 * Created by Gianluca on 24/08/2017.
 */
public interface Protocol {
    public Constructor<?> entryPoint() throws NoSuchMethodException;
}
