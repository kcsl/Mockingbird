package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 8/8/18.
 */
public interface BasicAnswer extends Answer {

    Object apply(Object proxy, Object[] params, Class<?> returnType);

    @Override
    default Object handle(Object[] args) {
        return apply(null, args, null);
    }

    @Override
    default Object handle(Object proxy, Object[] args, Method method) {
        return apply(proxy, args, method.getReturnType());
    }

    @Override
    default Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return apply(proxy, parameters, returnType);
    }

    @Override
    default Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) {
        return apply(proxy, args, method.getReturnType());
    }

    static BasicAnswer transform(Answer answer) {
        //TODO: make better? or not at all for applying
        return new BasicAnswer() {
            @Override
            public Object apply(Object proxy, Object[] params, Class<?> returnType) {
                return answer.handle(proxy, params, returnType.getName(), returnType);
            }

            @Override
            public Answer duplicate() {
                return answer.duplicate();
            }
        };
    }
}
