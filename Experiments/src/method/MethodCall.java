package method;

import javafx.util.Pair;
import method.callbacks.MethodCallback;
import mock.*;
import mock.answers.Answer;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Derrick Lockwood
 * @created 11/5/18.
 */
public class MethodCall {

    private final TransformClassLoader transformClassLoader;
    private final ClassMap methodCallMap;
    private final TransformMockClass methodClass;
    private final String methodName;

    private final Map<String, ClassMap> nameMap;
    private final TransformMockClass[] parameterCreators;
    private final ClassMap[] parameterClassMaps;
    private final Map<String, StoredMock> storedClassMapping;

    //TODO: Make MethodDescription as input?
    public MethodCall(TransformClassLoader transformClassLoader, String canonicalName, String methodName,
            String... parameterTypeNames) {
        methodClass = transformClassLoader.getTransformMockClassOrCreate(canonicalName);
        if (methodClass.isPrimitive()) {
            throw new RuntimeException("Method Call Cannot be primitive");
        }
        nameMap = new HashMap<>();
        storedClassMapping = new HashMap<>();
        methodCallMap = new ClassMap(true);
        methodCallMap.associateWithMockClass(methodClass);
        this.transformClassLoader = transformClassLoader;
        this.methodName = methodName;
        parameterCreators = new TransformMockClass[parameterTypeNames.length];
        for (int i = 0 ; i < parameterTypeNames.length; i++) {
            parameterCreators[i] = transformClassLoader.getTransformMockClassOrCreate(parameterTypeNames[i]);
        }
        parameterClassMaps = new ClassMap[parameterTypeNames.length];
    }

    public void associateClassMapToParameter(int index, ClassMap classMap) {
        if (index < 0 || index >= parameterClassMaps.length) {
            return;
        }
        parameterClassMaps[index] = classMap;
    }

    public void associateClassMapToParameter(int index, String classMapName) {
        if (index < 0 || index >= parameterClassMaps.length) {
            return;
        }
        parameterClassMaps[index] = nameMap.get(classMapName);
    }

    public ClassMap createClassMap(String name) {
        ClassMap classMap = new ClassMap();
        nameMap.put(name, classMap);
        return classMap;
    }

    public void associateClassMapToStoredMock(String canonicalName, ClassMap classMap) {
        if (storedClassMapping.containsKey(canonicalName)) {
            storedClassMapping.get(canonicalName).setClassMap(classMap);
        } else {
            storedClassMapping.put(canonicalName, new StoredMock(canonicalName, classMap));
        }
    }

    public void associateFieldVariable(String fieldName, String canonicalName, ClassMap classMap) {
        StoredMock mock;
        if (storedClassMapping.containsKey(canonicalName)) {
            mock = storedClassMapping.get(canonicalName);
        } else {
            mock = new StoredMock(canonicalName, classMap);
            storedClassMapping.put(canonicalName, mock);
        }
        methodCallMap.applyField(fieldName, mock);
    }

    public String getParameterTypeCanonicalName(int index) {
        if (index < 0 || index >= parameterCreators.length) {
            return null;
        }
        return parameterCreators[index].getCanonicalName();
    }

    public boolean parameterIsPrimitive(int index) {
        if (index < 0 || index >= parameterCreators.length) {
            return false;
        }
        return parameterCreators[index].isPrimitive();
    }

    public ClassMap getMethodClassMap() {
        return methodCallMap;
    }


    public MethodCallSession createSession(MethodCallback methodCallback) throws Exception {
        ResettableClassFileTransformer transformer = transformClassLoader.loadAgent(ByteBuddyAgent.install());
        ObjectInstantiator<?>[] parameterInstantiators = new ObjectInstantiator[parameterCreators.length];
        for (int i = 0; i < parameterCreators.length; i++) {
            parameterInstantiators[i] = parameterCreators[i].getObjectInstantiator(parameterClassMaps[i]);
        }
        ObjectInstantiator<?>[] storedMockInstantiators = new ObjectInstantiator[storedClassMapping.size()];
        Iterator<Map.Entry<String, StoredMock>> iterator = storedClassMapping.entrySet().iterator();
        for (int i = 0; i < storedClassMapping.size(); i++) {
            storedMockInstantiators[i] = iterator.next().getValue().getObjectInstantiator();
        }

        Class<?>[] paramClasses = new Class[parameterCreators.length];
        for (int i = 0; i < parameterCreators.length; i++) {
            paramClasses[i] = parameterCreators[i].loadClass();
        }
        Method methodToCall = methodClass.loadMethod(methodName, paramClasses);
        ObjectInstantiator<?> methodInstantiator = methodClass.getObjectInstantiator(methodCallMap);

        return new MethodCallSession(methodCallback, transformer, methodToCall,
                methodInstantiator, parameterInstantiators, storedMockInstantiators);
    }

    public void overrideMethod(Answer answer, String methodName, String... parameterCanonicalNames) {
        methodCallMap.applyMethod(answer, methodName, parameterCanonicalNames);
    }

    public void constructMethodClass(Answer constructAnswer) {
        methodCallMap.setConstructAnswer(constructAnswer);
    }

    @Override
    public String toString() {
        return methodClass.getCanonicalName() + " : " + methodName + " | " + Arrays.toString(
                parameterCreators);
    }

    public static class PrimitiveNeedsAnswerException extends RuntimeException {
        PrimitiveNeedsAnswerException(Class<?> primitiveClass) {
            super("Primitive " + primitiveClass.getCanonicalName() + " Needs Answer to be supplied ");
        }
    }
}
