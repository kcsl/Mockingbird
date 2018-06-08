package mock.answers;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 5/22/18.
 */
public interface SubAnswer extends Answer {
    ElementMatcher<? super MethodDescription> MATCHER = ElementMatchers.named("handle").and(ElementMatchers.takesArguments(Object.class, Object[].class, Callable.class, Method.class));

    @RuntimeType
    Object handle(@This Object proxy, @AllArguments Object[] args, @SuperCall Callable<Object> originalMethod, @Origin Method method) throws Throwable;

}
