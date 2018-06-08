package mock.answers.auto;

import mock.answers.RedefineAnswer;
import mock.answers.StaticAnswer;
import mock.answers.SubAnswer;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public interface AutoAnswer extends SubAnswer, RedefineAnswer, StaticAnswer {
    //TODO: Abstract all auto answers
    void mutate();

    Object createObject(Class<?> type);

    @Override
    default Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) {
        return createObject(method.getReturnType());
    }

    @Override
    default Object handle(Object proxy, Object[] args, Method method) {
        return createObject(method.getReturnType());
    }

    @Override
    default Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return createObject(returnType);
    }

    @Override
    default Object handle(Object[] args) {
        //TODO: Fix static assignment
        return null;
    }

}
