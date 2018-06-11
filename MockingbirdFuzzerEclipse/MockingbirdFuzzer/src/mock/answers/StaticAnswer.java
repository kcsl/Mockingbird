package mock.answers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public interface StaticAnswer extends Answer {
    ElementMatcher<? super MethodDescription> MATCHER = ElementMatchers.named("handle").and(ElementMatchers.takesArguments(Object[].class, String.class));

    Object handle(Object[] args) throws Throwable;

    class StaticDelegator {

        private static Map<String, StaticAnswer> delegators;

        static {
            delegators = new HashMap<>();
        }

        private StaticDelegator() {}


        public static void addDelegator(Method method, StaticAnswer staticAnswer) {
            delegators.put(method.toString(), staticAnswer);
        }

        @RuntimeType
        public static Object handle(@AllArguments Object[] args, @Origin String methodName) throws Throwable {
            //TODO: Get a more specific key that pertains to the class reference and all.
            if (delegators.containsKey(methodName)) {
                return delegators.get(methodName).handle(args);
            }
            throw new RuntimeException("Static Method " + methodName + " Not delegated");
        }

    }

}
