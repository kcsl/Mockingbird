package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/22/18.
 */
public interface ParameterAnswer extends BasicAnswer {

    Object applyParameters(Object[] parameters);

    @Override
    default Object apply(Object proxy, Object[] params, Class<?> returnType) {
        return applyParameters(params);
    }
}
