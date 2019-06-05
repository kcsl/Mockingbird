package mock;

import mock.answers.Answer;
import mock.answers.ReturnTypeAnswer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Derrick Lockwood
 * @created 11/26/18.
 */
public class ConstructAnswer implements ReturnTypeAnswer {

    private Answer primitiveAnswer;
    private Set<String> constructorParamTypeSet;
    private String[] constructorParamTypes;
    private Answer[] constructorAnswers;
    private Constructor<?> constructor;

    public ConstructAnswer(Answer primitiveAnswer) {
        this.primitiveAnswer = primitiveAnswer;
    }

    public ConstructAnswer(String[] constructorParamTypes, Answer[] constructorAnswers) {
        this.constructorParamTypeSet =  new HashSet<>();
        this.constructorParamTypeSet.addAll(Set.of(constructorParamTypes));
        this.constructorParamTypes = constructorParamTypes;
        this.constructorAnswers = constructorAnswers;
        this.constructor = null;
    }

    private Constructor<?> getConstructor(Class<?> returnType) {
        if (this.constructor == null) {
            for (Constructor<?> constructor : returnType.getConstructors()) {
                Class<?>[] constructorTypes = constructor.getParameterTypes();
                boolean found = true;
                for (Class<?> c : constructorTypes) {
                    if (!constructorParamTypeSet.contains(c.getCanonicalName())) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    constructor.setAccessible(true);
                    this.constructor = constructor;
                    break;
                }
            }
        }
        return this.constructor;
    }

    @Override
    public Object applyReturnType(Class<?> returnType, boolean forceReload) {
        if (returnType.isPrimitive()) {
            return primitiveAnswer.handle(null, null, null, returnType);
        }
        Constructor<?> constructor = getConstructor(returnType);
        if (constructor != null) {
            Class<?>[] constructorTypes = constructor.getParameterTypes();
            Object[] args = null;
            if (constructorAnswers != null) {
                args = new Object[constructorAnswers.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = constructorAnswers[i].handle(null, null, null, constructorTypes[i]);
                }
            }
            try {
                return constructor.newInstance(args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public Answer duplicate() {
        if (primitiveAnswer != null) {
            return new ConstructAnswer(primitiveAnswer);
        }
        return new ConstructAnswer(constructorParamTypes, constructorAnswers);
    }
}
