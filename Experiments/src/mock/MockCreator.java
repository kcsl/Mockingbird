package mock;

import mock.answers.Answer;
import org.objenesis.instantiator.ObjectInstantiator;

/**
 * @author Derrick Lockwood
 * @created 11/7/18.
 */
public interface MockCreator {

    boolean isPrimitive();

    //TODO: Change to be more specific than just Exception
    ObjectInstantiator<?> getObjectInstantiator(ClassMap classMap) throws Exception;

    Class<?> loadClass() throws ClassNotFoundException;

}
