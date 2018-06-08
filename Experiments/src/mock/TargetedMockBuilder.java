package mock;

import mock.answers.NotStubbedAnswer;
import mock.answers.RedefineAnswer;
import mock.answers.SubAnswer;
import mock.answers.StaticAnswer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Origin;
import org.objenesis.instantiator.ObjectInstantiator;

import java.util.Objects;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TargetedMockBuilder {

    private ByteBuddy byteBuddy;
    private InstanceCreatorHolder instanceCreatorHolder;

    public TargetedMockBuilder() {
        //TODO: create rebase which is a mix of subclass and redefine where it keeps the old methods
        byteBuddy = new ByteBuddy();
        instanceCreatorHolder = new InstanceCreatorHolder();
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
        return createSubclass(type, getSubAnswerImplementation(Objects.requireNonNullElseGet(defaultSubAnswer, NotStubbedAnswer::newInstance)));
    }

    public SubMockClass createSubclass(Class<?> type, Implementation defaultImplementation) {
        SubMockClass mockClass = new SubMockClass(this, type, byteBuddy.subclass(type, ConstructorStrategy.Default.NO_CONSTRUCTORS), instanceCreatorHolder);
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
        RedefineMockClass mockClass = new RedefineMockClass(this, type, byteBuddy.redefine(type), instanceCreatorHolder);
        if (defaultImplementation != null) {
            mockClass.setDefaultImplementation(defaultImplementation);
        }
        return mockClass;
    }

    void setObjectInstantiator(Class<?> type, ObjectInstantiator<?> objectInstantiator) {
        instanceCreatorHolder.addObjectInstantiator(type, objectInstantiator);
    }

    ObjectInstantiator<?> createObjectInstantiator(Class<?> oldType, Class<?> newType) {
        return instanceCreatorHolder.createObjectInstantiator(oldType, newType);
    }

    Object newMockInstance(Class<?> clazz) {
        return instanceCreatorHolder.getInstance(clazz);
    }

}
