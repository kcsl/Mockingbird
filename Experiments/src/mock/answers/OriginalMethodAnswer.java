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
        throw new InvalidHandleException();
    }

    @Override
    public Object handle(Object[] args) {
        throw new InvalidHandleException();
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
        throw new InvalidHandleException();
    }

    class InvalidHandleException extends RuntimeException {
        public InvalidHandleException() {
            super("Invalid handle method in Answer ");
        }
    }

    public static OriginalMethodAnswer newInstance() {
        return new OriginalMethodAnswer();
    }
}
