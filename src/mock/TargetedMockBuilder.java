package mock;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TargetedMockBuilder {

    private final List<Object> finishedMockObjects;
    private Object currentMockObject;
    private Class<?> currentType;

    public TargetedMockBuilder() {
        finishedMockObjects = new ArrayList<>();
        currentMockObject = null;
        currentType = null;
    }

    public TargetedMockBuilder addObject(Object value) {
        if (currentMockObject == null) {
            finishedMockObjects.add(value);
        }
        return this;
    }

    public TargetedMockBuilder adjust(Object mockObject) {
        return adjust(mockObject, mockObject.getClass());
    }

    public TargetedMockBuilder adjust(Object mockObject, Class<?> typeBeforeMock) {
        if (currentMockObject == null) {
            currentMockObject = mockObject;
            currentType = typeBeforeMock;
        } else {
            throw new RuntimeException("Current Mock Object not stored ");
        }
        return this;
    }

    public TargetedMockBuilder mock(Class<?> type) {
        return mock(type, invocation -> {
            throw new RuntimeException(invocation.getMethod().getName() + " is not stubbed");
        });
    }

    public TargetedMockBuilder mock(Class<?> type, Answer<?> defaultAnswer) {
        if (currentMockObject == null) {
            currentMockObject = Mockito.mock(type, defaultAnswer);
            currentType = type;
        } else {
            throw new RuntimeException("Current Mock Object not stored ");
        }
        return this;
    }

    public TargetedMockBuilder apply(Rule rule) throws InvocationTargetException, IllegalAccessException {
        return apply(rule.getAnswer(), rule.getMethod());
    }

    public TargetedMockBuilder apply(Answer<?> answer, String methodName, Class<?>... parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (currentMockObject != null) {
            apply(answer, currentType.getMethod(methodName, parameters));
        }
        return this;
    }

    public TargetedMockBuilder apply(Answer<?> answer, Method method) throws InvocationTargetException, IllegalAccessException {
        if (currentMockObject != null) {
            method.setAccessible(true);
            method.invoke(Mockito.doAnswer(answer).when(currentMockObject), getParams(method));
        }
        return this;
    }

    public TargetedMockBuilder store() {
        if (currentMockObject != null) {
            finishedMockObjects.add(currentMockObject);
            currentMockObject = null;
            currentType = null;
        }
        return this;
    }

    public Object[] getFinishedMockObjects() {
        Object[] mockObjects = finishedMockObjects.toArray();
        finishedMockObjects.clear();
        return mockObjects;
    }

    /*
        Note Mockito.anyVararg() does NOT work since we have to do the invoke with the correct number of parameters
    */
    private static Object[] getParams(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < params.length; i++) {
            if (parameterTypes[i].equals(byte.class)) {
                params[i] = Mockito.anyByte();
            } else if (parameterTypes[i].equals(int.class)) {
                params[i] = Mockito.anyInt();
            } else if (parameterTypes[i].equals(boolean.class)) {
                params[i] = Mockito.anyBoolean();
            } else if (parameterTypes[i].equals(char.class)) {
                params[i] = Mockito.anyChar();
            } else if (parameterTypes[i].equals(short.class)) {
                params[i] = Mockito.anyShort();
            } else if (parameterTypes[i].equals(double.class)) {
                params[i] = Mockito.anyDouble();
            } else if (parameterTypes[i].equals(float.class)) {
                params[i] = Mockito.anyFloat();
            } else if (parameterTypes[i].equals(long.class)) {
                params[i] = Mockito.anyLong();
            } else if (parameterTypes[i].equals(Collection.class)) {
                params[i] = Mockito.anyCollection();
            } else if (parameterTypes[i].equals(List.class)) {
                params[i] = Mockito.anyList();
            } else if (parameterTypes[i].equals(Set.class)) {
                params[i] = Mockito.anySet();
            } else if (parameterTypes[i].equals(Map.class)) {
                params[i] = Mockito.anyMap();
            } else if (parameterTypes[i].equals(String.class)) {
                params[i] = Mockito.anyString();
            } else {
                params[i] = Mockito.any();
            }
        }
        return params;
    }

}
