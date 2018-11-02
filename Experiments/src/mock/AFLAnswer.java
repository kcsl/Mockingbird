package mock;

import edu.cmu.sv.kelinci.Mem;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
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
        Mem.mem[id^Mem.prev_location]++;
        Mem.prev_location = id >> 1;
        return originalMethod.call();
    }

}
