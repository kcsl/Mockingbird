package mock;

import mock.answers.FixedAnswer;
import mock.answers.RedefineAnswer;
import mock.answers.StaticAnswer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
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

    public RedefineMockClass applyMethod(RedefineAnswer redefineAnswer, String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(redefineAnswer, oldType.getMethod(methodName, parameters));
    }

    public RedefineMockClass applyMethod(RedefineAnswer redefineAnswer, Method method) {
        return (RedefineMockClass) applyMethod(TargetedMockBuilder.getRedefineAnswerImplementation(redefineAnswer), method);
    }

    public <T> RedefineMockClass applyMethod(T value, String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(value, oldType.getMethod(methodName, parameters));
    }

    public <T> RedefineMockClass applyMethod(T value, Method method) {
        return applyMethod(FixedAnswer.newInstance(value), method);
    }

    public RedefineMockClass applyStaticMethod(StaticAnswer answer, String methodName, Class<?>... params) throws NoSuchMethodException {
        return applyStaticMethod(answer, oldType.getMethod(methodName, params));
    }

    public RedefineMockClass applyStaticMethod(StaticAnswer answer, Method method) {
        StaticAnswer.StaticDelegator.addDelegator(method, answer);
        if (!staticImplApplied) {
            staticImplApplied = true;
            return (RedefineMockClass) applyMethod(MethodDelegation.withDefaultConfiguration().filter(StaticAnswer.MATCHER).to(StaticAnswer.StaticDelegator.class), method);
        }
        return this;
    }

    public <V> RedefineMockClass applyStaticMethod(V value, String methodName, Class<?>... params) throws NoSuchMethodException {
        return applyStaticMethod(FixedAnswer.newInstance(value), methodName, params);
    }

    public <V> RedefineMockClass applyStaticMethod(V value, Method method) {
        return applyStaticMethod(FixedAnswer.newInstance(value), method);
    }

}
