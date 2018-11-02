package mock;

import mock.answers.Answer;
import mock.answers.ConstructParamAnswer;
import mock.answers.FixedAnswer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 10/25/18.
 */
public class TransformMockClass implements TransformClassLoader.Transformer {

    private final String canonicalName;
    private final ConstructParamAnswer constructParamAnswer;
    private List<TransformClassLoader.Transformer> transformers;
    private FieldInterceptor fieldInterceptor;
    private Class<?> transformedClass;
    TransformClassLoader tiedClassLoader;

    public TransformMockClass(String canonicalName, ConstructParamAnswer constructParamAnswer) {
        this.canonicalName = canonicalName;
        this.constructParamAnswer = constructParamAnswer;
        fieldInterceptor = new FieldInterceptor(canonicalName);
        transformers = new LinkedList<>();
        tiedClassLoader = null;
        transformedClass = null;
    }

    public TransformMockClass(String canonicalName) {
        this(canonicalName, null);
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void applyMethod(Answer answer, String methodName, Class<?>... args) {
        transformers.add(
                builder -> builder.method(ElementMatchers.named(methodName).and(ElementMatchers.takesArguments(args)))
                        .intercept(TransformClassLoader.getImplementation(answer)));
    }

    public <V> void applyMethod(V value, String methodName, Class<?>... parameters) {
        applyMethod(new FixedAnswer(value), methodName, parameters);
    }

    public void applyField(String fieldName, Object value) {
        fieldInterceptor.setFieldInstanciator(fieldName, new FixedAnswer(value));
    }

    public void applyField(String fieldName, Answer answerCreator) {
        fieldInterceptor.setFieldInstanciator(fieldName, answerCreator);
    }

    public Class<?> loadClass() throws ClassNotFoundException {
        if (transformedClass == null && tiedClassLoader != null) {
            transformedClass = tiedClassLoader.loadClass(canonicalName);
        }
        return transformedClass;
    }

    @SuppressWarnings("unchecked")
    public <V> V newInstance() throws
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        V v = null;
        if (constructParamAnswer != null) {
            v = (V) constructParamAnswer.applyReturnType(loadClass(), true);
        } else if (tiedClassLoader != null) {
            v = (V) tiedClassLoader.newInstance(loadClass());
        }
        if (v != null) {
            fieldInterceptor.reloadParameters(v);
            return v;
        }
        throw new ClassNotLoadedException("Mock Class (" + canonicalName + ") isn't loaded yet");
    }

    public Method loadMethod(String methodName, Class<?>... params) throws ClassNotFoundException,
            NoSuchMethodException {
        Class<?> loadedClass = loadClass();
        if (loadedClass != null) {
            return loadedClass.getMethod(methodName, params);
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

    public void reloadInstanceVariables(Object o, String... variableNamesToReload) throws
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        Class<?> loadedClass = loadClass();
        if (o == null || !o.getClass().equals(loadedClass)){
            return;
        }
        fieldInterceptor.reloadParameters(o, variableNamesToReload);
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder) {
        for (TransformClassLoader.Transformer transformer : transformers) {
            builder = transformer.transform(builder);
        }
        return fieldInterceptor.addFieldInterceptor(builder);
    }

    private class ClassNotLoadedException extends RuntimeException {
        public ClassNotLoadedException(String msg) {
            super(msg);
        }
    }

}
