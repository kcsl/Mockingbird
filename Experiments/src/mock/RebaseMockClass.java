package mock;

import mock.answers.Answer;
import mock.answers.ConstructParamAnswer;
import mock.answers.EmptyAnswer;
import mock.answers.FixedAnswer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 10/22/18.
 */
public class RebaseMockClass<T> {

    private final Class<T> oldType;
    private final TargetedMockBuilder targetedMockBuilder;
    private DynamicType.Builder<T> builder;
    private FieldSetInterceptor fieldSetInterceptor;
    private Map<Method, Answer> methodMap;
    private final ConstructParamAnswer constructorAnswer;

    RebaseMockClass(TargetedMockBuilder targetedMockBuilder, Class<T> oldType, DynamicType.Builder<T> builder,
            ConstructParamAnswer constructorAnswer) {
        this.targetedMockBuilder = targetedMockBuilder;
        this.oldType = oldType;
        this.builder = builder;
        this.constructorAnswer = constructorAnswer;
        methodMap = new HashMap<>();
    }

    public Class<T> getOldType() {
        return oldType;
    }

    public Answer getAnswer(String methodName, Class<?>[] parameters) throws NoSuchMethodException {
        return getAnswer(oldType.getMethod(methodName, parameters));
    }

    public Answer getAnswer(Method method) {
        return methodMap.get(method);
    }

//    public <V> MockClassDELDEL applyField(String fieldName, V value) throws NoSuchFieldException {
//        return applyField(oldType.getDeclaredField(fieldName), value);
//    }
//
//    public <V> MockClassDELDEL applyField(Field field, V value) {
//        //TODO: name objectinstantiator field?
//        fieldSetInterceptor.putField(field, targetedMockBuilder.createObjectInstantiator(null, value));
//        return this;
//    }
//
//    public <T> MockClassDELDEL applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, String fieldName) throws
//            NoSuchFieldException {
//        return applyField(tClass, objectInstantiator, oldType.getDeclaredField(fieldName));
//    }
//
//    public <T> MockClassDELDEL applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, Field field) {
//        targetedMockBuilder.setObjectInstantiator(tClass, objectInstantiator);
//        //TODO: Do this? what happens if this is true?
//        targetedMockBuilder.setNamedInstance(field.getName(), tClass);
//        fieldSetInterceptor.putField(field, objectInstantiator);
//        return this;
//    }
//
//    public MockClassDELDEL applyField(String fieldName, ObjectInstantiator<?> objectInstantiator) throws
//            NoSuchFieldException {
//        return applyField(oldType.getDeclaredField(fieldName), objectInstantiator);
//    }
//
//    public MockClassDELDEL applyField(String fieldName, Answer answerCreator) throws NoSuchFieldException {
//        Field field = oldType.getDeclaredField(fieldName);
//        return applyField(field, new AnswerInstantiator(answerCreator, field, field.getType()));
//    }
//
//    public MockClassDELDEL applyField(Field field, ObjectInstantiator<?> objectInstantiator) {
//        fieldSetInterceptor.putField(field, objectInstantiator);
//        return this;
//    }

    public RebaseMockClass<T> applyMethod(Implementation implementation,
            ElementMatcher<? super MethodDescription> matcher) {
        builder = builder.method(matcher)
                .intercept(implementation);
        return this;
    }

    public RebaseMockClass<T> setEmptyMethod(String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(FixedValue.nullValue(), methodName, parameters);
    }

    public <V> RebaseMockClass<T> applyMethod(V value, String methodName, Class<?>... parameters) throws
            NoSuchMethodException {
        return applyMethod(FixedValue.value(value), methodName, parameters);
    }

    public RebaseMockClass<T> applyMethod(Implementation implementation, String methodName, Class<?>... parameters) throws
            NoSuchMethodException {
        return applyMethod(implementation,
                ElementMatchers.named(methodName).and(ElementMatchers.takesArguments(parameters)));
    }

    public RebaseMockClass<T> applyMethod(Answer answer, String methodName, Class<?>... parameters) throws
            NoSuchMethodException {
        return applyMethod(TargetedMockBuilder.getImplementation(answer),
                ElementMatchers.named(methodName).and(ElementMatchers.takesArguments(parameters)));
    }

    public void save(File folder) throws IOException {
        save(oldType.getName(), folder);
    }

    public void save(String newName, File folder) throws IOException {
        builder.name(newName)
                .make()
                .saveIn(folder);
        targetedMockBuilder.addSaveLocation(folder);
        builder = null;
    }

}
