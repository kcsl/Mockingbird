package mock;

import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 5/16/18.
 */
public class ParameterSet {

    private final Class<?>[] parameterTypes;
    private final List<ParameterRules> parameterRules;
    private int typeIndex;

    public ParameterSet(Class<?>... parameterTypes) {
        this.parameterTypes = parameterTypes;
        this.parameterRules = new ArrayList<>();
        typeIndex = -1;
    }

    public void addParameterRules(Object value) {
        if (typeIndex < parameterTypes.length - 1 && checkTypes(value.getClass(), parameterTypes[typeIndex + 1])) {
            typeIndex++;
            parameterRules.add(new ParameterRules(value));
        }
    }

    public void addParameterRules(ParameterRules parameterRules) {
        if (typeIndex < parameterTypes.length - 1 && checkTypes(parameterRules.getType(), parameterTypes[typeIndex + 1])) {
            typeIndex++;
            this.parameterRules.add(parameterRules);
        }
    }

    public ParameterBuilder createParameterBuilder() {
        if (typeIndex < parameterTypes.length - 1) {
            typeIndex++;
            return new ParameterBuilder(parameterRules, new ParameterRules(parameterTypes[typeIndex]));
        }
        return null;
    }


    public void setRule(int paramIndex, Answer<?> answer, String methodName, Class<?>[] paramTypes) throws NoSuchMethodException {
        if (paramIndex >= 0 && paramIndex < parameterRules.size()) {
            parameterRules.get(paramIndex).addRule(answer, methodName, paramTypes);
        }
    }

    public void setRule(int paramIndex, Answer<?> answer, Method method) {
        if (paramIndex >= 0 && paramIndex < parameterRules.size()) {
            parameterRules.get(paramIndex).addRule(answer, method);
        }
    }

    public void setRule(int paramIndex, Rule rule) {
        if (paramIndex >= 0 && paramIndex < parameterRules.size()) {
            parameterRules.get(paramIndex).addRule(rule);
        }
    }

    public ParameterRules[] getParameterRules() {
        return parameterRules.toArray(new ParameterRules[0]);
    }

    public ParameterRules[] getAndCheckParameterRules() {
        ParameterRules[] rules = getParameterRules();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i >= rules.length) {
                /*
                TODO: Default ParameterRules for parameters?
                 */
                throw new RuntimeException("Not enough parameters supplied");
            }
            if (!checkTypes(rules[i].getType(), parameterTypes[i])) {
                throw new RuntimeException("Invalid types: " + rules[i].getType().toString() + " - " + parameterTypes[i].toString());
            }
        }
        return rules;
    }

    private static boolean checkTypes(Class<?> t1, Class<?> t2) {
        return t1.equals(t2) ||
                comparePrimative(int.class, Integer.class, t1, t2) ||
                comparePrimative(boolean.class, Boolean.class, t1, t2) ||
                comparePrimative(char.class, Character.class, t1, t2) ||
                comparePrimative(short.class, Short.class, t1, t2) ||
                comparePrimative(double.class, Double.class, t1, t2) ||
                comparePrimative(float.class, Float.class, t1, t2) ||
                comparePrimative(long.class, Long.class, t1, t2);
    }

    private static boolean comparePrimative(Class<?> primative, Class<?> object, Class<?> t2, Class<?> t3) {
        return (primative.equals(t2) && object.equals(t3)) || (primative.equals(t3) && object.equals(t2));
    }

    public class ParameterBuilder {

        private List<ParameterRules> parameterRulesList;
        private ParameterRules parameterRules;

        ParameterBuilder(List<ParameterRules> parameterRuleList,  ParameterRules parameterRules) {
            this.parameterRulesList = parameterRuleList;
            this.parameterRules = parameterRules;
        }

        public ParameterBuilder addRule(Rule rule) {
            if (this.parameterRules != null) {
                this.parameterRules.addRule(rule);
            } else {
                throw new RuntimeException("Rule can't be added to finished Builder");
            }
            return this;
        }

        public ParameterBuilder addRule(Answer<?> answer, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
            if (this.parameterRules != null) {
                this.parameterRules.addRule(answer, methodName, paramTypes);
            } else {
                throw new RuntimeException("Rule can't be added to finished Builder");
            }
            return this;
        }

        public ParameterBuilder addRule(Answer<?> answer, Method method) {
            if (this.parameterRules != null) {
                this.parameterRules.addRule(answer, method);
            } else {
                throw new RuntimeException("Rule can't be added to finished Builder");
            }
            return this;
        }

        public int finish() {
            parameterRulesList.add(this.parameterRules);
            this.parameterRules = null;
            int index = parameterRulesList.size() - 1;
            parameterRulesList = null;
            return index;
        }
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
}
