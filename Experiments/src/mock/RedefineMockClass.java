package mock;

import mock.answers.Answer;
import mock.answers.FixedAnswer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.reflect.Method;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class RedefineMockClass extends MockClass {

    private boolean staticImplApplied;

    RedefineMockClass(TargetedMockBuilder targetedMockBuilder, Class<?> oldType, DynamicType.Builder<?> builder) {
        super(targetedMockBuilder, oldType, builder);
        staticImplApplied = false;
    }

    @Override
    Class<?> createClass(DynamicType.Unloaded<?> unloaded) {
        return unloaded.load(getClass().getClassLoader(), ClassReloadingStrategy.fromInstalledAgent())
                .getLoaded();
    }

    @Override
    Implementation getImplementation(Answer answer) {
        return TargetedMockBuilder.getRedefineAnswerImplementation(answer);
    }

    public RedefineMockClass applyStaticMethod(Answer answer, String methodName, Class<?>... params) throws
            NoSuchMethodException {
        return applyStaticMethod(answer, oldType.getMethod(methodName, params));
    }

    public RedefineMockClass applyStaticMethod(Answer answer, Method method) {
        Answer.StaticDelegator.addDelegator(method, answer);
        if (!staticImplApplied) {
            staticImplApplied = true;
            return (RedefineMockClass) applyMethod(
                    MethodDelegation.withDefaultConfiguration().filter(Answer.STATIC_MATCHER).to(
                            Answer.StaticDelegator.class),
                    method);
        }
        return this;
    }

    public <V> RedefineMockClass applyStaticMethod(V value, String methodName, Class<?>... params) throws
            NoSuchMethodException {
        return applyStaticMethod(new FixedAnswer(value), methodName, params);
    }

    public <V> RedefineMockClass applyStaticMethod(V value, Method method) {
        return applyStaticMethod(new FixedAnswer(value), method);
    }

}
