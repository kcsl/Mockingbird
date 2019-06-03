package mock;

import javafx.util.Pair;
import mock.answers.Answer;
import mock.answers.NotStubbedAnswer;
import mock.answers.OriginalMethodAnswer;
import mock.matchers.MethodMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ArrayTypeMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Derrick Lockwood
 * @created 1/27/19.
 */
public class ClassInterceptor implements TransformClassLoader.Transformer {
    private final Set<String> methodFieldNames;
    private final String interceptorName;
    private final String canonicalName;

    protected ClassInterceptor(TransformMockClass transformMockClass) {
        methodFieldNames = new HashSet<>();
        canonicalName = transformMockClass.getCanonicalName();
        this.interceptorName = "initFields" + canonicalName.substring(canonicalName.lastIndexOf('.')+1) + randomMethodSuffix();
    }

    protected void initMethod(String fieldName) {
        methodFieldNames.add(fieldName);
    }

    private static String randomMethodSuffix() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stringBuilder.append(randomCharLettering(random));
        }
        return stringBuilder.toString();
    }

    private static char randomCharLettering(Random random) {
        int i = random.nextInt(51);
        if (i <= 25) {
            return (char) (65 + i);
        }
        return (char) (97 + i - 26);
    }

    public void applyMap(Object o, ClassMap classMap) throws
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        if (o == null || classMap == null) {
            return;
        }
        Class<?> clazz = o.getClass();
        Method m = clazz.getMethod(interceptorName, ClassMap.class);
        m.invoke(o, classMap);
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder) {
        return builder.defineMethod(interceptorName, Void.TYPE, Modifier.PUBLIC)
                .withParameters(ClassMap.class)
                .intercept(MethodDelegation.withDefaultConfiguration().
                        filter(ElementMatchers.named("intercept")).
                        to(this));
    }

    public ElementMatcher<? super MethodDescription> getInterceptorMatcher() {
        return MethodMatchers.getMethodMatcher(interceptorName, ClassMap.class);
    }

    @RuntimeType
    public void intercept(@Origin Method method, @This(optional = true) Object o, @AllArguments Object[] args) throws Exception {
        if (args == null || args.length < 1 || args[0] == null || !(args[0] instanceof ClassMap)) {
            throw new IllegalArgumentException("Didn't get right argument to class interceptor");
        }
        Class<?> c = method.getDeclaringClass();
        ClassMap classMap = (ClassMap) args[0];
        if (!classMap.isAssociated(canonicalName)) {
            throw new IllegalArgumentException("Class Map not associated with Mock Class");
        }
        for (Map.Entry<String, StoredMock> entry : classMap.getFieldEntries()) {
            //Todo: Soft pass or crash on NoSuchFieldException?
            Field f = c.getDeclaredField(entry.getKey());
            f.setAccessible(true);
            if (o != null) {
                f.set(o, entry.getValue().getObjectInstantiator().newInstance());
            } else {
                f.set(null, entry.getValue().getObjectInstantiator().newInstance());
            }
        }

//        for (String fieldName : methodFieldNames) {
//            Field f = c.getField(fieldName);
//            Pair<Boolean, Answer> a;
//            if ((a = classMap.getAnswer(fieldName)) != null) {
//                if (a.getKey()) {
//                    f.set(o, a.getValue().duplicate());
//                } else {
//                    f.set(o, a.getValue());
//                }
//
//            } else {
//                //TODO: Set it every time?
//                if (classMap.useOriginal) {
//                    f.set(o, OriginalMethodAnswer.newInstance());
//                } else {
//                    f.set(o, NotStubbedAnswer.newInstance());
//                }
//            }
//        }
    }
}
