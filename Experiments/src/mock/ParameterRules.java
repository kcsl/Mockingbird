package mock;

import mock.answers.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 5/16/18.
 */
public class ParameterRules {

    private List<Rule> rules;
    private Class<?> type;
    private Object value;

    public ParameterRules(Object value) {
        this.rules = null;
        this.value = value;
        this.type = value.getClass();
    }

    public ParameterRules(Class<?> type) {
        this.rules = new ArrayList<>();
        this.value = null;
        this.type = type;
    }

    public void addRule(Rule rule) {
        if (!isValueSet() && rule.getClassType().equals(type)) {
            rules.add(rule);
        }
    }

    public void addRule(Answer<?> answer, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        if (!isValueSet()) {
            rules.add(new Rule(answer, type, methodName, paramTypes));
        }
    }

    public void addRule(Answer<?> answer, Method method) {
        if (!isValueSet()) {
            rules.add(new Rule(answer, method));
        }
    }

    public void applyRules(TargetedMockBuilder targetedMockBuilder) throws InvocationTargetException, IllegalAccessException {
        if (!isValueSet()) {
            for (Rule rule : rules) {
                targetedMockBuilder.apply(rule);
            }
        }
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isValueSet() {
        return this.value != null;
    }
}
