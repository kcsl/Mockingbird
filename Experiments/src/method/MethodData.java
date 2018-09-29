package method;

import method.callbacks.MethodCallback;

import java.time.Duration;
import java.util.function.Function;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class MethodData {
    private final Class<?> declaringClass;
    private final String methodName;
    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;
    private final Object mockObject;
    private final Object[] parameters;

    private transient Object returnValue;
    private transient Throwable returnException;
    private transient Duration duration;
    private transient long deltaHeapMemory;
    private transient String systemOut;

    MethodData(
            Object mockObject,
            Object[] parameters,
            Class<?> declaringClass,
            String methodName,
            Class<?> returnType,
            Class<?>... parameterTypes) {
        this.mockObject = mockObject;
        this.parameters = parameters;
        this.methodName = methodName;
        this.declaringClass = declaringClass;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    void setOutput(Object returnValue, Throwable returnException, Duration duration, long deltaHeapMemory, String systemOut) {
        this.returnValue = returnValue;
        this.returnException = returnException;
        this.duration = duration;
        this.deltaHeapMemory = deltaHeapMemory;
        this.systemOut = systemOut;
    }

    public Object getMockObject() {
        return mockObject;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public Throwable getReturnException() {
        return returnException;
    }

    public Duration getDuration() {
        return duration;
    }

    public long getDeltaHeapMemory() {
        return deltaHeapMemory;
    }

    Function<Object[], MethodData> objectMapFunction() {
        return values -> {
            this.setOutput(values[0], (Exception) values[1], (Duration) values[2], (long) values[3],
                    (String) values[4]);
            return this;
        };
    }

    @Override
    public String toString() {
        return "Duration: " + (duration == null ? "none" : duration) + " Memory: " + deltaHeapMemory;
    }
}
