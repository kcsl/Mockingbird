package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class NotStubbedAnswer implements SubAnswer, RedefineAnswer, StaticAnswer {
    private NotStubbedAnswer() {
    }

    @Override
    public Object handle(Object proxy, Object[] args, Method method) throws Throwable {
        return throwExcept(method);
    }

    @Override
    public Object handle(Object[] args) throws Throwable {
        return throwExcept("static unknown method FIX?");
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) throws Throwable {
        return throwExcept(method);
    }

    private Object throwExcept(Method method) {
        return throwExcept(method.getName());
    }

    private Object throwExcept(String methodName) {
        throw new RuntimeException("Method " + methodName + " is not stubbed");
    }

    public static NotStubbedAnswer newInstance() {
        return new NotStubbedAnswer();
    }


    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return throwExcept("Method " + name + " " + returnType.toString() + " is not stubbed");
    }
}
