package testagents;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 7/23/18.
 */
public class SpaceMethodInterceptor {

    private static CSVMethodMap csvMethodMap;

    private static MemoryPoolMXBean edenSpace;
    private static MemoryPoolMXBean survivorSpace;

    static {
        try {
            csvMethodMap = new CSVMethodMap("./spaces.csv", "Max Memory", "Total Memory used in each method");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @RuntimeType
    public static Object intercept(@AllArguments Object[] args, @Origin Method method,
            @SuperCall Callable<?> callable) {
        long currentHeapBytes = edenSpace.getUsage().getUsed();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            long survivorSpaceMemory = survivorSpace.getUsage().getUsed();
            long edenSpaceUsage = edenSpace.getUsage().getUsed();
            long edenSpaceMax = edenSpace.getUsage().getCommitted();
            long deltaHeapMemory = edenSpaceUsage - currentHeapBytes;
            if (deltaHeapMemory < 0) {
                deltaHeapMemory = edenSpaceMax - currentHeapBytes + survivorSpaceMemory;
            }
            long finalDeltaHeapMemory = deltaHeapMemory;
            csvMethodMap.<Long>computeObject(method, 0, d -> (d != null ? Math.max(d,
                    finalDeltaHeapMemory) : finalDeltaHeapMemory));
            csvMethodMap.<Long>computeObject(method, 1,
                    d -> (d != null ? d + finalDeltaHeapMemory : finalDeltaHeapMemory));
            csvMethodMap.writeMethodMap();
        }
    }
}
