package mock;

import mock.answers.EmptyAnswer;
import mock.answers.FixedAnswer;
import mock.answers.SubAnswer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.lang.reflect.Method;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class SubMockClass extends MockClass {
    SubMockClass(TargetedMockBuilder targetedMockBuilder, Class<?> oldType, DynamicType.Builder<?> builder, InstanceCreatorHolder parentHolder) {
        super(targetedMockBuilder, oldType, builder, parentHolder);
    }

    public SubMockClass applyMethod(String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(new EmptyAnswer(), methodName, parameters);
    }

    public <T> SubMockClass applyMethod(T value, String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(value, oldType.getMethod(methodName, parameters));
    }

    public <T> SubMockClass applyMethod(T value, Method method) {
        return applyMethod(FixedAnswer.newInstance(value), method);
    }

    public SubMockClass applyMethod(SubAnswer subAnswer, String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(subAnswer, oldType.getMethod(methodName, parameters));
    }

    public SubMockClass applyMethod(SubAnswer subAnswer, Method method) {
        return (SubMockClass) applyMethod(TargetedMockBuilder.getSubAnswerImplementation(subAnswer), method);
    }

    @Override
    Class<?> createClass(DynamicType.Unloaded<?> unloaded) {
        return unloaded.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
    }
}
