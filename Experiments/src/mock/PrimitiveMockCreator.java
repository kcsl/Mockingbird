package mock;

import mock.answers.Answer;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class PrimitiveMockCreator implements MockCreator {
    private Class<?> type;
    private TargetedMockBuilder builder;
    private ObjectInstantiator<?> objectInstantiator;

    private PrimitiveMockCreator(TargetedMockBuilder builder, Class<?> type, Answer answer) {
        this.builder = builder;
        this.type = type;
        objectInstantiator = (ObjectInstantiator<Object>) () -> {
            try {
                return answer.handle(null, null, null, type);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable.getCause());
            }
        };
    }

    @Override
    public Object create() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return objectInstantiator.newInstance();
    }

    @Override
    public void store() throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException {
        builder.setObjectInstantiator(type, objectInstantiator);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    public static PrimitiveMockCreator create(TargetedMockBuilder builder, Class<?> type, Answer answer) {
        if (type.isAssignableFrom(int.class) ||
                type.isAssignableFrom(long.class) ||
                type.isAssignableFrom(float.class) ||
                type.isAssignableFrom(double.class) ||
                type.isAssignableFrom(boolean.class) ||
                type.isAssignableFrom(char.class) ||
                type.isAssignableFrom(byte.class) ||
                type.isAssignableFrom(String.class)
                ) {
            return new PrimitiveMockCreator(builder, type, answer);
        }
        return null;
    }

}
