package mock;

import org.objenesis.instantiator.ObjectInstantiator;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public interface MockCreator extends ObjectInstantiator<Object> {

    boolean isPrimitive();

    String getName();

    void setName(String name);

    Class<?> getType();

}
