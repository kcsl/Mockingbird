package mock;

import mock.answers.Answer;

import java.lang.reflect.Method;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class Rule {

    private Answer<?> answer;
    private Class<?> classType;
    private Method method;

    public Rule(Answer<?> answer, Class<?> type, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        this.answer = answer;
        this.classType = type;
        this.method = type.getMethod(methodName, paramTypes);
    }

    public Rule(Answer<?> answer, Method method) {
        this.answer = answer;
        this.classType = method.getDeclaringClass();
        this.method = method;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public Answer<?> getAnswer() {
        return answer;
    }

    public Method getMethod() {
        return method;
    }

}
