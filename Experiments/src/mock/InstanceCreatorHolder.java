package mock;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 5/29/18.
 */
public class InstanceCreatorHolder {
    private final Objenesis objenesis;
    private final Map<Class<?>, ObjectInstantiator<?>> objectInstantiatorMap;
    private InstanceCreatorHolder parentHolder;
    private List<InstanceCreatorHolder> childrenHolders;

    public InstanceCreatorHolder(InstanceCreatorHolder parentHolder) {
        objectInstantiatorMap = new HashMap<>();
        objenesis = parentHolder.objenesis;
        this.parentHolder = parentHolder;
        childrenHolders = new LinkedList<>();
    }

    public InstanceCreatorHolder() {
        objectInstantiatorMap = new HashMap<>();
        this.objenesis = new ObjenesisStd();
        parentHolder = null;
        childrenHolders = new LinkedList<>();
    }

    public void addChildHolder(InstanceCreatorHolder childHolder) {
        childHolder.parentHolder = this;
        childrenHolders.add(childHolder);
    }

    public ObjectInstantiator<?> createObjectInstantiator(Class<?> oldClass, Class<?> newClass) {
        ObjectInstantiator<?> objectInstantiator = objenesis.getInstantiatorOf(newClass);
        objectInstantiatorMap.put(oldClass, objectInstantiator);
        return objectInstantiator;
    }

    public void addObjectInstantiator(Class<?> tClass, ObjectInstantiator<?> objectInstantiator) {
        objectInstantiatorMap.put(tClass, objectInstantiator);
    }

    @SuppressWarnings("unchecked")
    public <T> void addFixedInstantiator(T value) {
        Class<T> tClass = (Class<T>) value.getClass();
        objectInstantiatorMap.put(tClass, (ObjectInstantiator<T>) () -> value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> tClass) {
        if (parentHolder != null) {
            try {
                return parentHolder.getInstance(tClass);
            } catch (RuntimeException ignored) {
            }
        }
        if (!objectInstantiatorMap.containsKey(tClass)) {
            throw new RuntimeException("Class " + tClass.getName() + " instance creator not found");
        }
        return (T) objectInstantiatorMap.get(tClass).newInstance();
    }


    public <T> T getInstanceFromAll(Class<T> tClass) {
        if (parentHolder != null) {
            return parentHolder.getInstanceFromAll(tClass);
        }
        return instanceFromAll(tClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T instanceFromAll(Class<T> tClass) {
        if (objectInstantiatorMap.containsKey(tClass)) {
            return (T) objectInstantiatorMap.get(tClass).newInstance();
        }
        for (InstanceCreatorHolder instanceCreatorHolder : childrenHolders) {
            try {
                return instanceCreatorHolder.instanceFromAll(tClass);
            } catch (RuntimeException ignored) {
            }
        }
        throw new RuntimeException("Class " + tClass.getName() + " instance creator not found");
    }

}
