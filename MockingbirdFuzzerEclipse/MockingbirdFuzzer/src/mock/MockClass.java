package mock;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.objenesis.instantiator.ObjectInstantiator;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author Derrick Lockwood
 * @created 5/24/18.
 */
public abstract class MockClass implements MockCreator{

    final Class<?> oldType;
    private final TargetedMockBuilder targetedMockBuilder;
    private DynamicType.Builder<?> builder;
    private Class<?> newType;
    private FieldSetInterceptor fieldSetInterceptor;
    private ObjectInstantiator<?> objectInstantiator;

    MockClass(TargetedMockBuilder targetedMockBuilder,
              Class<?> oldType,
              DynamicType.Builder<?> builder) {
        this.oldType = oldType;
        this.targetedMockBuilder = targetedMockBuilder;
        fieldSetInterceptor = new FieldSetInterceptor(oldType.getSimpleName());
        this.builder = builder;
        this.newType = null;
    }

    public void setDefaultImplementation(Implementation implementation) {
        builder = builder.method(ElementMatchers.any())
                .intercept(implementation);
    }

    public Class<?> getOldType() {
        return oldType;
    }

    public Class<?> getNewType() {
        return newType;
    }

    public MockClass applyMethod(Implementation implementation, Method method) {
        method.setAccessible(true);
        builder = builder.method(ElementMatchers.is(method))
                .intercept(implementation);
        return this;
    }

    public <T> MockClass applyField(String fieldName, T value) throws NoSuchFieldException {
        return applyField(oldType.getDeclaredField(fieldName), value);
    }

    public <T> MockClass applyField(Field field, T value) {
        fieldSetInterceptor.putField(field, targetedMockBuilder.createObjectInstantiator(value));
        return this;
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, String fieldName) throws NoSuchFieldException {
        return applyField(tClass, objectInstantiator, oldType.getDeclaredField(fieldName));
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, Field field) {
        targetedMockBuilder.addObjectInstantiator(tClass, objectInstantiator);
        fieldSetInterceptor.putField(field, objectInstantiator);
        return this;
    }

    public MockClass applyField(String fieldName, ObjectInstantiator<?> objectInstantiator) throws NoSuchFieldException {
        return applyField(oldType.getDeclaredField(fieldName), objectInstantiator);
    }

    public MockClass applyField(Field field, ObjectInstantiator<?> objectInstantiator) {
        fieldSetInterceptor.putField(field, objectInstantiator);
        return this;
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
        objectInstantiator = targetedMockBuilder.createObjectInstantiator(oldType, newType);
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

}
