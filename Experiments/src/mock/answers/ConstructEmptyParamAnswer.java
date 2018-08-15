package mock.answers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Derrick Lockwood
 * @created 8/7/18.
 */
public class ConstructEmptyParamAnswer implements ReturnTypeAnswer {
    @Override
    public Object createObject(Class<?> returnType, boolean forceReload) {
        try {
            Constructor<?> c = returnType.getConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Answer duplicate() {
        return new ConstructEmptyParamAnswer();
    }
}
