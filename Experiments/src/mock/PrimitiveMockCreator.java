package mock;

import org.objenesis.instantiator.ObjectInstantiator;

/**
 * @author Derrick Lockwood
 * @created 11/26/18.
 */
public class PrimitiveMockCreator implements MockCreator {
    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public ObjectInstantiator<?> getObjectInstantiator(ClassMap classMap) throws Exception {
        return null;
    }

    @Override
    public Class<?> loadClass() throws ClassNotFoundException {
        return null;
    }
}
