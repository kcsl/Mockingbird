package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class OriginalMethodAnswer implements Answer {
    @Override
    public Object handle(Object proxy, Object[] args, Method method) {
        return null;
    }

    @Override
    public Object handle(Object[] args) {
        return null;
    }

    @Override
    public Answer duplicate() {
        return new OriginalMethodAnswer();
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) throws Throwable {
        return originalMethod.call();
    }

    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return null;
    }
}
