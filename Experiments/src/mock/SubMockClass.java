package mock;

import mock.answers.Answer;
import mock.answers.EmptyAnswer;
import mock.answers.FixedAnswer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Implementation;

import java.lang.reflect.Method;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class SubMockClass extends MockClass {
    SubMockClass(TargetedMockBuilder targetedMockBuilder, Class<?> oldType, DynamicType.Builder<?> builder) {
        super(targetedMockBuilder, oldType, builder);
    }

    @Override
    Class<?> createClass(DynamicType.Unloaded<?> unloaded) {
        return unloaded.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
    }

    @Override
    Implementation getImplementation(Answer answer) {
        return TargetedMockBuilder.getSubAnswerImplementation(answer);
    }

}
