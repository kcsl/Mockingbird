package method;

import method.callbacks.EmptyMethodCallback;
import method.callbacks.MethodCallback;
import mock.MockCreator;
import mock.PrimitiveMockCreator;
import mock.SubMockClass;
import mock.TargetedMockBuilder;
import mock.answers.Answer;
import mock.answers.NotStubbedAnswer;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

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
    private final List<MockCreator> normalObjects;
    private final SubMockClass methodMockClass;
    private final Method method;
    private final AttributeClass[] parameterDefinitions;
    private MemoryPoolMXBean edenSpace;
    private MemoryPoolMXBean survivorSpace;
    private MethodCallback methodCallback;
    private Object mockObject;
    private Object[] mockParameters;
    private boolean isLoaded;

    private MethodCall(AttributeClass[] parameterDefinitions, Method method, MethodCallback methodCallback) {
        this.method = method;
        this.parameterDefinitions = parameterDefinitions;
        builder = new TargetedMockBuilder();
        methodMockClass = builder.createSubclassRealMethods(method.getDeclaringClass());
        parameters = new MockCreator[method.getParameterCount()];
        primitiveInstanceVariables = new ArrayList<>();
        this.methodCallback = methodCallback;
        normalObjects = new ArrayList<>();
        isLoaded = false;
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
     * Creates a mocked method call that is determined by {@code methodName} and {@code parameterTypes} which is then used to
     * supply functionality to the mocked method. The method callback is used to control the flow of the method to be run.
     * <p>
     * Note: Everything is createObject the form of a callback and thus should only have to create objects once if not a primitive unless
     * otherwise specified
     *
     * @param methodCallback callback to send back the method data and control how many times the method is run
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found createObject {@code type}
     */
    public static MethodCall createMethodCall(
            MethodCallback methodCallback,
            Class<?> type,
            String methodName,
            AttributeClass... parameterTypes) throws NoSuchMethodException {
        Class<?>[] classes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            classes[i] = parameterTypes[i].getRealClass();
        }
        return new MethodCall(parameterTypes, type.getDeclaredMethod(methodName, classes), methodCallback);
    }

    public static MethodCall createMethodCall(Class<?> type,
            String methodName,
            AttributeClass... parameterTypes) throws NoSuchMethodException {
        return createMethodCall(EmptyMethodCallback.create(), type, methodName, parameterTypes);
    }

    /**
     * Creates a mocked method call that is determined by {@code methodName} and {@code parameterTypes} which is then used to
     * supply functionality to the mocked method. The method callback is used to control the flow of the method to be run.
     * <p>
     * Note: Everything is createObject the form of a callback and thus should only have to create objects once if not a primitive unless
     * otherwise specified
     *
     * @param methodCallback callback to send back the method data and control how many times the method is run
     * @param type           the class with the method to mock
     * @param methodName     the method to mock name
     * @param parameterTypes the parameter types
     * @return Method call object to add parameters and instance variables and run the method
     * @throws NoSuchMethodException if no such method is found createObject {@code type}
     */
    public static MethodCall createMethodCall(
            MethodCallback methodCallback,
            Class<?> type,
            String methodName,
            Class<?>... parameterTypes) throws NoSuchMethodException {
        AttributeClass[] attributeClasses = new AttributeClass[parameterTypes.length];
        for (int i = 0; i < attributeClasses.length; i++) {
            attributeClasses[i] = new AttributeClass(parameterTypes[i]);
        }
        return new MethodCall(attributeClasses, type.getDeclaredMethod(methodName, parameterTypes), methodCallback);
    }


    public static MethodCall createMethodCall(
            MethodCallback methodCallback,
            Class<?> type,
            String methodName,
            String... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        AttributeClass[] attributeClasses = parseParameterTypes(parameterTypes);
        Class<?>[] classes = new Class[attributeClasses.length];
        for (int i = 0; i < attributeClasses.length; i++) {
            classes[i] = attributeClasses[i].getRealClass();
        }
        return new MethodCall(attributeClasses, type.getDeclaredMethod(methodName, classes), methodCallback);
    }

    public static MethodCall createMethodCall(Class<?> type,
            String methodName,
            String... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return createMethodCall(EmptyMethodCallback.create(), type, methodName, parameterTypes);
    }

    private static AttributeClass[] parseParameterTypes(String... parameterTypes) throws ClassNotFoundException {
        AttributeClass[] arr = new AttributeClass[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            arr[i] = AttributeClass.createAttributeClass(parameterTypes[i]);
        }
        return arr;
    }

    SubMockClass getMethodMockClass() {
        return methodMockClass;
    }

    <T> ObjectInstantiator<T> getObjectInstantiatorByName(String name) {
        return builder.getInstantiator(name);
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
     * @param index         the index of the parameter of the method
     * @param defaultAnswer the default answer by which any method that is called createObject {@code parameters[index].type} is run.
     *                      This is also the answer that is supplied to the primitive variables when {@code .store()}
     *                      is run on them which just returns the value of the primitive variable.
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     */
    public SubMockClass createParameterMock(int index, Answer defaultAnswer) {
        if (index < 0 || index >= parameters.length) {
            //TODO: Soft ignore?
            return null;
        }
        MockCreator parameterMock = createMockCreator(parameterDefinitions[index], defaultAnswer);
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
        return createParameterMock(index, NotStubbedAnswer.newInstance());
    }

    public AttributeClass getParameterAttributeClass(int index) {
        if (index < 0 || index >= parameters.length) {
            //TODO: Soft ignore?
            return null;
        }
        return parameterDefinitions[index];
    }

    private MockCreator createMockCreator(AttributeClass attributeClass, Answer answer) {
        MockCreator mockCreator;
        if (attributeClass.getMockClass().isPrimitive() || attributeClass.getMockClass().isAssignableFrom(
                String.class)) {
            if (attributeClass.getAttribute(AttributeClass.IS_ARRAY, false) || attributeClass.getAttribute(
                    AttributeClass.IS_LIST, false)) {
                mockCreator = PrimitiveMockCreator.create(builder, attributeClass, answer);
            } else {
                mockCreator = PrimitiveMockCreator.create(builder, attributeClass.getMockClass(), answer);
            }
        } else if (attributeClass.getAttribute(AttributeClass.IS_ARRAY, false) || attributeClass.getAttribute(
                AttributeClass.IS_LIST, false)) {
            mockCreator = builder.createMultipleMockClass(attributeClass);
        } else {
            mockCreator = builder.createSubclass(attributeClass.getMockClass());
        }
        return mockCreator;
    }

    /**
     * Creates an instance variable mock similar to {@link method.MethodCall#createParameterMock(int, Answer) createParameterMock}
     * but creates it for the instance variable used createObject the method to mock and supplied to the declaring object.
     *
     * @param instanceVariableName instance variable name to mock
     * @param defaultAnswer        the default answer by which any method that is called createObject {@code parameters[index].type} is run.
     *                             This is also the answer that is supplied to the primitive variables when {@code .store()}
     *                             is run on them which just returns the value of the primitive variable.
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     * @throws NoSuchFieldException if the field is not found
     */
    public SubMockClass createInstanceMock(String instanceVariableName, Answer defaultAnswer) throws
            NoSuchFieldException, ClassNotFoundException {
        Class<?> instanceVariableClass = method.getDeclaringClass().getDeclaredField(instanceVariableName).getType();
        MockCreator instanceMock = createMockCreator(
                AttributeClass.createAttributeClass(instanceVariableClass.getName()), defaultAnswer);
        if (instanceMock.isPrimitive()) {
            primitiveInstanceVariables.add(instanceVariableName);
        }
        methodMockClass.applyField(instanceVariableName, instanceMock);
        return instanceMock instanceof SubMockClass ? (SubMockClass) instanceMock : null;
    }

    /**
     * Creates an instance variable mock but the default answer is not stubbed.
     *
     * @param instanceVariableName instance variable name to mock
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     * @throws NoSuchFieldException if the field is not found
     */
    public SubMockClass createInstanceMock(String instanceVariableName) throws
            NoSuchFieldException,
            ClassNotFoundException {
        return createInstanceMock(instanceVariableName, null);
    }

    public MockCreator createStoredMock(AttributeClass attributeClass) {
        return createStoredMock(attributeClass, null);
    }

    public MockCreator createStoredMock(AttributeClass attributeClass, Answer defaultAnswer) {
        MockCreator multipleMockCreator = createMockCreator(attributeClass, defaultAnswer);
        normalObjects.add(multipleMockCreator);
        return multipleMockCreator;
    }

    private void loadMethodRequirements(boolean resetEveryTime) {
        if (!isLoaded || resetEveryTime) {
            mockObject = methodMockClass.newInstance();
            mockParameters = new Object[parameters.length];
            for (MockCreator mockCreator : normalObjects) {
                mockCreator.newInstance();
            }
            for (int i = 0; i < parameters.length; i++) {
                mockParameters[i] = parameters[i].newInstance();
            }
            isLoaded = true;
        } else {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isPrimitive()) {
                    mockParameters[i] = parameters[i].newInstance();
                }
            }
            //Reloads the primitive instance variables defined createObject the mockObject
            methodMockClass.reloadInstanceVariables(mockObject, primitiveInstanceVariables);
        }
    }

    public MethodData runMethod(ExecutorService executorService, long timeOut, boolean resetEveryTime) {
        try {
            loadMethodRequirements(resetEveryTime);
        } catch (Exception e) {
            MethodData methodData = new MethodData(mockObject, mockParameters, method.getDeclaringClass(),
                    method.getName(),
                    method.getReturnType(), method.getParameterTypes());
            methodData.setOutput(null, e, null, 0);
            return methodData;
        }
        MethodData methodData = new MethodData(mockObject, mockParameters, method.getDeclaringClass(), method.getName(),
                method.getReturnType(), method.getParameterTypes());
        methodCallback.onBefore(methodData);
        Future<Object[]> future = executorService.submit(getCallable(mockObject, mockParameters));
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
            future.cancel(true);
            values[1] = e;
            values[2] = Duration.of(timeOut, ChronoUnit.MILLIS);
        }
        methodData.setOutput(values[0], (Exception) values[1], (Duration) values[2], (long) values[3]);
        methodCallback.onAfter(methodData);
        return methodData;
    }


    private Callable<Object[]> getCallable(Object mockObject, Object[] objects) {
        return () -> {
            long currentHeapBytes = edenSpace.getUsage().getUsed();
            Object returnValue = null;
            Exception returnException = null;
            Instant instant = Instant.now();
            try {
                returnValue = method.invoke(mockObject, objects);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    returnException = (Exception) e.getCause();
                } else {
                    returnException = e;
                }
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
        };
    }

    @Override
    public String toString() {
        return method.getDeclaringClass().getName() + " : " + method.getName() + " | " + Arrays.toString(
                method.getParameterTypes());
    }

}
