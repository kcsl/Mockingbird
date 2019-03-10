package mock;

import javafx.util.Pair;
import mock.answers.Answer;
import mock.answers.AnswerCreator;
import mock.answers.FixedAnswer;
import mock.matchers.MethodMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Derrick Lockwood
 * @created 1/14/19.
 */
public class ClassMap {

    private final Map<String, Answer> fieldMap;
    private final Map<ElementMatcher<? super MethodDescription>, Pair<Boolean, Answer>> methodMap;
    private TransformMockClass transformMockClass;
    private final Map<String, Pair<Boolean, Answer>> mockClassMap;
    private Answer constructAnswer;
    private boolean loadEveryInstantiation;
    private String name;
    final boolean useOriginal;


    public ClassMap(boolean useOriginal) {
        fieldMap = new HashMap<>();
        methodMap = new HashMap<>();
        mockClassMap = new HashMap<>();
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
        if (this.transformMockClass != null) {
            mockClassMap.clear();
        }
        this.transformMockClass = transformMockClass;
        for (Map.Entry<ElementMatcher<? super MethodDescription>, Pair<Boolean, Answer>> entry : methodMap.entrySet()) {
            mockClassMap.put(transformMockClass.getMethodFieldName(entry.getKey()), entry.getValue());
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

    public Pair<Boolean, Answer> getAnswer(String methodFieldName) {
        return mockClassMap.get(methodFieldName);
    }

    public Set<Map.Entry<String, Answer>> getFieldEntries() {
        return fieldMap.entrySet();
    }

    public void applyDescribedMethod(Answer answer, boolean duplicate, ElementMatcher<? super MethodDescription> methodMatcher) {
        methodMap.put(methodMatcher, new Pair<>(duplicate, answer));
        if (this.transformMockClass != null) {
            mockClassMap.put(transformMockClass.getMethodFieldName(methodMatcher), new Pair<>(duplicate, answer));
        }
    }

    public void applyMethod(Answer answer, boolean duplicate, String methodName, String... parameterClasses) {
        applyDescribedMethod(answer, duplicate, MethodMatchers.getMethodMatcher(methodName, parameterClasses));
    }

    public void applyMethod(Answer answer,  String methodName, String... parameterClasses) {
        applyMethod(answer, true, methodName, parameterClasses);
    }

    public void applyField(String fieldName, Object value) {
        fieldMap.put(fieldName, new FixedAnswer(value));
    }

    public void applyField(String fieldName, Answer answerCreator) {
        fieldMap.put(fieldName, answerCreator);
    }

    public void overrideConstructor(String[] constructorParamsTypes) {

    }

}
