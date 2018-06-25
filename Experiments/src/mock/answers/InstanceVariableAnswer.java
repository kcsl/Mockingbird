package mock.answers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/23/18.
 */
public class InstanceVariableAnswer implements Answer {

    private final Object setField;
    private final String fieldName;

    public InstanceVariableAnswer(String fieldName, Object setField) {
        this.fieldName = fieldName;
        this.setField = setField;
    }

    public InstanceVariableAnswer(String fieldName) {
        this(fieldName, null);
    }

    private Object getField(Object proxy){
        Class<?> type = proxy.getClass();
        try {
            Field field = type.getDeclaredField(fieldName);
            if (setField != null) {
                field.set(proxy, setField);
            }
            return field.get(proxy);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) {
        return getField(proxy);
    }

    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return getField(proxy);
    }

    @Override
    public Object handle(Object[] args) {
        return null;
    }

    @Override
    public Object handle(Object proxy, Object[] args, Method method) {
        return getField(proxy);
    }
}
