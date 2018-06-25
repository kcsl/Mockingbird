package mock.answers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public interface Answer {
    ElementMatcher<? super MethodDescription> SUB_MATCHER = ElementMatchers.named("handle").and(
            ElementMatchers.takesArguments(Object.class, Object[].class, Callable.class, Method.class));

    ElementMatcher<? super MethodDescription> STATIC_MATCHER = ElementMatchers.named("handle").and(
            ElementMatchers.takesArguments(Object[].class, String.class));

    ElementMatcher<? super MethodDescription> REDEFINE_MATCHER = ElementMatchers.named("handle").and(
            ElementMatchers.takesArguments(Object.class, Object[].class, Method.class));

    Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType);

    @RuntimeType
    Object handle(@This Object proxy, @AllArguments Object[] args, @Origin Method method) throws Throwable;

    @RuntimeType
    Object handle(@This Object proxy, @AllArguments Object[] args, @SuperCall Callable<Object> originalMethod,
            @Origin Method method) throws
            Throwable;


    Object handle(Object[] args) throws Throwable;

    default Answer link(Answer answer) {
        if (answer == null) {
            return this;
        }
        Answer self = this;
        return new Answer() {
            @Override
            public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
                return answer.handle(proxy, appendReturn(parameters, self.handle(proxy, parameters, name, returnType)),
                        name, returnType);
            }

            @Override
            public Object handle(Object proxy, Object[] args, Method method) throws Throwable {
                return answer.handle(proxy, appendReturn(args, self.handle(proxy, args, method)), method);
            }

            @Override
            public Object handle(Object proxy, Object[] args, Callable<Object> originalMethod, Method method) throws
                    Throwable {
                return answer.handle(proxy, appendReturn(args, self.handle(proxy, args, originalMethod, method)), originalMethod, method);
            }

            @Override
            public Object handle(Object[] args) throws Throwable {
                return answer.handle(appendReturn(args, self.handle(args)));
            }

            private Object[] appendReturn(Object[] parameters, Object returnVal) {
                Object[] newParameters = Arrays.copyOf(parameters, parameters.length + 1);
                newParameters[newParameters.length - 1] = returnVal;
                return newParameters;
            }
        };
    }

    class StaticDelegator {

        private static Map<String, Answer> delegators;

        static {
            delegators = new HashMap<>();
        }

        private StaticDelegator() {
        }


        public static void addDelegator(Method method, Answer staticAnswer) {
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
