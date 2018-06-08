package method;

import method.callbacks.AutoMethodCallback;
import method.callbacks.IterationMethodCallback;
import method.callbacks.MethodCallback;
import mock.*;
import mock.answers.SubAnswer;
import mock.answers.auto.AutoIncrementor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 6/6/18.
 */
public class MethodCall {

    private final TargetedMockBuilder builder;
    private final MockCreator[] parameters;
    private final List<MockCreator> instanceVariablesUsed;
    private final SubMockClass methodMockClass;
    private final Method method;
    private MethodCallback methodCallback;

    private MethodCall(Method method, MethodCallback methodCallback) {
        this.method = method;
        instanceVariablesUsed = new ArrayList<>();
        builder = new TargetedMockBuilder();
        methodMockClass = builder.createSubclassRealMethods(method.getDeclaringClass());
        parameters = new MockCreator[method.getParameterCount()];
        this.methodCallback = methodCallback;
    }

    public void linkMethodCallback(MethodCallback methodCallback) {
        this.methodCallback = this.methodCallback.link(methodCallback);
    }

    public MethodCallback getMethodCallback() {
        return methodCallback;
    }

    public SubMockClass createParameterMock(int index, SubAnswer defaultAnswer) {
        if (index < 0 || index >= parameters.length) {
            return null;
        }

        MockCreator parameterMock = createMockCreator(method.getParameterTypes()[index], defaultAnswer);
        parameters[index] = parameterMock;
        return parameterMock instanceof SubMockClass ? (SubMockClass) parameterMock : null;
    }
    public SubMockClass createParameterMock(int index) {
        return createParameterMock(index, null);
    }

    public void fillParameters(SubAnswer defaultAnswer) {
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = createMockCreator(types[i], defaultAnswer);
        }
    }

    public void autoFillParameters() {
        AutoMethodCallback autoMethodCallback = AutoMethodCallback.create();
        methodCallback = methodCallback.link(autoMethodCallback);
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            AutoIncrementor autoIncrementor = AutoIncrementor.createIncrementor();
            autoMethodCallback.add(autoIncrementor);
            parameters[i] = createMockCreator(types[i], autoIncrementor);
        }
    }

    private MockCreator createMockCreator(Class<?> type, SubAnswer answer) {
        MockCreator mockCreator = PrimitiveMockCreator.create(builder, type, answer);
        if (mockCreator == null) {
            return builder.createSubclass(type, answer);
        }
        return mockCreator;
    }

    public SubMockClass createInstanceMock(String instanceVariableName, SubAnswer defaultAnswer) throws NoSuchFieldException {
        Class<?> instanceVariableClass = method.getDeclaringClass().getDeclaredField(instanceVariableName).getType();
        MockCreator instanceMock = createMockCreator(instanceVariableClass, defaultAnswer);
        methodMockClass.applyField(instanceVariableClass, instanceVariableName);
        instanceVariablesUsed.add(instanceMock);
        return instanceMock instanceof SubMockClass ? (SubMockClass) instanceMock : null;
    }

    public SubMockClass createInstanceMock(String instanceVariableName) throws NoSuchFieldException {
        return createInstanceMock(instanceVariableName, null);
    }

    public static MethodCall createMethodCall(Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(1, type, methodName, parameterTypes);
    }

    public static MethodCall createMethodCall(int times, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(IterationMethodCallback.create(times), type, methodName, parameterTypes);
    }

    public static MethodCall createMethodCall(MethodCallback methodCallback, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return new MethodCall(type.getDeclaredMethod(methodName, parameterTypes), methodCallback);
    }

    public void runRecreate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        while (methodCallback.continueIteration()) {
            for (MockCreator mockCreator : instanceVariablesUsed) {
                mockCreator.store();
            }
            Object mockObject = methodMockClass.create();
            Object[] objects = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                objects[i] = parameters[i].create();
            }
            runMethod(mockObject, objects);
        }
        methodCallback.onEndIteration();
    }

    public void run() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (MockCreator mockCreator : instanceVariablesUsed) {
            mockCreator.store();
        }
        Object mockObject = methodMockClass.create();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            objects[i] = parameters[i].create();
        }
        while (methodCallback.continueIteration()) {
            runMethod(mockObject, objects);
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isPrimitive()) {
                    objects[i] = parameters[i].create();
                }
            }
        }
    }

    private void runMethod(Object mockObject, Object[] objects) {
        MethodData methodData = new MethodData(mockObject, objects, method.getDeclaringClass(), method.getName(), method.getReturnType(), method.getParameterTypes());
        methodCallback.onBefore(methodData);
        Object returnValue = null;
        Exception returnException = null;
        //TODO: Figure out whether to track memory and time at the same time or one or the other
        long currentHeapBytes = Runtime.getRuntime().totalMemory();
        Instant instant = Instant.now();
        try {
            returnValue = method.invoke(mockObject, objects);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
        } catch (Exception e) {
            returnException = e;
        }
        Duration duration = Duration.between(instant, Instant.now());
        methodData.setOutput(returnValue, returnException, duration,  Runtime.getRuntime().totalMemory() - currentHeapBytes);
        methodCallback.onAfter(methodData);
    }

}
