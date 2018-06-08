package mock;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public interface MockCreator {
    Object create() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    void store() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException;

    boolean isPrimitive();
}
