package mock.matchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author Derrick Lockwood
 * @created 11/25/18.
 */
public class MethodMatchers {
    private MethodMatchers() {

    }

    public static ElementMatcher<? super MethodDescription> getMethodMatcher(String methodName, Class<?>... parameterClasses) {
        return ElementMatchers.named(methodName).and(ElementMatchers.takesArguments(parameterClasses));
    }

    public static ElementMatcher<? super MethodDescription> getMethodMatcher(String methodName, String... parameterClasses) {
        ElementMatcher.Junction<? super TypeDescription> elementMatcher = ElementMatchers.none();
        for (String s : parameterClasses) {
            elementMatcher = elementMatcher.or(ElementMatchers.named(s));
        }
        return ElementMatchers.named(methodName).and(ElementMatchers.takesArguments(ElementMatchers.whereAny(elementMatcher)));
    }
}
