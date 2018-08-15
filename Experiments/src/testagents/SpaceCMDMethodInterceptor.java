package testagents;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author Derrick Lockwood
 * @created 7/23/18.
 */
public class SpaceCMDMethodInterceptor {

    private static CSVMethodMap csvMethodMap;
    private static Function<String, Boolean> filter = s -> s.contains("Stac") && s.contains("/usr/bin/java");

    static {
        try {
            csvMethodMap = new CSVMethodMap("./spaces.csv", "Max Memory", "Total Memory used in each method");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RuntimeType
    public static Object intercept(@AllArguments Object[] args, @Origin Method method,
            @SuperCall Callable<?> callable) {
        csvMethodMap.addPlaceholder(method);
        long kbStart = 0;
        try {
            kbStart = SpaceProcess.getMemoryUsage(filter);
        } catch (IOException ignored) {

        }
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                long deltaHeapMemory = SpaceProcess.getMemoryUsage(filter) - kbStart;
                csvMethodMap.<Long>computeObject(method, 0, d -> (d != null ? Math.max(d,
                        deltaHeapMemory) : deltaHeapMemory));
                csvMethodMap.<Long>computeObject(method, 1,
                        d -> (d != null ? d + deltaHeapMemory : deltaHeapMemory));
                csvMethodMap.writeMethodMap();
            } catch (IOException ignored) {

            }

        }
    }

}
