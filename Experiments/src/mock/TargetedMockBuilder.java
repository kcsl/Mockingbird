package mock;

import mock.answers.Answer;
import mock.answers.ConstructParamAnswer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 10/22/18.
 */
public class TargetedMockBuilder {

    private final ByteBuddy byteBuddy;
    private final ClassLoader classLoader;
    private final Set<File> saveLocations;

    public TargetedMockBuilder() {
        this(ClassLoader.getSystemClassLoader());
    }

    public TargetedMockBuilder(ClassLoader classLoader) {
        byteBuddy = new ByteBuddy();
        this.classLoader = classLoader;
        saveLocations = new HashSet<>();

    }

    public RebaseMockClass<?> rebaseClass(String name) throws
            ClassNotFoundException {
        return rebaseClass(name, null);
    }

    public <T> RebaseMockClass<T> rebaseClass(Class<T> clazz) {
        return rebaseClass(clazz, null);
    }

    public RebaseMockClass<?> rebaseClass(String name, ConstructParamAnswer constructParamAnswer) throws
            ClassNotFoundException {
        return rebaseClass(classLoader.loadClass(name), constructParamAnswer);
    }

    public <T> RebaseMockClass<T> rebaseClass(Class<T> clazz, ConstructParamAnswer constructParamAnswer) {
        return new RebaseMockClass<T>(this, clazz,
                byteBuddy.rebase(clazz, ClassFileLocator.ForClassLoader.of(classLoader)),
                constructParamAnswer);
    }

    public static Implementation getImplementation(Answer answer) {
        return MethodDelegation.withDefaultConfiguration().filter(ElementMatchers.named("handle").and(
                ElementMatchers.takesArguments(Object.class, Object[].class, Callable.class, Method.class)).or(
                ElementMatchers.takesArguments(Object.class, Object[].class, Method.class))).to(answer, Answer.class);
    }

    public static class EmptyInterceptor {
        public void intercept() {
            System.out.println("Intercepted");
        }
    }

    void addSaveLocation(File saveLocation) {
        saveLocations.add(saveLocation);
    }

    public ClassLoader createSavedLocationsLoader() {
        return new URLClassLoader(saveLocations.stream().map(a -> {
            try {
                return a.toURI().toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }).toArray(URL[]::new));
    }

}
