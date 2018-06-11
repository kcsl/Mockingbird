package mock.answers;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author Derrick Lockwood
 * @created 5/22/18.
 */
public interface SubAnswer extends Answer {
    ElementMatcher<? super MethodDescription> MATCHER = ElementMatchers.named("handle").and(ElementMatchers.takesArguments(Object.class, Object[].class, Callable.class, Method.class));

    @RuntimeType
    Object handle(@This Object proxy, @AllArguments Object[] args, @SuperCall Callable<Object> originalMethod, @Origin Method method) throws Throwable;

}
