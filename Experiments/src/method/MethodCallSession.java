package method;

import method.callbacks.MethodCallback;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.objenesis.instantiator.ObjectInstantiator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @author Derrick Lockwood
 * @created 11/7/18.
 */
public class MethodCallSession {

    private MemoryPoolMXBean edenSpace;
    private MemoryPoolMXBean survivorSpace;
    private final ResettableClassFileTransformer transformer;
    private MethodCallback methodCallback;
    private Method methodToCall;
    private final ObjectInstantiator<?> methodClassInstantiator;
    private final ObjectInstantiator<?>[] parameterInstantiators;
    private final ObjectInstantiator<?>[] storedMockInstantiators;
    private Object[] mockParameters;

    MethodCallSession(MethodCallback methodCallback, ResettableClassFileTransformer transformer, Method methodToCall,
            ObjectInstantiator<?> methodClassInstantiator, ObjectInstantiator<?>[] parameterInstantiators,
            ObjectInstantiator<?>[] storedMockInstantiators) throws
            ClassNotFoundException, NoSuchMethodException {
        this.transformer = transformer;
        this.methodCallback = methodCallback;
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
        this.methodClassInstantiator = methodClassInstantiator;
        this.methodToCall = methodToCall;
        this.parameterInstantiators = parameterInstantiators;
        this.storedMockInstantiators = storedMockInstantiators;
        mockParameters = new Object[parameterInstantiators.length];
    }

    public boolean revertClasses(Instrumentation instrumentation) {
        return transformer.reset(instrumentation, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }

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

    private Callable<MethodData> getMethodDataCallable(MethodData methodData, Object mockObject,
            Object[] methodParameters) {
        Callable<Object[]> callable = getCallableRunner(edenSpace, survivorSpace, methodToCall, mockObject,
                methodParameters, false);
        return () -> methodData.objectMapFunction().apply(callable.call());
    }

    private Callable<MethodData> getCreatedCallable() {
        Object mockObject = null;
        Object[] mockParameters = null;
        try {
            mockObject = methodClassInstantiator.newInstance();
            mockParameters = new Object[parameterInstantiators.length];
            for (int i = 0; i < parameterInstantiators.length; i++) {
                mockParameters[i] = parameterInstantiators[i].newInstance();
            }
        } catch (Exception e) {
            MethodData methodData = new MethodData(mockObject, mockParameters, methodToCall.getDeclaringClass(),
                    methodToCall.getName(),
                    methodToCall.getReturnType(), methodToCall.getParameterTypes());
            methodData.setOutput(null, e, null, 0, null);
            return () -> methodData;
        }
        MethodData methodData = new MethodData(mockObject, mockParameters, methodToCall.getDeclaringClass(),
                methodToCall.getName(),
                methodToCall.getReturnType(), methodToCall.getParameterTypes());
        methodCallback.onBefore(methodData);
        return getMethodDataCallable(methodData, mockObject, mockParameters);
    }

    public Future<MethodData> runAsyncMethod(ExecutorService executorService) {
        Object mockObject = null;
        Object[] mockParameters = null;
        try {
            mockObject = methodClassInstantiator.newInstance();
            mockParameters = new Object[parameterInstantiators.length];
            for (int i = 0; i < parameterInstantiators.length; i++) {
                mockParameters[i] = parameterInstantiators[i].newInstance();
            }
        } catch (Exception e) {
            MethodData methodData = new MethodData(mockObject, mockParameters, methodToCall.getDeclaringClass(),
                    methodToCall.getName(),
                    methodToCall.getReturnType(), methodToCall.getParameterTypes());
            methodData.setOutput(null, e, null, 0, null);
            return new Future<>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public MethodData get() throws InterruptedException, ExecutionException {
                    return methodData;
                }

                @Override
                public MethodData get(long timeout, TimeUnit unit) throws
                        InterruptedException,
                        ExecutionException,
                        TimeoutException {
                    return methodData;
                }
            };
        }
        MethodData methodData = new MethodData(mockObject, mockParameters, methodToCall.getDeclaringClass(),
                methodToCall.getName(),
                methodToCall.getReturnType(), methodToCall.getParameterTypes());
        methodCallback.onBefore(methodData);
        return after(executorService.submit(getMethodDataCallable(methodData, mockObject, mockParameters)),
                md -> methodCallback.onAfter(md));
    }

    private <V> Future<V> after(Future<V> future, Consumer<V> consumer) {
        return new Future<V>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public V get() throws InterruptedException, ExecutionException {
                V v = future.get();
                consumer.accept(v);
                return v;
            }

            @Override
            public V get(long timeout, TimeUnit unit) throws
                    InterruptedException,
                    ExecutionException,
                    TimeoutException {
                V v = future.get(timeout, unit);
                consumer.accept(v);
                return v;
            }
        };
    }

    public MethodData runMethod(ExecutorService executorService, long timeOut, boolean sysOutStop) {
        MethodData methodData = new MethodData(null, null, methodToCall.getDeclaringClass(),
                methodToCall.getName(),
                methodToCall.getReturnType(), methodToCall.getParameterTypes());
        Object mockObject;
        try {
            //TODO: Fix Error Handling maybe? The throwables are weird and out of scope in different areas
            mockObject = methodClassInstantiator.newInstance();
            for (int i = 0; i < parameterInstantiators.length; i++) {
                mockParameters[i] = parameterInstantiators[i].newInstance();
            }
        } catch (Throwable e) {
            methodCallback.onBefore(methodData);
            methodData.setOutput(null, e, null, 0, null);
            methodCallback.onAfter(methodData);
            return methodData;
        }
        methodCallback.onBefore(methodData);
        Future<MethodData> future = executorService.submit(
                getMethodDataCallable(methodData, mockObject, mockParameters));
        try {
            if (timeOut > 0) {
                future.get(timeOut, TimeUnit.MILLISECONDS);
            } else {
                future.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            methodData.setError(e, Duration.of(timeOut, ChronoUnit.MILLIS));
        } finally {
            future.cancel(true);
        }
        methodCallback.onAfter(methodData);
        return methodData;
    }

    public MethodData runMethod(ExecutorService executorService, long timeOut) {
        return runMethod(executorService, timeOut, false);
    }

    public MethodData runMethod(ExecutorService executorService) {
        return runMethod(executorService, -1, false);
    }

    public MethodData[] runMultipleTimesMethod(ExecutorService executorService, int n, long timeOut,
            boolean sysOutStop) {
        MethodData[] methodData = new MethodData[n];
        for (int i = 0; i < methodData.length; i++) {
            methodData[i] = runMethod(executorService, timeOut, sysOutStop);
        }
        return methodData;
    }

    public MethodData[] runMultipleTimesMethod(ExecutorService executorService, int n) {
        return runMultipleTimesMethod(executorService, n, -1, false);
    }

    @Override
    public String toString() {
        return methodToCall.toString();
    }

    private static Callable<Object[]> getCallableRunner(MemoryPoolMXBean edenSpace, MemoryPoolMXBean survivorSpace,
            Method toRunMethod, Object mockObject, Object[] mockParameters, boolean overrideSystemOut) {
        return () -> {
            long currentHeapBytes = edenSpace.getUsage().getUsed();
            Object returnValue = null;
            Throwable returnException = null;
            ByteArrayOutputStream byteArrayOutputStream = null;
            PrintStream originalOut = null;
            if (overrideSystemOut) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                originalOut = System.out;
                System.setOut(new PrintStream(byteArrayOutputStream, true, StandardCharsets.UTF_8));
            }
            Instant instant = Instant.now();
            try {
                returnValue = toRunMethod.invoke(mockObject, mockParameters);
            } catch (Throwable e) {
                if (e instanceof InvocationTargetException) {
                    returnException = e.getCause();
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
