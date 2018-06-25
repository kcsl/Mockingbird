package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/22/18.
 */
public interface ParameterAnswer extends Answer {

    Object getObject(Object[] parameters);

    @Override
    default Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return getObject(parameters);
    }

    @Override
    default Object handle(Object[] args) {
        return getObject(args);
    }

    @Override
    default Object handle(Object proxy, Object[] args, Method method) {
        return getObject(args);
    }

    @Override
    default Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) {
        return getObject(args);
    }
}
