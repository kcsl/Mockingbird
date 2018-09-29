package mock;

import mock.answers.Answer;
import mock.answers.ConstructParamAnswer;
import mock.answers.ReturnTypeAnswer;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Implementation;

/**
 * @author Derrick Lockwood
 * @created 5/30/18.
 */
public class SubMockClass extends MockClass {

    SubMockClass(TargetedMockBuilder targetedMockBuilder, Class<?> oldType, DynamicType.Builder<?> builder, ConstructParamAnswer returnTypeAnswer) {
        super(targetedMockBuilder, oldType, builder, returnTypeAnswer);
    }

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
        return TargetedMockBuilder.getImplementation(answer, Answer.SUB_MATCHER);
    }

    public void createField(String name, Class<?> type, int modifiers) {
        this.builder = builder.defineField(name, type, modifiers);
    }

}
