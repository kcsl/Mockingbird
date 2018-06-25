package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class FixedAnswer implements Answer {

    private final Object value;

    public FixedAnswer(Object value) {
        this.value = value;
    }


    @Override
    public Object handle(Object proxy, Object[] args, Method method) {
        return value;
    }

    @Override
    public Object handle(Object[] args) {
        return value;
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) {
        return value;
    }

    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return value;
    }

}
