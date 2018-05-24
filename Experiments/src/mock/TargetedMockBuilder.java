package mock;

import mock.answers.Answer;
import mock.answers.InvocationData;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 5/15/18.
 */
public class TargetedMockBuilder {

    private DynamicType.Unloaded<?> allUnloaded;
    private List<DynamicType.Unloaded<?>> createdObjects;
    private DynamicType.Builder<?> currentBuilder;
    private Class<?> currentType;
    private Implementation defaultImplementation;
    private ByteBuddy byteBuddy;
    private boolean isRedefine;

    public TargetedMockBuilder() {
        this(invocationData -> {
            throw new RuntimeException("Method not stubbed " + invocationData.getMethod().getName());
        });
    }

    /*
    Things User can do:
    Complete Redefine of Class (Needs Load Agent)
    Subclass the class instead and use that instance (For parameters)
     */
    public TargetedMockBuilder(Answer<?> defaultAnswer) {
        this(getAnswerImplementation(defaultAnswer));
    }

    public TargetedMockBuilder(Implementation defaultImplementation) {
        currentBuilder = null;
        this.defaultImplementation = defaultImplementation;
        byteBuddy = new ByteBuddy();
        isRedefine = false;
        createdObjects = new ArrayList<>();
    }

    private static Implementation getAnswerImplementation(Answer<?> answer) {
        return answer == null ? null : InvocationHandlerAdapter.of(((proxy, method, args) -> answer.handle(new InvocationData(proxy, method, args))));
    }

    public TargetedMockBuilder startSubclass(Class<?> type) {
        return startSubclass(type, (Implementation) null);
    }

    public TargetedMockBuilder startSubclass(Class<?> type, Answer<?> defaultAnswer) {
        return startSubclass(type, getAnswerImplementation(defaultAnswer));
    }

    public TargetedMockBuilder startSubclass(Class<?> type, Implementation defaultImplementation) {
        if (currentBuilder == null) {
            currentBuilder = byteBuddy.subclass(type, ConstructorStrategy.Default.NO_CONSTRUCTORS);
            checkDefault(type, defaultImplementation);
            isRedefine = false;
        }
        return this;
    }

    public TargetedMockBuilder startRedefine(Class<?> type) {
        return startRedefine(type, (Implementation) null);
    }

    public TargetedMockBuilder startRedefine(Class<?> type, Answer<?> defaultAnswer) {
        return startRedefine(type, getAnswerImplementation(defaultAnswer));
    }

    public TargetedMockBuilder startRedefine(Class<?> type, Implementation implementation) {
        if (currentBuilder == null) {
            currentBuilder = byteBuddy.redefine(type);
            checkDefault(type, implementation);
            isRedefine = true;
        }
        return this;
    }

    private void checkDefault(Class<?> type, Implementation defaultImplementation) {
        if (defaultImplementation != null) {
            currentBuilder = currentBuilder.method(ElementMatchers.any())
                    .intercept(defaultImplementation);
        } else if (this.defaultImplementation != null) {
            currentBuilder = currentBuilder.method(ElementMatchers.any())
                    .intercept(this.defaultImplementation);
        }
        currentType = type;
    }

    public TargetedMockBuilder apply(Rule rule) {
        return apply(rule.getAnswer(), rule.getMethod());
    }

    public TargetedMockBuilder apply(Answer<?> answer, String methodName, Class<?>... parameters) throws NoSuchMethodException {
        if (currentBuilder != null) {
            apply(answer, currentType.getMethod(methodName, parameters));
        }
        return this;
    }

    public TargetedMockBuilder apply(Answer<?> answer, Method method) {
        if (currentBuilder != null) {
            method.setAccessible(true);
            currentBuilder = currentBuilder.method(ElementMatchers.is(method))
                    .intercept(getAnswerImplementation(answer));
        }
        return this;
    }

    public TargetedMockBuilder storeSubclass() {
        if (currentBuilder != null && !isRedefine) {
            createdObjects.add(currentBuilder.make());
            currentType = null;
            currentBuilder = null;
        }
        return this;
    }

    public TargetedMockBuilder storeRedefine() {
        if (currentBuilder != null && isRedefine) {
            currentBuilder.make()
                    .load(this.getClass().getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
            currentType = null;
            currentBuilder = null;
        }
        return this;
    }

    public Object[] loadNoParameterProxies() {
        Object[] objects = new Object[createdObjects.size()];
        ClassLoader classLoader = this.getClass().getClassLoader();
        Objenesis objenesis = new ObjenesisStd();
        for (int i = 0; i < objects.length; i++) {
            objects[i] = objenesis.newInstance(createdObjects.get(i)
                    .load(classLoader, ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded());
        }
        return objects;
    }

}
