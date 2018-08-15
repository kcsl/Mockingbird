package testagents;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 7/23/18.
 */
public class TimingMethodInterceptor {

    private static CSVMethodMap csvMethodMap;

    static {
        try {
            csvMethodMap = new CSVMethodMap("./timings.csv", "Number of times called", "Max Time in one Method Call",
                    "Total time spent in method");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RuntimeType
    public static Object intercept(@AllArguments Object[] args, @Origin Method method,
            @SuperCall Callable<?> callable) {
        long start = System.currentTimeMillis();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            long time = System.currentTimeMillis() - start;
            csvMethodMap.<Long>computeObject(method, 0, d -> (d != null ? d + 1 : 1));
            csvMethodMap.<Long>computeObject(method, 1, d -> (d != null ? Math.max(d, time) : time));
            csvMethodMap.<Long>computeObject(method, 2, d -> (d != null ? d + time : time));
            csvMethodMap.writeMethodMap();
        }
    }
}
