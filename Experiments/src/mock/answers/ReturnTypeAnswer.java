package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/12/18.
 */
public interface ReturnTypeAnswer extends Answer {

    Object createObject(Class<?> returnType, boolean forceReload);

    @Override
    default Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return createObject(returnType, false);
    }

    @Override
    default Object handle(Object[] args) {
        return null;
    }

    @Override
    default Object handle(Object proxy, Object[] args, Method method) {
        return createObject(method.getReturnType(), false);
    }

    @Override
    default Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) {
        return createObject(method.getReturnType(), false);
    }
}
