package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/12/18.
 */
public interface ReturnTypeAnswer extends BasicAnswer {

    //TODO: Fix force reload it doesn't really make sense to have it here... look into finding way to adjust. Maybe AnnotatedClass?
    Object applyReturnType(Class<?> returnType, boolean forceReload);

    @Override
    default Object apply(Object proxy, Object[] params, Class<?> returnType) {
        return applyReturnType(returnType, true);
    }
}
