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
    private Answer answer;

    private PrimitiveMockCreator(TargetedMockBuilder builder, Class<?> type, Answer answer) {
        this.type = type;
        builder.setObjectInstantiator(type, this);
        this.answer = answer;
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

    @Override
    public Object newInstance() {
        try {
            if (answer == null) {
                throw new RuntimeException("Primitive not stubbed and no value is set " + type.getName());
            }
            return answer.handle(null, null, null, type);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable.getCause());
        }
    }
}
