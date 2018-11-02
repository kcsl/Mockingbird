package mock;

import method.AttributeClass;
import mock.answers.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TargetedMockBuilderDEL {

    private final Objenesis objenesis;
    private ByteBuddy byteBuddy;
    private Map<Class<?>, ObjectInstantiator<?>> instanceCreatorHolder;
    private Map<String, Class<?>> namedInstanceMap;

    public TargetedMockBuilderDEL() {
        //TODO: create rebase which is a mix of subclass and redefine where it keeps the old methods
        byteBuddy = new ByteBuddy();
        instanceCreatorHolder = new HashMap<>();
        namedInstanceMap = new HashMap<>();
        objenesis = new ObjenesisStd();
    }

    static Implementation getRedefineAnswerImplementation(Answer redefineAnswer) {
        return getImplementation(redefineAnswer, Answer.REDEFINE_MATCHER);
    }

    static Implementation getImplementation(Answer answer, ElementMatcher<? super MethodDescription> matcher) {
        return MethodDelegation.withDefaultConfiguration().filter(matcher).to(answer, Answer.class);
    }

    public MultipleMockClass createMultipleMockClass(AttributeClass attributeClass) {
        return new MultipleMockClass(this,
                byteBuddy.subclass(attributeClass.getMockClass(), ConstructorStrategy.Default.NO_CONSTRUCTORS),
                attributeClass);
    }

    public SubMockClass createSubclass(Class<?> type) {
        return createSubclass(type, NotStubbedAnswer.newInstance());
    }

    private SubMockClass createSubclass(Class<?> type, ConstructParamAnswer constructorAnswer) {
        return new SubMockClass(this, type, byteBuddy.subclass(type, ConstructorStrategy.Default.IMITATE_SUPER_CLASS), constructorAnswer);
    }

    public SubMockClass createSubclass(Class<?> type, Answer defaultSubAnswer, ConstructParamAnswer constructorAnswer) {
        SubMockClass mockClass = createSubclass(type, constructorAnswer);
        if (defaultSubAnswer != null) {
            mockClass.setDefaultImplementation(mockClass.getImplementation(defaultSubAnswer));
        }
        return mockClass;
    }

    public SubMockClass createSubclass(Class<?> type, Answer defaultSubAnswer) {
        return createSubclass(type, defaultSubAnswer, null);
    }

    public SubMockClass createSubclass(Class<?> type, Implementation defaultImplementation, ConstructParamAnswer constructorAnswer) {
        SubMockClass mockClass = createSubclass(type, constructorAnswer);
        if (defaultImplementation != null) {
            mockClass.setDefaultImplementation(defaultImplementation);
        }
        return mockClass;
    }

    public SubMockClass createSubclass(Class<?> type, Implementation defaultImplementation) {
        return createSubclass(type, defaultImplementation, null);
    }

    public SubMockClass createSubclassRealMethods(Class<?> type, ConstructParamAnswer constructorAnswer) {
        return createSubclass(type, SuperMethodCall.INSTANCE, constructorAnswer);
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectInstantiator<T> getInstantiator(String name) {
        Class<T> tClass = (Class<T>) namedInstanceMap.get(name);
        return tClass == null ? null : getInstantiator(tClass);
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectInstantiator<T> getInstantiator(Class<T> tClass) {
        return (ObjectInstantiator<T>) instanceCreatorHolder.get(tClass);
    }

    public <T> T newInstance(Class<T> tClass) {
        return getInstantiator(tClass).newInstance();
    }

    @SuppressWarnings("unchecked")
    public <T> T newInstance(String name) {
        return (T) getInstantiator(name).newInstance();
    }

    void setObjectInstantiator(Class<?> type, ObjectInstantiator<?> objectInstantiator) {
        instanceCreatorHolder.put(type, objectInstantiator);
    }

    void setNamedInstance(String name, Class<?> type) {
        namedInstanceMap.put(name, type);
    }

    <T> ObjectInstantiator<T> createObjectInstantiator(String name, Class<T> newType) {
        ObjectInstantiator<T> objectInstantiator = objenesis.getInstantiatorOf(newType);
        instanceCreatorHolder.put(newType, objectInstantiator);
        if (name != null) {
            namedInstanceMap.put(name, newType);
        }
        return objectInstantiator;
    }

    @SuppressWarnings("unchecked")
    <T> ObjectInstantiator<T> createObjectInstantiator(String name, T value) {
        Class<T> tClass = (Class<T>) value.getClass();
        ObjectInstantiator<T> objectInstantiator = () -> value;
        instanceCreatorHolder.put(tClass, objectInstantiator);
        return objectInstantiator;
    }

    <T> ObjectInstantiator<T> createObjectInstantiator(String name, Class<T> newType, ConstructParamAnswer constructorAnswer) {
        System.out.println(constructorAnswer);
        ObjectInstantiator<T> objectInstantiator = () -> newType.cast(constructorAnswer.applyReturnType(newType, true));
        instanceCreatorHolder.put(newType, objectInstantiator);
        if (name != null) {
            namedInstanceMap.put(name, newType);
        }
        return objectInstantiator;
    }

}
