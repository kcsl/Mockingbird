package method;

import method.callbacks.EmptyMethodCallback;
import method.callbacks.MethodCallback;
import mock.MockCreator;
import mock.PrimitiveMockCreator;
import mock.SubMockClass;
import mock.TargetedMockBuilderDEL;
import mock.answers.Answer;
import mock.answers.ConstructParamAnswer;
import mock.answers.NotStubbedAnswer;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates an environment to call the method specified and captures an analysis on the method
 * called.
 *
 * @author Derrick Lockwood
 * @created 6/6/18.
 */
public class MethodCall {

    private final TargetedMockBuilderDEL builder;
    final MockCreator[] parameters;
    final List<String> primitiveInstanceVariables;
    final List<MockCreator> normalObjects;
    final SubMockClass methodMockClass;
    final Method method;
    private final AttributeClass[] parameterDefinitions;

    MethodCall(AttributeClass[] parameterDefinitions, Method method) {
        this(parameterDefinitions, method, null);
    }

    MethodCall(AttributeClass[] parameterDefinitions, Method method, ConstructParamAnswer constructorAnswer) {
        this.method = method;
        this.parameterDefinitions = parameterDefinitions;
        builder = new TargetedMockBuilderDEL();
        methodMockClass = builder.createSubclassRealMethods(method.getDeclaringClass(), constructorAnswer);
        parameters = new MockCreator[method.getParameterCount()];
        primitiveInstanceVariables = new ArrayList<>();
        normalObjects = new ArrayList<>();
    }

    SubMockClass getMethodMockClass() {
        return methodMockClass;
    }

    <T> ObjectInstantiator<T> getObjectInstantiatorByName(String name) {
        return builder.getInstantiator(name);
    }

    public void overrideMethodCall(Answer answer) {
        this.overrideMethod(answer, method);
    }

    public void overrideMethod(Answer answer, Method method) {
        methodMockClass.applyMethod(answer, method);
    }

    /**
     * Creates a parameter SubMockClass by which the parameter is instantiated
     *
     * @param index         the index of the parameter of the method
     * @param defaultAnswer the default answer by which any method that is called applyReturnType {@code parameters[index].type} is run.
     *                      This is also the answer that is supplied to the primitive variables when {@code .store()}
     *                      is run on them which just returns the value of the primitive variable.
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     */
    public SubMockClass createParameterMock(int index, Answer defaultAnswer) {
        if (index < 0 || index >= parameters.length) {
            //TODO: Soft ignore?
            return null;
        }
        MockCreator parameterMock = createMockCreator(parameterDefinitions[index], defaultAnswer);
        parameters[index] = parameterMock;
        return parameterMock instanceof SubMockClass ? (SubMockClass) parameterMock : null;
    }

    /**
     * Creates a parameter SubMockClass by which the parameter is instantiated but the default answer is null and thus
     * set to an error if method is not mocked when run.
     *
     * @param index the index of the parameter of the method
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     */
    public SubMockClass createParameterMock(int index) {
        return createParameterMock(index, NotStubbedAnswer.newInstance());
    }

    public AttributeClass getParameterAttributeClass(int index) {
        if (index < 0 || index >= parameters.length) {
            //TODO: Soft ignore?
            return null;
        }
        return parameterDefinitions[index];
    }

    private MockCreator createMockCreator(AttributeClass attributeClass, Answer answer) {
        MockCreator mockCreator;
        if (attributeClass.getMockClass().isPrimitive() || attributeClass.getMockClass().isAssignableFrom(
                String.class)) {
            if (attributeClass.getAttribute(AttributeClass.IS_ARRAY, false) || attributeClass.getAttribute(
                    AttributeClass.IS_LIST, false)) {
                mockCreator = PrimitiveMockCreator.create(builder, attributeClass, answer);
            } else {
                mockCreator = PrimitiveMockCreator.create(builder, attributeClass.getMockClass(), answer);
            }
        } else if (attributeClass.getAttribute(AttributeClass.IS_ARRAY, false) || attributeClass.getAttribute(
                AttributeClass.IS_LIST, false)) {
            mockCreator = builder.createMultipleMockClass(attributeClass);
        } else {
            mockCreator = builder.createSubclass(attributeClass.getMockClass());
        }
        return mockCreator;
    }

    /**
     * Creates an instance variable mock similar to {@link method.MethodCall#createParameterMock(int, Answer) createParameterMock}
     * but creates it for the instance variable used applyReturnType the method to mock and supplied to the declaring object.
     *
     * @param fieldVariableName instance variable name to mock
     * @param defaultAnswer     the default answer by which any method that is called applyReturnType {@code parameters[index].type} is run.
     *                          This is also the answer that is supplied to the primitive variables when {@code .store()}
     *                          is run on them which just returns the value of the primitive variable.
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     * @throws NoSuchFieldException if the field is not found
     */
    public SubMockClass createFieldMock(String fieldVariableName, Answer defaultAnswer) throws
            NoSuchFieldException, ClassNotFoundException {
        Class<?> instanceVariableClass = method.getDeclaringClass().getDeclaredField(fieldVariableName).getType();
        MockCreator instanceMock = createMockCreator(
                AttributeClass.createAttributeClass(instanceVariableClass.getName()), defaultAnswer);
        if (instanceMock.isPrimitive()) {
            primitiveInstanceVariables.add(fieldVariableName);
        }
        methodMockClass.applyField(fieldVariableName, instanceMock);
        return instanceMock instanceof SubMockClass ? (SubMockClass) instanceMock : null;
    }

    /**
     * Creates an instance variable mock but the default answer is not stubbed.
     *
     * @param fieldName instance variable name to mock
     * @return A {@link mock.SubMockClass SubMockClass} if the class of the parameter is not a primitive, else {@code null}.
     * @throws NoSuchFieldException if the field is not found
     */
    public SubMockClass createFieldMock(String fieldName) throws
            NoSuchFieldException,
            ClassNotFoundException {
        return createFieldMock(fieldName, null);
    }

    public void addParameterInstantiator(int index, MockCreator mockCreator) {
        parameters[index] = mockCreator;
    }

    public void addFieldInstantiator(String fieldName, Answer answer) throws
            NoSuchFieldException {
        methodMockClass.applyField(fieldName, answer);
    }

    public MockCreator createStoredMock(AttributeClass attributeClass) {
        return createStoredMock(attributeClass, null);
    }

    public MockCreator createStoredMock(AttributeClass attributeClass, Answer defaultAnswer) {
        MockCreator multipleMockCreator = createMockCreator(attributeClass, defaultAnswer);
        normalObjects.add(multipleMockCreator);
        return multipleMockCreator;
    }

    public MethodCallSession createSession() {
        return createSession(false);
    }

    public MethodCallSession createSession(boolean resetEveryTime) {
        return createSession(EmptyMethodCallback.create(), resetEveryTime);
    }

    public MethodCallSession createSession(MethodCallback methodCallback) {
        return createSession(methodCallback, false);
    }

    public MethodCallSession createSession(MethodCallback methodCallback, boolean resetEveryTime) {
        return new MethodCallSession(this, methodCallback, resetEveryTime);
    }

    @Override
    public String toString() {
        return method.getDeclaringClass().getName() + " : " + method.getName() + " | " + Arrays.toString(
                method.getParameterTypes());
    }

}
