package mock;

import org.objenesis.instantiator.ObjectInstantiator;

/**
 * @author Derrick Lockwood
 * @created 2019-03-25.
 */
public class StoredMock {
    private final TransformMockClass transformMockClass;
    private ClassMap classMap;
    private ObjectInstantiator<?> objectInstantiator;
    private boolean reload;

    public StoredMock(String canonicalName, ClassMap classMap) {
        this.transformMockClass = new TransformMockClass(canonicalName);
        this.classMap = classMap;
        reload = true;
    }

    public void setClassMap(ClassMap classMap) {
        this.classMap = classMap;
    }

    public void reset() {
        reload = true;
    }

    public ObjectInstantiator<?> getObjectInstantiator() throws ClassNotFoundException {
        if (objectInstantiator == null || reload) {
            objectInstantiator = transformMockClass.getObjectInstantiator(classMap);
            reload = false;
        }
        return objectInstantiator;
    }
}
