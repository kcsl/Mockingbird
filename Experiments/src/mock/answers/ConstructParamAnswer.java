package mock.answers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @author Derrick Lockwood
 * @created 9/5/18.
 */
public class ConstructParamAnswer implements ReturnTypeAnswer {

    private final Class<?>[] constructorParams;
    private final Answer[] constructorAnswers;

    public ConstructParamAnswer(Class<?>[] constructorParams, Answer[] constructorAnswers) {
        if (!(constructorParams == null || constructorParams.length == 0) && (constructorAnswers == null || constructorAnswers.length < constructorParams.length)) {
            throw new RuntimeException("Invalid number of answers for object instantiator");
        }
        this.constructorParams = constructorParams;
        this.constructorAnswers = constructorAnswers;
    }

    @Override
    public Object applyReturnType(Class<?> returnType, boolean forceReload) {
        try {
            Constructor<?> c = returnType.getConstructor(constructorParams);
            c.setAccessible(true);
            Object[] args = null;
            if (constructorParams != null) {
                args = new Object[constructorParams.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = constructorAnswers[i].handle(null, null, null, constructorParams[i]);
                }
            }
            return c.newInstance(args);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Answer duplicate() {
        return new ConstructParamAnswer(constructorParams, constructorAnswers);
    }
}
