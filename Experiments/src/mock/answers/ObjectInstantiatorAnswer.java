package mock.answers;

import org.objenesis.instantiator.ObjectInstantiator;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class ObjectInstantiatorAnswer implements ReturnTypeAnswer {

    private final ObjectInstantiator<?> objectInstantiator;
    private Object object;

    public ObjectInstantiatorAnswer(ObjectInstantiator<?> objectInstantiator) {
        object = null;
        this.objectInstantiator = objectInstantiator;
    }

    @Override
    public Object createObject(Class<?> returnType, boolean forceReload) {
        if (object != null && !forceReload) {
            return object;
        }
        if (objectInstantiator != null) {
            object = objectInstantiator.newInstance();
        }
        return object;
    }
}
