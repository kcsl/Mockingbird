package mock;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Derrick Lockwood
 * @created 5/24/18.
 */
public class FieldSetInterceptor {
    private final String interceptorName;
    private Map<Field, Class<?>> fields;
    private InstanceCreatorHolder instanceCreatorHolder;

    FieldSetInterceptor(InstanceCreatorHolder instanceCreatorHolder, String className) {
        fields = new HashMap<>();
        this.instanceCreatorHolder = instanceCreatorHolder;
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

    public String getInterceptorName() {
        return interceptorName;
    }

    public void putField(Field field, Class<?> value) {
        fields.put(field, value);
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @RuntimeType
    public void intercept(@This Object o) throws Exception {
        for (Map.Entry<Field, Class<?>> entry : fields.entrySet()) {
            entry.getKey().setAccessible(true);
            entry.getKey().set(o, instanceCreatorHolder.getInstance(entry.getValue()));
        }
    }
}
