package mock;

import instrumentor.AFLMethodVisitor;
import mock.answers.Answer;
import mock.answers.AnswerInstantiator;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Derrick Lockwood
 * @created 10/25/18.
 */
public class TransformMockClass implements TransformClassLoader.Transformer, MockCreator {

    private final String canonicalName;
    private final boolean isPrimitive;
    private String name;
    private List<TransformClassLoader.Transformer> transformers;
    private Class<?> transformedClass;
    private ElementMatcher.Junction<? super MethodDescription> transformedMethods;
    private final Map<ElementMatcher<? super MethodDescription>, String> matcherToMethodField;
    private ObjectInstantiator<?> objectInstantiator;
    private final ClassInterceptor interceptor;
    TransformClassLoader tiedClassLoader;

    public TransformMockClass(String canonicalName) {
        this.canonicalName = canonicalName;
        this.name = name + "_" + canonicalName;
        transformers = new LinkedList<>();
        tiedClassLoader = null;
        transformedClass = getClassForString(canonicalName, null);
        isPrimitive = transformedClass != null;
        transformedMethods = ElementMatchers.none();
        matcherToMethodField = new HashMap<>();
        objectInstantiator = null;
        interceptor = new ClassInterceptor(this);
        transformers.add(interceptor);
        transformedMethods = transformedMethods.or(interceptor.getInterceptorMatcher());
    }

    String getMethodFieldName(ElementMatcher<? super MethodDescription> methodMatcher) {
        String fieldName = matcherToMethodField.get(methodMatcher);
        if (fieldName == null) {
            transformedMethods = transformedMethods.or(methodMatcher);
            fieldName = getCanonicalName().replaceAll("[.]", "_") + "_" + matcherToMethodField.size();
            matcherToMethodField.put(methodMatcher, fieldName);
            String finalFieldName = fieldName;
            transformers.add(
                    builder -> builder.defineField(finalFieldName, Answer.class, Modifier.PUBLIC)
                            .method(methodMatcher)
                            .intercept(MethodDelegation.toField(finalFieldName)));
            interceptor.initMethod(fieldName);
        }
        return fieldName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    @Override
    public Class<?> loadClass() throws ClassNotFoundException {
        if (transformedClass == null && tiedClassLoader != null) {
            transformedClass = getClassForString(canonicalName, tiedClassLoader);
        }
        return transformedClass;
    }

    public Method loadMethod(String methodName, Class<?>... params) throws ClassNotFoundException,
            NoSuchMethodException {
        Class<?> loadedClass = loadClass();
        if (loadedClass != null) {
            return loadedClass.getDeclaredMethod(methodName, params);
        }
        throw new ClassNotLoadedException("Mock Class (" + canonicalName + ") isn't loaded yet");
    }

    public Field loadField(String fieldName) throws ClassNotFoundException, NoSuchFieldException {
        Class<?> loadedClass = loadClass();
        if (loadedClass != null) {
            return loadedClass.getField(fieldName);
        }
        throw new ClassNotLoadedException("Mock Class (" + canonicalName + ") isn't loaded yet");
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder) {
        if (transformedClass != null) {
            return builder;
        }
        for (TransformClassLoader.Transformer transformer : transformers) {
            builder = transformer.transform(builder);
        }
        builder = AFLMethodVisitor.applyAFLTransformation(builder, ElementMatchers.not(transformedMethods));

        return builder;
    }

    @Override
    public boolean isPrimitive() {
        return isPrimitive;
    }

    @Override
    public ObjectInstantiator<?> getObjectInstantiator(ClassMap classMap) throws ClassNotFoundException {
        if (this.objectInstantiator == null && tiedClassLoader != null && !isPrimitive) {
            objectInstantiator = tiedClassLoader.getInstantiator(loadClass());
        }
        if (!classMap.isAssociated(this)){
            classMap.associateWithMockClass(this);
        }
        ObjectInstantiator<?> objectInstantiator = this.objectInstantiator;
        if (classMap.getConstructAnswer() != null) {
            objectInstantiator = new AnswerInstantiator(classMap.getConstructAnswer(), loadClass());
        } else if (isPrimitive) {
            throw new RuntimeException("Primitive Class doesn't have answer");
        }
        if (objectInstantiator != null) {
            ObjectInstantiator<?> finalObjectInstantiator = objectInstantiator;
            return new ObjectInstantiator<>() {

                private Object instance;

                @Override
                public Object newInstance() {
                    if (classMap.getLoadEveryInstantiation() || instance == null) {
                        instance = finalObjectInstantiator.newInstance();
                        if (isPrimitive) {
                            return instance;
                        }
                        try {
                            interceptor.applyMap(instance, classMap);
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            Throwable t = e.getCause();
                            if (t == null) {
                                t = e;
                            }
                            throw new RuntimeException(t);
                        }
                    }
                    return instance;
                }
            };
        }
        throw new ClassNotLoadedException("Mock Class (" + canonicalName + ") isn't loaded yet");
    }

    private static Class<?> getClassForString(String name, ClassLoader classLoader) {
        switch (name) {
            case "Boolean":
            case "boolean":
                return boolean.class;
            case "Byte":
            case "byte":
                return byte.class;
            case "Character":
            case "char":
                return char.class;
            case "Short":
            case "short":
                return short.class;
            case "Integer":
            case "int":
                return int.class;
            case "Long":
            case "long":
                return long.class;
            case "Float":
            case "float":
                return float.class;
            case "Double":
            case "double":
                return double.class;
            case "String":
                return String.class;
            default:
                if (classLoader != null) {
                    try {
                        return Class.forName(name, true, classLoader);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
        }
    }

    private class ClassNotLoadedException extends RuntimeException {
        public ClassNotLoadedException(String msg) {
            super(msg);
        }
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }

    @Override
    public int hashCode() {
        return getCanonicalName().hashCode();
    }
}
