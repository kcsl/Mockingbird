package mock.answers;

import java.lang.reflect.Method;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public interface RedefineAnswer extends Answer {
    ElementMatcher<? super MethodDescription> MATCHER = ElementMatchers.named("handle").and(ElementMatchers.takesArguments(Object.class, Object[].class, Method.class));
    @RuntimeType
    Object handle(@This Object proxy, @AllArguments Object[] args, @Origin Method method) throws Throwable;

}
