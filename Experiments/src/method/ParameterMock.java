package method;

import mock.SubMockClass;
import mock.TargetedMockBuilder;
import mock.answers.SubAnswer;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Derrick Lockwood
 * @created 6/3/18.
 */
public class ParameterMock {

    private SubMockClass mockClass;

    public ParameterMock(TargetedMockBuilder builder, Class<?> type) {
        mockClass = builder.createSubclass(type);
    }

    public void addMethodOverwrite(SubAnswer answer, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        mockClass.applyMethod(answer, methodName, parameterTypes);
    }

    Object create() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return mockClass.create();
    }
}
