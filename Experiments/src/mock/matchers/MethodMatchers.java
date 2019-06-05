package mock.matchers;

import net.bytebuddy.description.TypeVariableSource;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.annotation.AnnotationValue;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.modifier.*;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.matcher.CollectionElementMatcher;
import net.bytebuddy.matcher.CollectionOneToOneMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        ElementMatcher.Junction<? super MethodDescription> elementMatcher = ElementMatchers.named(methodName);
        for (int i = 0; i < parameterClasses.length; i++) {
            elementMatcher = elementMatcher.and(ElementMatchers.takesArgument(i, ElementMatchers.named(parameterClasses[i])));
        }
        return elementMatcher;
    }

    public static MethodDescription methodToDescription(Method method) {
        return new MethodDescription.ForLoadedMethod(method);
    }
}
