package mock;

import mock.answers.*;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Derrick Lockwood
 * @created 5/24/18.
 */
public abstract class MockClass implements MockCreator {

    final Class<?> oldType;
    private final TargetedMockBuilderDEL targetedMockBuilderDEL;
    DynamicType.Builder<?> builder;
    private Class<?> newType;
    private FieldSetInterceptor fieldSetInterceptor;
    private ObjectInstantiator<?> objectInstantiator;
    private String name;
    private Map<Method, Answer> methodMap;
    private final ConstructParamAnswer constructorAnswer;
    private boolean construct;

    MockClass(
            TargetedMockBuilderDEL targetedMockBuilderDEL,
            Class<?> oldType,
            DynamicType.Builder<?> builder, ConstructParamAnswer constructorAnswer) {
        this.oldType = oldType;
        this.targetedMockBuilderDEL = targetedMockBuilderDEL;
        fieldSetInterceptor = new FieldSetInterceptor(oldType.getSimpleName());
        //TODO: handle toString better?
        this.builder = builder.method(ElementMatchers.named("toString").and(ElementMatchers.takesArguments(0)))
                .intercept(getImplementation(new FixedAnswer(oldType.toString())));
        this.newType = null;
        methodMap = new HashMap<>();
        name = null;
        this.constructorAnswer = constructorAnswer;
        this.construct = constructorAnswer != null;
    }

    MockClass(
            TargetedMockBuilderDEL targetedMockBuilderDEL,
            Class<?> oldType,
            DynamicType.Builder<?> builder) {
        this(targetedMockBuilderDEL, oldType, builder, null);

    }

    public void setDefaultImplementation(Implementation implementation) {
        builder = builder.method(ElementMatchers.any())
                .intercept(implementation);
    }

    public void setConstruct(boolean construct) {
        this.construct = construct;
    }

    public void setToConstruct() {
        this.construct = true;
    }

    public Class<?> getOldType() {
        return oldType;
    }

    public Class<?> getNewType() {
        return newType;
    }

    @Override
    public Class<?> getType() {
        return newType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Answer getAnswer(String methodName, Class<?>[] parameters) throws NoSuchMethodException {
        return getAnswer(oldType.getMethod(methodName, parameters));
    }

    public Answer getAnswer(Method method) {
        return methodMap.get(method);
    }

    public <T> MockClass applyField(String fieldName, T value) throws NoSuchFieldException {
        return applyField(oldType.getDeclaredField(fieldName), value);
    }

    public <T> MockClass applyField(Field field, T value) {
        //TODO: name objectinstantiator field?
        fieldSetInterceptor.putField(field, targetedMockBuilderDEL.createObjectInstantiator(null, value));
        return this;
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, String fieldName) throws
            NoSuchFieldException {
        return applyField(tClass, objectInstantiator, oldType.getDeclaredField(fieldName));
    }

    public <T> MockClass applyField(Class<T> tClass, ObjectInstantiator<T> objectInstantiator, Field field) {
        targetedMockBuilderDEL.setObjectInstantiator(tClass, objectInstantiator);
        //TODO: Do this? what happens if this is true?
        targetedMockBuilderDEL.setNamedInstance(field.getName(), tClass);
        fieldSetInterceptor.putField(field, objectInstantiator);
        return this;
    }

    public MockClass applyField(String fieldName, ObjectInstantiator<?> objectInstantiator) throws
            NoSuchFieldException {
        return applyField(oldType.getDeclaredField(fieldName), objectInstantiator);
    }

    public MockClass applyField(String fieldName, Answer answerCreator) throws NoSuchFieldException {
        Field field = oldType.getDeclaredField(fieldName);
        return applyField(field, new AnswerInstantiator(answerCreator, field, field.getType()));
    }

    public MockClass applyField(Field field, ObjectInstantiator<?> objectInstantiator) {
        fieldSetInterceptor.putField(field, objectInstantiator);
        return this;
    }

    public MockClass applyMethod(Implementation implementation, Method method) {
        method.setAccessible(true);
//        builder.visit(new AsmVisitorWrapper.ForDeclaredMethods().method(ElementMatchers.is(method), new MemberSubstitution()));
        builder = builder.method(ElementMatchers.is(method))
                .intercept(implementation);
        return this;
    }

    public MockClass applyMethod(String methodName, Class<?>... parameters) throws NoSuchMethodException {
        return applyMethod(new EmptyAnswer(), methodName, parameters);
    }

    public <T> MockClass applyMethod(T value, String methodName, Class<?>... parameters) throws
            NoSuchMethodException {
        return applyMethod(value, oldType.getMethod(methodName, parameters));
    }

    public <T> MockClass applyMethod(T value, Method method) {
        return applyMethod(new FixedAnswer(value), method);
    }

    public MockClass applyMethod(Answer answer, String methodName, Class<?>... parameters) throws
            NoSuchMethodException {
        return applyMethod(answer, oldType.getMethod(methodName, parameters));
    }

    public MockClass applyMethod(Answer answer, Method method) {
        methodMap.put(method, answer);
        return applyMethod(getImplementation(answer), method);
    }

    public void reloadInstanceVariables(Object object, List<String> instanceVariables) {
        if (!object.getClass().equals(newType)) {
            return;
        }
        try {
            fieldSetInterceptor.reloadParameters(object, instanceVariables.toArray(new String[0]));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Object create() throws
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        Object newObject;
        if (newType != null) {
            newObject = objectInstantiator.newInstance();
        } else {
            if (construct) {
                newObject = makeInstantiator(constructorAnswer).newInstance();
            } else {
                newObject = makeInstantiator().newInstance();
            }
        }
        fieldSetInterceptor.reloadParameters(newObject);
        return newObject;
    }

    public void store() {
        if (newType != null) {
            return;
        }
        if (construct) {
            makeInstantiator(constructorAnswer);
        } else {
            makeInstantiator();
        }
    }

    private ObjectInstantiator makeInstantiator(ConstructParamAnswer constructorAnswer) {
        builder = fieldSetInterceptor.addFields(builder);
        newType = createClass(builder.make());
        builder = null;
        objectInstantiator = targetedMockBuilderDEL.createObjectInstantiator(name, newType, constructorAnswer);
        return objectInstantiator;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    private ObjectInstantiator makeInstantiator() {
        builder = fieldSetInterceptor.addFields(builder);
        newType = createClass(builder.make());
        builder = null;
        objectInstantiator = targetedMockBuilderDEL.createObjectInstantiator(name, newType);
        return objectInstantiator;
    }

    @Override
    public Object newInstance() {
        try {
            return create();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    abstract Class<?> createClass(DynamicType.Unloaded<?> unloaded);

    abstract Implementation getImplementation(Answer answer);

}
