package mock;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Derrick Lockwood
 * @created 5/24/18.
 */
public class FieldSetInterceptor {
    private final String interceptorName;
    private Map<String, Pair<Field, ObjectInstantiator<?>>> fields;

    FieldSetInterceptor(String className) {
        fields = new HashMap<>();
        this.interceptorName = "initFields" + className + randomMethodSuffix();
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

    public void putField(Field field, ObjectInstantiator<?> objectInstantiator) {
        field.setAccessible(true);
        fields.put(field.getName(), new Pair<>(field, objectInstantiator));
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public void reloadParameters(Object object) throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        reloadParameters(object, null);
    }

    public void reloadParameters(Object object, String[] parameters) throws
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        if (!isEmpty()) {
            Class<?> type = object.getClass();
            Method method = type.getDeclaredMethod(interceptorName, String[].class);
            method.invoke(object, (Object) parameters);
        }
    }

    DynamicType.Builder<?> addFields(DynamicType.Builder<?> builder) {
        if (!isEmpty()) {
            builder = builder.defineMethod(interceptorName, Void.TYPE, Modifier.PUBLIC)
                    .withParameters(String[].class)
                    .intercept(MethodDelegation.withDefaultConfiguration().
                            filter(ElementMatchers.named("setup")).
                            to(this));
        }
        return builder;
    }

    @RuntimeType
    public void intercept(@This Object o, @AllArguments Object[] args) throws Exception {
        String[] fieldsToReload = (String[]) args[0];
        if (fieldsToReload != null) {
            for (String field : fieldsToReload) {
                Pair<Field, ObjectInstantiator<?>> pair = fields.get(field);
                pair.getKey().set(o, pair.getValue().newInstance());
            }
        } else {
            for (Map.Entry<String, Pair<Field, ObjectInstantiator<?>>> entry : fields.entrySet()) {
                entry.getValue().getKey().set(o, entry.getValue().getValue().newInstance());
            }
        }
    }

    private static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
