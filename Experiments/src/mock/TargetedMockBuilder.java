package mock;

import mock.answers.Answer;
import mock.answers.NotStubbedAnswer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TargetedMockBuilder {

    private final Objenesis objenesis;
    private ByteBuddy byteBuddy;
    private Map<Class<?>, ObjectInstantiator<?>> instanceCreatorHolder;
    private Map<String, Class<?>> namedInstanceMap;

    public TargetedMockBuilder() {
        //TODO: create rebase which is a mix of subclass and redefine where it keeps the old methods
        byteBuddy = new ByteBuddy();
        instanceCreatorHolder = new HashMap<>();
        namedInstanceMap = new HashMap<>();
        objenesis = new ObjenesisStd();
    }

    static Implementation getSubAnswerImplementation(Answer subAnswer) {
        return getImplementation(subAnswer, Answer.SUB_MATCHER);
    }

    static Implementation getRedefineAnswerImplementation(Answer redefineAnswer) {
        return getImplementation(redefineAnswer, Answer.REDEFINE_MATCHER);
    }

    private static Implementation getImplementation(Answer answer, ElementMatcher<? super MethodDescription> matcher) {
        return MethodDelegation.withDefaultConfiguration().filter(matcher).to(answer, Answer.class);
    }

    public SubMockClass createSubclass(Class<?> type) {
        return createSubclass(type, NotStubbedAnswer.newInstance());
    }

    public SubMockClass createSubclass(Class<?> type, Answer defaultSubAnswer) {
        if (defaultSubAnswer == null) {
            return createSubclass(type, getSubAnswerImplementation(NotStubbedAnswer.newInstance()));
        }
        return createSubclass(type, getSubAnswerImplementation(defaultSubAnswer));
    }

    public SubMockClass createSubclass(Class<?> type, Implementation defaultImplementation) {
        SubMockClass mockClass = new SubMockClass(this, type,
                byteBuddy.subclass(type, ConstructorStrategy.Default.NO_CONSTRUCTORS));
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

    public RedefineMockClass createRedefine(Class<?> type, Answer defaultRedefineAnswer) {
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
        namedInstanceMap.put(name, newType);
        return createObjectInstantiator(newType);
    }

    <T> ObjectInstantiator<T> createObjectInstantiator(Class<T> newType) {
        ObjectInstantiator<T> objectInstantiator = objenesis.getInstantiatorOf(newType);
        instanceCreatorHolder.put(newType, objectInstantiator);
        return objectInstantiator;
    }

    @SuppressWarnings("unchecked")
    <T> ObjectInstantiator<T> createObjectInstantiator(T value) {
        Class<T> tClass = (Class<T>) value.getClass();
        ObjectInstantiator<T> objectInstantiator = () -> value;
        instanceCreatorHolder.put(tClass, objectInstantiator);
        return objectInstantiator;
    }

}
