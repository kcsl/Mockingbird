package mock.answers;

import java.lang.reflect.Method;

/**
 * @author Derrick Lockwood
 * @created 6/19/18.
 */
public class NotStubbedException extends RuntimeException {

    public final String methodName;

    public NotStubbedException(Method method) {
        this(method.getName());
    }

    public NotStubbedException(String methodName) {
        super("Method " + methodName + " is not stubbed");
        this.methodName = methodName;
    }
}
