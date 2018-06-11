package mock;

import java.util.HashMap;
import java.util.Map;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import mock.answers.NotStubbedAnswer;
import mock.answers.RedefineAnswer;
import mock.answers.SubAnswer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TargetedMockBuilder {

    private ByteBuddy byteBuddy;
    private Map<Class<?>, ObjectInstantiator<?>> instanceCreatorHolder;
    private final Objenesis objenesis;

    public TargetedMockBuilder() {
        //TODO: create rebase which is a mix of subclass and redefine where it keeps the old methods
        byteBuddy = new ByteBuddy();
        instanceCreatorHolder = new HashMap<>();
        objenesis = new ObjenesisStd();
    }

    static Implementation getSubAnswerImplementation(SubAnswer subAnswer) {
        return MethodDelegation.withDefaultConfiguration().filter(SubAnswer.MATCHER).to(subAnswer, SubAnswer.class);
    }

    static Implementation getRedefineAnswerImplementation(RedefineAnswer redefineAnswer) {
        return MethodDelegation.withDefaultConfiguration().filter(RedefineAnswer.MATCHER).to(redefineAnswer, RedefineAnswer.class);
    }

    public SubMockClass createSubclass(Class<?> type) {
        return createSubclass(type, NotStubbedAnswer.newInstance());
    }

    public SubMockClass createSubclass(Class<?> type, SubAnswer defaultSubAnswer) {
        if (defaultSubAnswer == null) {
            return createSubclass(type, getSubAnswerImplementation(NotStubbedAnswer.newInstance()));
        }
        return createSubclass(type, getSubAnswerImplementation(defaultSubAnswer));
    }

    public SubMockClass createSubclass(Class<?> type, Implementation defaultImplementation) {
        SubMockClass mockClass = new SubMockClass(this, type, byteBuddy.subclass(type, ConstructorStrategy.Default.NO_CONSTRUCTORS));
        if (defaultImplementation != null) {
            mockClass.setDefaultImplementation(defaultImplementation);
        }
        return mockClass;
    }

    public SubMockClass createSubclassRealMethods(Class<?> type) {
        return createSubclass(type, SuperMethodCall.INSTANCE);
    }

    public RedefineMockClass createRedefine(Class<?> type) {
        return createRedefine(type, NotStubbedAnswer.newInstance());
    }

    public RedefineMockClass createRedefine(Class<?> type, RedefineAnswer defaultRedefineAnswer) {
        return createRedefine(type, getRedefineAnswerImplementation(defaultRedefineAnswer));
    }

    public RedefineMockClass createRedefine(Class<?> type, Implementation defaultImplementation) {
        RedefineMockClass mockClass = new RedefineMockClass(this, type, byteBuddy.redefine(type));
        if (defaultImplementation != null) {
            mockClass.setDefaultImplementation(defaultImplementation);
        }
        return mockClass;
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectInstantiator<T> getInstantiator(Class<T> tClass) {
        return (ObjectInstantiator<T>) instanceCreatorHolder.get(tClass);
    }

    public <T> T newInstance(Class<T> tClass) {
        return getInstantiator(tClass).newInstance();
    }

    void setObjectInstantiator(Class<?> type, ObjectInstantiator<?> objectInstantiator) {
        instanceCreatorHolder.put(type, objectInstantiator);
    }

    ObjectInstantiator<?> createObjectInstantiator(Class<?> oldType, Class<?> newType) {
        ObjectInstantiator<?> objectInstantiator = objenesis.getInstantiatorOf(newType);
        instanceCreatorHolder.put(oldType, objectInstantiator);
        return objectInstantiator;
    }

    @SuppressWarnings("unchecked")
    <T> ObjectInstantiator<T> createObjectInstantiator(T value) {
        Class<T> tClass = (Class<T>) value.getClass();
        ObjectInstantiator<T> objectInstantiator = () -> value;
        instanceCreatorHolder.put(tClass, objectInstantiator);
        return objectInstantiator;
    }

    <T> void addObjectInstantiator(Class<T> tClass, ObjectInstantiator<T> objectInstantiator) {
        instanceCreatorHolder.put(tClass, objectInstantiator);
    }

}
