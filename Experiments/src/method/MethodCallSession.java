package method;

import method.callbacks.MethodCallback;
import mock.MockCreator;
import util.AdvancedFuture;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

/**
 * @author Derrick Lockwood
 * @created 8/14/18.
 */
public class MethodCallSession {

    private MemoryPoolMXBean edenSpace;
    private MemoryPoolMXBean survivorSpace;
    private final MethodCall methodCall;
    private MethodCallback methodCallback;
    private Object mockObject;
    private Object[] mockParameters;
    private boolean isLoaded;
    private boolean resetEveryTime;

    MethodCallSession(MethodCall methodCall, boolean resetEveryTime) {
        this.methodCall = methodCall;
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

    MethodCallSession(MethodCall methodCall) {
        this(methodCall, false);
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

    private void loadMethodRequirements() {
        if (!isLoaded || resetEveryTime) {
            mockObject = methodCall.methodMockClass.newInstance();
            mockParameters = new Object[methodCall.parameters.length];
            for (MockCreator mockCreator : methodCall.normalObjects) {
                mockCreator.newInstance();
            }
            for (int i = 0; i < methodCall.parameters.length; i++) {
                mockParameters[i] = methodCall.parameters[i].newInstance();
            }
            isLoaded = true;
        } else {
            for (int i = 0; i < methodCall.parameters.length; i++) {
                if (methodCall.parameters[i].isPrimitive()) {
                    mockParameters[i] = methodCall.parameters[i].newInstance();
                }
            }
            //Reloads the primitive instance variables defined createObject the mockObject
            methodCall.methodMockClass.reloadInstanceVariables(mockObject, methodCall.primitiveInstanceVariables);
        }
    }

    public MethodData runMethod(ExecutorService executorService, long timeOut) {
        try {
            loadMethodRequirements();
        } catch (Exception e) {
            MethodData methodData = new MethodData(mockObject, mockParameters, methodCall.method.getDeclaringClass(),
                    methodCall.method.getName(),
                    methodCall.method.getReturnType(), methodCall.method.getParameterTypes());
            methodData.setOutput(null, e, null, 0, null);
            return methodData;
        }
        MethodData methodData = new MethodData(mockObject, mockParameters, methodCall.method.getDeclaringClass(),
                methodCall.method.getName(),
                methodCall.method.getReturnType(), methodCall.method.getParameterTypes());
        methodCallback.onBefore(methodData);
        Future<Object[]> future = executorService.submit(getCallable(mockObject, mockParameters, false));
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
        } finally {
            future.cancel(true);
        }
        methodData.setOutput(values[0], (Exception) values[1], (Duration) values[2], (long) values[3],
                (String) values[4]);
        methodCallback.onAfter(methodData);
        return methodData;
    }

    public AdvancedFuture<MethodData> runAsyncMethod(ExecutorService executorService) {
        Object mockObject = null;
        Object[] mockParameters = null;
        try {
            mockObject = methodCall.methodMockClass.newInstance();
            mockParameters = new Object[methodCall.parameters.length];
            for (MockCreator mockCreator : methodCall.normalObjects) {
                mockCreator.newInstance();
            }
            for (int i = 0; i < methodCall.parameters.length; i++) {
                mockParameters[i] = methodCall.parameters[i].newInstance();
            }
        } catch (Exception e) {
            MethodData methodData = new MethodData(mockObject, mockParameters, methodCall.method.getDeclaringClass(),
                    methodCall.method.getName(),
                    methodCall.method.getReturnType(), methodCall.method.getParameterTypes());
            methodData.setOutput(null, e, null, 0, null);
            return AdvancedFuture.completedFuture(methodData);
        }
        MethodData methodData = new MethodData(mockObject, mockParameters, methodCall.method.getDeclaringClass(),
                methodCall.method.getName(),
                methodCall.method.getReturnType(), methodCall.method.getParameterTypes());
        methodCallback.onBefore(methodData);

        return AdvancedFuture.submit(executorService, getCallable(mockObject, mockParameters, false))
                .map(methodData.objectMapFunction())
                .after(md -> methodCallback.onAfter(md));
    }


    private Callable<Object[]> getCallable(Object mockObject, Object[] objects, boolean overrideSystemOut) {
        return () -> {
            long currentHeapBytes = edenSpace.getUsage().getUsed();
            Object returnValue = null;
            Exception returnException = null;
            ByteArrayOutputStream byteArrayOutputStream = null;
            PrintStream originalOut = null;
            if (overrideSystemOut) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                originalOut = System.out;
                System.setOut(new PrintStream(byteArrayOutputStream, true, "UTF-8"));
            }
            Instant instant = Instant.now();
            try {
                returnValue = methodCall.method.invoke(mockObject, objects);
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
            String sysOut = null;
            if (overrideSystemOut) {
                System.setOut(originalOut);
                sysOut = new String(byteArrayOutputStream.toByteArray());
            }
            return new Object[]{
                    returnValue,
                    returnException,
                    duration,
                    deltaHeapMemory,
                    sysOut
            };
        };
    }


}
