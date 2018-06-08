package mock;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Derrick Lockwood
 * @created 5/24/18.
 */
public abstract class MockClass implements MockCreator {

    final Class<?> oldType;
    private final TargetedMockBuilder targetedMockBuilder;
    private DynamicType.Builder<?> builder;
    private Class<?> newType;
    private InstanceCreatorHolder instanceCreatorHolder;
    private FieldSetInterceptor fieldSetInterceptor;

    MockClass(TargetedMockBuilder targetedMockBuilder,
              Class<?> oldType,
              DynamicType.Builder<?> builder,
              InstanceCreatorHolder parentHolder) {
        this.oldType = oldType;
        this.targetedMockBuilder = targetedMockBuilder;
        instanceCreatorHolder = new InstanceCreatorHolder(parentHolder);
        fieldSetInterceptor = new FieldSetInterceptor(instanceCreatorHolder, oldType.getSimpleName());
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

    public <T> MockClass applyField(T value, String fieldName) throws NoSuchFieldException {
        return applyField(value, oldType.getDeclaredField(fieldName));
    }

    public <T> MockClass applyField(T value, Field field) {
        instanceCreatorHolder.addFixedInstantiator(value);
        fieldSetInterceptor.putField(field, value.getClass());
        return this;
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, String fieldName) throws NoSuchFieldException {
        return applyField(tClass, objectInstantiator, oldType.getDeclaredField(fieldName));
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, Field field) {
        instanceCreatorHolder.addObjectInstantiator(tClass, objectInstantiator);
        fieldSetInterceptor.putField(field, tClass);
        return this;
    }

    public MockClass applyField(Class<?> mockClass, String fieldName) throws NoSuchFieldException {
        return applyField(mockClass, oldType.getDeclaredField(fieldName));
    }

    public MockClass applyField(Class<?> mockClass, Field field) {
        fieldSetInterceptor.putField(field, mockClass);
        return this;
    }


    public Object create() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (newType != null) {
            return targetedMockBuilder.newMockInstance(newType);
        }
        Object object = makeInstantiator().newInstance();
        if (!fieldSetInterceptor.isEmpty()) {
            Method method = newType.getDeclaredMethod(fieldSetInterceptor.getInterceptorName());
            method.invoke(object);
        }
        return object;
    }

    public void store() {
        if (newType != null) {
            return;
        }
        makeInstantiator();
    }

    public void reloadParameters(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (!object.getClass().equals(newType)) {
            return;
        }
        //TODO: only reload certain parameters? using method.invoke(object, Parameters to reload) (null for all)
        Method method = newType.getDeclaredMethod(fieldSetInterceptor.getInterceptorName());
        method.invoke(object);
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    private ObjectInstantiator makeInstantiator() {
        if (!fieldSetInterceptor.isEmpty()) {
            builder = builder.defineMethod(fieldSetInterceptor.getInterceptorName(), Void.TYPE, Modifier.PUBLIC)
                    .intercept(MethodDelegation.withDefaultConfiguration().
                            filter(ElementMatchers.named("intercept")).
                            to(fieldSetInterceptor));
        }
        newType = createClass(builder.make());
        builder = null;
        return targetedMockBuilder.createObjectInstantiator(oldType, newType);
    }

    abstract Class<?> createClass(DynamicType.Unloaded<?> unloaded);

}
