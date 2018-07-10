package mock.answers;

import method.AttributeClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/26/18.
 */
public class MultipleMockAnswer implements Answer {

    public static final String INDEX_FIELD = "index_fdsafdsafds";

    private final Answer[] answers;
    private final Answer originalAnswer;

    public MultipleMockAnswer(Answer originalAnswer, int size) {
        answers = new Answer[size];
        this.originalAnswer = originalAnswer;
        for (int i = 0; i < answers.length; i++) {
            answers[i] = originalAnswer.duplicate();
        }
    }

    public static Answer getMultiAnswer(AttributeClass attributeClass, Answer answer) {
        return getMultiAnswer((int[]) attributeClass.getAttribute(AttributeClass.DIMENSIONS), answer);
    }

    public static Answer getMultiAnswer(int[] dimensions, Answer answer) {
        Answer multiAnswer = answer;
        for (int dimension : dimensions) {
            multiAnswer = new MultipleMockAnswer(multiAnswer, dimension);
        }
        return multiAnswer;
    }

    @Override
    public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
        return getCalledAnswer(proxy).handle(proxy, parameters, name, returnType);
    }

    @Override
    public Object handle(Object proxy, Object[] args, Method method) throws Throwable {
        return getCalledAnswer(proxy).handle(proxy, args, method);
    }

    @Override
    public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) throws Throwable {
        return getCalledAnswer(proxy).handle(proxy, args, originalMethod, method);
    }

    @Override
    public Object handle(Object[] args) throws Throwable {
        return null;
    }

    private Answer getCalledAnswer(Object proxy) {
        if (proxy == null) {
            return null;
        }
        Class<?> type = proxy.getClass();
        try {
            Field field = type.getDeclaredField(INDEX_FIELD);
            field.setAccessible(true);
            return answers[(int) field.get(proxy)];
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Index Field not declared on proxy object of type " + type);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't access Index Field on proxy object of type " + type);
        }
    }

    @Override
    public Answer duplicate() {
        return new MultipleMockAnswer(originalAnswer, answers.length);
    }

}
