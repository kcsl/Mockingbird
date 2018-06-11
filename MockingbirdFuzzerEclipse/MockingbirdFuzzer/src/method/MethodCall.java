package method;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import method.callbacks.AutoMethodCallback;
import method.callbacks.IterationMethodCallback;
import method.callbacks.MethodCallback;
import mock.MockCreator;
import mock.PrimitiveMockCreator;
import mock.SubMockClass;
import mock.TargetedMockBuilder;
import mock.answers.Answer;
import mock.answers.SubAnswer;
import mock.answers.auto.AutoIncrementor;

/**
 * Creates an environment to call the method specified and captures an analysis on the method
 * called.
 *
 * @author Derrick Lockwood
 * @created 6/6/18.
 */
public class MethodCall {

    private final TargetedMockBuilder builder;
    private final MockCreator[] parameters;
    private final List<String> primitiveInstanceVariables;
    private final SubMockClass methodMockClass;
    private final Method method;
    private final long timeOut;
    private final ExecutorService executorService;
    private MemoryPoolMXBean edenSpace;
    private MemoryPoolMXBean survivorSpace;
    private MethodCallback methodCallback;

    private MethodCall(Method method, MethodCallback methodCallback, long timeOut) {
        this.method = method;
        builder = new TargetedMockBuilder();
        methodMockClass = builder.createSubclassRealMethods(method.getDeclaringClass());
        parameters = new MockCreator[method.getParameterCount()];
        primitiveInstanceVariables = new ArrayList<>();
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

    /**
     * Links the {@code methodCallback} to the current {@code methodCallback}
     *
     * @param methodCallback
     */
    public void linkMethodCallback(MethodCallback methodCallback) {
        this.methodCallback = this.methodCallback.link(methodCallback);
    }

    /**
     * Gets the current {@code methodCallback}
     *
     * @return the current {@code methodCallback}
     */
    public MethodCallback getMethodCallback() {
        return methodCallback;
    }

    /**
     * Creates a parameter SubMockClass by which the parameter is instantiated
     *
     * @param index the index of the parameter of the method
     * @param defaultAnswer the default answer by which any method that is called in {@code parameters[index].type} is run.
     *                      This is also the answer that is supplied to the primitive variables when {@code .store()}
     *                      is run on them which just returns the value of the primitive variable.
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     */
    public SubMockClass createParameterMock(int index, SubAnswer defaultAnswer) {
        if (index < 0 || index >= parameters.length) {
            return null;
        }

        MockCreator parameterMock = createMockCreator(method.getParameterTypes()[index], defaultAnswer);
        parameters[index] = parameterMock;
        return parameterMock instanceof SubMockClass ? (SubMockClass) parameterMock : null;
    }
    /**
     * Creates a parameter SubMockClass by which the parameter is instantiated but the default answer is null and thus
     * set to an error if method is not mocked when run.
     *
     * @param index the index of the parameter of the method
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     */
    public SubMockClass createParameterMock(int index) {
        return createParameterMock(index, null);
    }

    /**
     * Fills in all parameters with the default answer
     * @param defaultAnswer the default answer by which any method that is called in {@code parameters[index].type} is run.
     *                      This is also the answer that is supplied to the primitive variables when {@code .store()}
     *                      is run on them which just returns the value of the primitive variable.
     */
    public void fillParameters(SubAnswer defaultAnswer) {
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = createMockCreator(types[i], defaultAnswer);
        }
    }

    /**
     * Tries to fill in all method parameters with auto classes that can act like fuzzing without using a fuzzer
     */
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

    private MockCreator createMockCreator(Class<?> type, Answer answer) {
        MockCreator mockCreator = PrimitiveMockCreator.create(builder, type, answer);
        if (mockCreator == null) {
            return builder.createSubclass(type, (SubAnswer) answer);
        }
        return mockCreator;
    }

    /**
     * Creates an instance variable mock similar to {@link method.MethodCall#createParameterMock(int, SubAnswer) createParameterMock}
     * but creates it for the instance variable used in the method to mock and supplied to the declaring object.
     * @param instanceVariableName instance variable name to mock
     * @param defaultAnswer the default answer by which any method that is called in {@code parameters[index].type} is run.
     *                      This is also the answer that is supplied to the primitive variables when {@code .store()}
     *                      is run on them which just returns the value of the primitive variable.
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     * @throws NoSuchFieldException if the field is not found
     */
    public SubMockClass createInstanceMock(String instanceVariableName, SubAnswer defaultAnswer) throws NoSuchFieldException {
        Class<?> instanceVariableClass = method.getDeclaringClass().getDeclaredField(instanceVariableName).getType();
        MockCreator instanceMock = createMockCreator(instanceVariableClass, defaultAnswer);
        if (instanceMock.isPrimitive()) {
            primitiveInstanceVariables.add(instanceVariableName);
        }
        methodMockClass.applyField(instanceVariableName, instanceMock);
        return instanceMock instanceof SubMockClass ? (SubMockClass) instanceMock : null;
    }

    /**
     * Creates an instance variable mock but the default answer is not stubbed.
     * @param instanceVariableName instance variable name to mock
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     * @throws NoSuchFieldException if the field is not found
     */
    public SubMockClass createInstanceMock(String instanceVariableName) throws NoSuchFieldException {
        return createInstanceMock(instanceVariableName, null);
    }

    /**
     * Runs the method to mock and recreates the declared object and its parameters every time the method is run
     */
    public void runRecreate() {
        while (methodCallback.continueIteration()) {
            Object mockObject = methodMockClass.newInstance();
            Object[] objects = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                objects[i] = parameters[i].newInstance();
            }
            runMethod(mockObject, objects);
        }
        methodCallback.onEndIteration();
        executorService.shutdown();
    }

    /**
     * Runs the method to mock but only creates the declared object ONCE and the parameters ONCE unless it is a primitive
     * variable
     */
    public void run() {
        Object mockObject = methodMockClass.newInstance();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            objects[i] = parameters[i].newInstance();
        }
        while (methodCallback.continueIteration()) {
            runMethod(mockObject, objects);
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isPrimitive()) {
                    objects[i] = parameters[i].newInstance();
                }
            }
            //Reloads the primitive instance variables defined in the mockObject
            methodMockClass.reloadInstanceVariables(mockObject, primitiveInstanceVariables);
        }
        methodCallback.onEndIteration();
        executorService.shutdown();
    }

    private void runMethod(Object mockObject, Object[] objects) {
        MethodData methodData = new MethodData(mockObject, objects, method.getDeclaringClass(), method.getName(), method.getReturnType(), method.getParameterTypes());
        methodCallback.onBefore(methodData);
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
        Object[] values = new Object[]{
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


    /**
     * See {@link method.MethodCall#createMethodCall(int, Class, String, Class[]) here}.
     * <p>
     * Sets the method to mock to only run once
     *
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found in {@code type}
     */
    public static MethodCall createMethodCall(Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(1, type, methodName, parameterTypes);
    }

    /**
     * See {@link method.MethodCall#createMethodCall(MethodCallback, Class, String, Class[]) here}.
     * <p>
     * Sets the {@code methodCallback} to be an iterative method callback based on the number of {@code times}
     *
     * @param times          number of times to run the method to mock
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found in {@code type}
     */
    public static MethodCall createMethodCall(int times, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(IterationMethodCallback.create(times), type, methodName, parameterTypes);
    }

    /**
     * See {@link method.MethodCall#createMethodCall(MethodCallback, long, Class, String, Class[]) here}.
     * <p>
     * Sets the timeout to be infinite
     *
     * @param methodCallback callback to send back the method data and control how many times the method is run
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found in {@code type}
     */
    public static MethodCall createMethodCall(MethodCallback methodCallback, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(methodCallback, -1, type, methodName, parameterTypes);
    }

    /**
     * Creates a mocked method call that is determined by {@code methodName} and {@code parameterTypes} which is then used to
     * supply functionality to the mocked method. The method callback is used to control the flow of the method to be run.
     * <p>
     * Note: Everything is in the form of a callback and thus should only have to create objects once if not a primitive unless
     * otherwise specified
     *
     * @param methodCallback callback to send back the method data and control how many times the method is run
     * @param timeOut        timeout of the method call
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found in {@code type}
     */
    public static MethodCall createMethodCall(MethodCallback methodCallback, long timeOut, Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return new MethodCall(type.getDeclaredMethod(methodName, parameterTypes), methodCallback, timeOut);
    }

}
