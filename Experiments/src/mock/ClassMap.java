package mock;

import javafx.util.Pair;
import mock.answers.Answer;
import mock.matchers.MethodMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Derrick Lockwood
 * @created 1/14/19.
 */
public class ClassMap {

    private final Map<String, StoredMock> fieldMap;
    private final Map<ElementMatcher<? super MethodDescription>, Pair<Boolean, Answer>> methodMap;
    private TransformMockClass transformMockClass;
    private Answer constructAnswer;
    private boolean loadEveryInstantiation;
    private String name;
    final boolean useOriginal;


    public ClassMap(boolean useOriginal) {
        fieldMap = new HashMap<>();
        methodMap = new HashMap<>();
        this.useOriginal = useOriginal;
        transformMockClass = null;
        constructAnswer = null;
        loadEveryInstantiation = true;
        this.name = "";
    }

    public ClassMap() {
        this(true);
    }

    public void associateWithMockClass(TransformMockClass transformMockClass) {
        this.transformMockClass = transformMockClass;
        for (Map.Entry<ElementMatcher<? super MethodDescription>, Pair<Boolean, Answer>> entry : methodMap.entrySet()) {
            transformMockClass.associateMethodInterceptor(entry.getKey(), entry.getValue().getValue());
        }
    }

    public void setLoadEveryInstantiation(boolean loadEveryInstantiation) {
        this.loadEveryInstantiation = loadEveryInstantiation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean getLoadEveryInstantiation() {
        return this.loadEveryInstantiation;
    }

    public TransformMockClass getAssociatedMockClass() {
        return transformMockClass;
    }

    public void setConstructAnswer(Answer constructAnswer) {
        this.constructAnswer = constructAnswer;
    }

    public Answer getConstructAnswer() {
        return constructAnswer;
    }

    public boolean isAssociated(TransformMockClass transformMockClass) {
        return isAssociated(transformMockClass.getCanonicalName());
    }

    public boolean isAssociated(String canonicalName) {
        if (this.transformMockClass == null) {
            return false;
        }
        return this.transformMockClass.getCanonicalName().equals(canonicalName);
    }

    public Set<Map.Entry<String, StoredMock>> getFieldEntries() {
        return fieldMap.entrySet();
    }

    public void applyDescribedMethod(Answer answer, boolean duplicate, ElementMatcher<? super MethodDescription> methodMatcher) {
        methodMap.put(methodMatcher, new Pair<>(duplicate, answer));
        if (this.transformMockClass != null) {
            transformMockClass.associateMethodInterceptor(methodMatcher, answer);
        }
    }

    public void applyMethod(Answer answer, boolean duplicate, String methodName, String... parameterClasses) {
        applyDescribedMethod(answer, duplicate, MethodMatchers.getMethodMatcher(methodName, parameterClasses));
    }

    public void applyMethod(Answer answer,  String methodName, String... parameterClasses) {
        applyMethod(answer, false, methodName, parameterClasses);
    }

    public void applyField(String fieldName, StoredMock storedMock) {
        fieldMap.put(fieldName, storedMock);
    }

    public void overrideConstructor(String[] constructorParamsTypes) {
        //TODO
    }

    public static ClassMap forConstructAnswer(Answer answer) {
        ClassMap a = new ClassMap();
        a.setConstructAnswer(answer);
        return a;
    }

}
