package mock;

import mock.answers.Answer;
import mock.answers.EmptyAnswer;
import mock.answers.FixedAnswer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 5/24/18.
 */
public abstract class MockClass implements MockCreator {

    final Class<?> oldType;
    private final TargetedMockBuilder targetedMockBuilder;
    private DynamicType.Builder<?> builder;
    private Class<?> newType;
    private FieldSetInterceptor fieldSetInterceptor;
    private ObjectInstantiator<?> objectInstantiator;
    private String name;
    private Map<Method, Answer> methodMap;

    MockClass(
            TargetedMockBuilder targetedMockBuilder,
            Class<?> oldType,
            DynamicType.Builder<?> builder) {
        this.oldType = oldType;
        this.targetedMockBuilder = targetedMockBuilder;
        fieldSetInterceptor = new FieldSetInterceptor(oldType.getSimpleName());
        this.builder = builder;
        this.newType = null;
        methodMap = new HashMap<>();
        name = null;
    }

    public void setDefaultImplementation(Implementation implementation) {
        builder = builder.method(ElementMatchers.any())
                .intercept(implementation);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getOldType() {
        return oldType;
    }

    public Class<?> getNewType() {
        return newType;
    }

    @Override
    public Class<?> getType() {
        return newType;
    }

    public String getName() {
        return name;
    }

    public Answer getAnswer(String methodName, Class<?>[] parameters) throws NoSuchMethodException {
        return getAnswer(oldType.getMethod(methodName, parameters));
    }

    public Answer getAnswer(Method method) {
        return methodMap.get(method);
    }

    public <T> MockClass applyField(String fieldName, T value) throws NoSuchFieldException {
        return applyField(oldType.getDeclaredField(fieldName), value);
    }

    public <T> MockClass applyField(Field field, T value) {
        fieldSetInterceptor.putField(field, targetedMockBuilder.createObjectInstantiator(value));
        return this;
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, String fieldName) throws
            NoSuchFieldException {
        return applyField(tClass, objectInstantiator, oldType.getDeclaredField(fieldName));
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, Field field) {
        targetedMockBuilder.setObjectInstantiator(tClass, objectInstantiator);
        //TODO: Do this? what happens if this is true?
        targetedMockBuilder.setNamedInstance(field.getName(), tClass);
        fieldSetInterceptor.putField(field, objectInstantiator);
        return this;
    }

    public MockClass applyField(String fieldName, ObjectInstantiator<?> objectInstantiator) throws
            NoSuchFieldException {
        return applyField(oldType.getDeclaredField(fieldName), objectInstantiator);
    }

    public MockClass applyField(Field field, ObjectInstantiator<?> objectInstantiator) {
        fieldSetInterceptor.putField(field, objectInstantiator);
        return this;
    }

    public MockClass applyMethod(Implementation implementation, Method method) {
        method.setAccessible(true);
        builder = builder.method(ElementMatchers.is(method))
                .intercept(implementation);
        return this;
    }

    public MockClass applyMethod(String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(new EmptyAnswer(), methodName, parameters);
    }

    public <T> MockClass applyMethod(T value, String methodName, Class<?>... parameters) throws
            NoSuchMethodException {
        return applyMethod(value, oldType.getMethod(methodName, parameters));
    }

    public <T> MockClass applyMethod(T value, Method method) {
        return applyMethod(new FixedAnswer(value), method);
    }

    public MockClass applyMethod(Answer answer, String methodName, Class<?>... parameters) throws
            NoSuchMethodException {
        return applyMethod(answer, oldType.getMethod(methodName, parameters));
    }

    public MockClass applyMethod(Answer answer, Method method) {
        methodMap.put(method, answer);
        return applyMethod(getImplementation(answer), method);
    }


    private Object create() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object newObject;
        if (newType != null) {
            newObject = objectInstantiator.newInstance();
        } else {
            newObject = makeInstantiator().newInstance();
        }
        fieldSetInterceptor.reloadParameters(newObject);
        return newObject;
    }

    public void reloadInstanceVariables(Object object, List<String> instanceVariables) {
        if (!object.getClass().equals(newType)) {
            return;
        }
        try {
            fieldSetInterceptor.reloadParameters(object, instanceVariables.toArray(new String[0]));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void store() {
        if (newType != null) {
            return;
        }
        makeInstantiator();
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    private ObjectInstantiator makeInstantiator() {
        builder = fieldSetInterceptor.addFields(builder);
        newType = createClass(builder.make());
        builder = null;
        if (name != null) {
            objectInstantiator = targetedMockBuilder.createObjectInstantiator(name, newType);
        } else {
            objectInstantiator = targetedMockBuilder.createObjectInstantiator(newType);
        }
        return objectInstantiator;
    }

    @Override
    public Object newInstance() {
        try {
            return create();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    abstract Class<?> createClass(DynamicType.Unloaded<?> unloaded);

    abstract Implementation getImplementation(Answer answer);

}
