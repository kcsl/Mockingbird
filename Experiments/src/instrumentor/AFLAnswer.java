package instrumentor;

import edu.cmu.sv.kelinci.Mem;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 11/1/18.
 */
public class AFLAnswer {

    public static ElementMatcher<? super MethodDescription> MATCHER = ElementMatchers.named("handle").and(
            ElementMatchers.takesArguments(Callable.class));

    private Random r;
    private HashSet<Integer> ids;


    public AFLAnswer() {
        ids = new HashSet<>();
        r = new Random();
    }

    /**
     * Best effort to generate a random id that is not already in use.
     */
    private int getNewLocationId() {
        int id;
        int tries = 0;
        do {
            id = r.nextInt(Mem.SIZE);
            tries++;
        } while (tries <= 10 && ids.contains(id));
        ids.add(id);
        return id;
    }

    @RuntimeType
    public Object handle(@SuperCall Callable<Object> originalMethod) throws Throwable {
        int id = getNewLocationId();
        AFLPathMem.mem[id^Mem.prev_location]++;
        AFLPathMem.prev_location = id >> 1;
        return originalMethod.call();
    }

    public static DynamicType.Builder<?> applyAFLTransformation(DynamicType.Builder<?> builder, ElementMatcher<? super MethodDescription> descriptions) {
        return builder.method(descriptions)
                .intercept(MethodDelegation.withDefaultConfiguration().filter(AFLAnswer.MATCHER).to(new AFLAnswer()));
    }
}
