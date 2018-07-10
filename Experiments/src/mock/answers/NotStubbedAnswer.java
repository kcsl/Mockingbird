package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class NotStubbedAnswer implements Answer {
    private NotStubbedAnswer() {
    }

    public static NotStubbedAnswer newInstance() {
        return new NotStubbedAnswer();
    }

    @Override
    public Object handle(Object proxy, Object[] args, Method method) {
        return throwExcept(method);
    }

    @Override
    public Object handle(Object[] args) {
        return throwExcept("static unknown method FIX?");
    }

    @Override
    public Answer duplicate() {
        return new NotStubbedAnswer();
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) {
        return throwExcept(method);
    }

    private Object throwExcept(Method method) {
        throw new NotStubbedException(method);
    }

    private Object throwExcept(String methodName) {
        throw new NotStubbedException(methodName);
    }

    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return throwExcept("Method " + name + " " + returnType + " is not stubbed");
    }
}
