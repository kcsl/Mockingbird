package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class FixedAnswer implements SubAnswer, RedefineAnswer, StaticAnswer {

    private Object value;

    private FixedAnswer(Object value) {
        this.value = value;
    }

    @Override
    public Object handle(Object proxy, Object[] args, Method method) throws Throwable {
        return value;
    }

    @Override
    public Object handle(Object[] args) throws Throwable {
        return value;
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) throws Throwable {
        return value;
    }

    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) throws Throwable {
        return value;
    }

    public static FixedAnswer newInstance(Object value) {
        return new FixedAnswer(value);
    }

}
