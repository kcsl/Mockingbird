package method;

import method.callbacks.AutoMethodCallback;
import method.callbacks.IterationMethodCallback;
import method.callbacks.MethodCallback;
import mock.MockCreator;
import mock.PrimitiveMockCreator;
import mock.SubMockClass;
import mock.TargetedMockBuilder;
import mock.answers.SubAnswer;
import mock.answers.auto.AutoIncrementor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    private final long timeOut;
    private final ExecutorService executorService;
    private MemoryPoolMXBean edenSpace;
    private MemoryPoolMXBean survivorSpace;
    private MethodCallback methodCallback;

    private MethodCall(Method method, MethodCallback methodCallback, long timeOut) {
        this.method = method;
        instanceVariablesUsed = new ArrayList<>();
        builder = new TargetedMockBuilder();
        methodMockClass = builder.createSubclassRealMethods(method.getDeclaringClass());
        parameters = new MockCreator[method.getParameterCount()];
        this.methodCallback = methodCallback;
        this.timeOut = timeOut;
        executorService = Executors.newCachedThreadPool();
        for (MemoryPoolMXBean bean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (bean.getType() == MemoryType.HEAP) {
                if (bean.getName().contains("Eden")) {
                    edenSpace = bean;
                } else if (bean.getName().contains("Survivor")) {
                    survivorSpace = bean;
                }
                if (edenSpace != null && survivorSpace != null) {
                    break;
                }
            }
        }
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
        executorService.shutdown();
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
        methodCallback.onEndIteration();
        executorService.shutdown();
    }

    private void runMethod(Object mockObject, Object[] objects) {
        MethodData methodData = new MethodData(mockObject, objects, method.getDeclaringClass(), method.getName(), method.getReturnType(), method.getParameterTypes());
        methodCallback.onBefore(methodData);
        //TODO: Figure out whether to track memory and time at the same time or one or the other
        Future<Object[]> future = executorService.submit(() -> {
            long currentHeapBytes = edenSpace.getUsage().getUsed();
            Object returnValue = null;
            Exception returnException = null;
            Instant instant = Instant.now();
            try {
                returnValue = method.invoke(mockObject, objects);
            } catch (Exception e) {
                returnException = e;
            }
            Duration duration = Duration.between(instant, Instant.now());
            long survivorSpaceMemory = survivorSpace.getUsage().getUsed();
            long edenSpaceUsage = edenSpace.getUsage().getUsed();
            long edenSpaceMax = edenSpace.getUsage().getCommitted();
            long deltaHeapMemory = edenSpaceUsage - currentHeapBytes;
            if (deltaHeapMemory < 0) {
                deltaHeapMemory = edenSpaceMax - currentHeapBytes + survivorSpaceMemory;
            }
            return new Object[]{
                    returnValue,
                    returnException,
                    duration,
                    deltaHeapMemory
            };
        });
        Object[] values = new Object[] {
                null,
                null,
                null,
                0L
        };
        try {
            if (timeOut > 0) {
                values = future.get(timeOut, TimeUnit.MILLISECONDS);
            } else {
                values = future.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            values[1] = e;
            values[2] = Duration.of(timeOut, ChronoUnit.MILLIS);
        }
        methodData.setOutput(values[0], (Exception) values[1], (Duration) values[2], (long) values[3]);
        methodCallback.onAfter(methodData);
    }

    public static MethodCall createMethodCall(Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(1, type, methodName, parameterTypes);
    }

    public static MethodCall createMethodCall(int times, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(IterationMethodCallback.create(times), type, methodName, parameterTypes);
    }

    public static MethodCall createMethodCall(MethodCallback methodCallback, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(methodCallback, -1, type, methodName, parameterTypes);
    }

    /**
     * Creates a mocked method call that is determined by {@code methodName} and {@code parameterTypes} which is then used to
     * supply functionality to the mocked method. The method callback is used to control the flow of the method to be run.
     *
     * Note: Everything is in the form of a callback and thus should only have to create objects once if not a primitive unless
     * otherwise specified
     * @param methodCallback
     * @param timeOut
     * @param type
     * @param methodName
     * @param parameterTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static MethodCall createMethodCall(MethodCallback methodCallback, long timeOut, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return new MethodCall(type.getDeclaredMethod(methodName, parameterTypes), methodCallback, timeOut);
    }

}
