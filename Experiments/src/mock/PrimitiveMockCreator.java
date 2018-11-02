package mock;

import method.AttributeClass;
import mock.answers.Answer;
import mock.answers.MultiReturnTypeAnswer;
import mock.answers.ReturnTypeAnswer;

/**
 * A primitive mock creator used by the targeted method's parameters and instance variables
 *
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class PrimitiveMockCreator implements MockCreator {
    private final Class<?> type;
    private final TargetedMockBuilderDEL builder;
    private ReturnTypeAnswer answer;
    private String name;

    private PrimitiveMockCreator(TargetedMockBuilderDEL builder, Class<?> type, ReturnTypeAnswer answer) {
        this.type = type;
        this.builder = builder;
        builder.setObjectInstantiator(type, this);
        this.answer = answer;
        name = null;
    }

    public static PrimitiveMockCreator create(TargetedMockBuilderDEL builder, Class<?> type, Answer answer) {
        if (!(answer instanceof ReturnTypeAnswer)) {
            return null;
        }
        if (type.isAssignableFrom(int.class) ||
                type.isAssignableFrom(long.class) ||
                type.isAssignableFrom(float.class) ||
                type.isAssignableFrom(double.class) ||
                type.isAssignableFrom(boolean.class) ||
                type.isAssignableFrom(char.class) ||
                type.isAssignableFrom(byte.class) ||
                type.isAssignableFrom(String.class)
                ) {
            return new PrimitiveMockCreator(builder, type, (ReturnTypeAnswer) answer);
        }
        return null;
    }

    public static PrimitiveMockCreator create(TargetedMockBuilderDEL builder, AttributeClass attributeClass,
            Answer answer) {
        if (!(answer instanceof ReturnTypeAnswer)) {
            return null;
        }
        Class<?> type = attributeClass.getMockClass();
        if (attributeClass.getAttribute(AttributeClass.IS_PRIMITIVE)) {
            return new PrimitiveMockCreator(builder, type,
                    new MultiReturnTypeAnswer(attributeClass, (ReturnTypeAnswer) answer));
        }
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        builder.setNamedInstance(name, type);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Object newInstance() {
        try {
            if (answer == null) {
                throw new RuntimeException("Primitive not stubbed and no value is set " + type.getName());
            }
            return answer.applyReturnType(type, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
