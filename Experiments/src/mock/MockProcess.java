package mock;

import mock.harness.ParameterHarness;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @author Derrick Lockwood
 * @created 5/16/18.
 */
public class MockProcess {

    private Object output;
    private Duration time;

    MockProcess(Object output, long time) {
        this.output = output;
        this.time = Duration.of(time, ChronoUnit.MILLIS);
    }

    public static MockProcess runMethod(Class<?> type, String methodName, ParameterSet parameterSet) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return runMethod(type.getDeclaredMethod(methodName, parameterSet.getParameterTypes()), parameterSet.getParameterRules());
    }

    public static MockProcess runMethod(Method method, ParameterRules[] rules) throws InvocationTargetException, IllegalAccessException {
        /*
        TODO: Setup so that it doesn't have to create same mock object everytime it runs so you can run it
         n times and use the same mock object but only change the Answer function
         */

//        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
//        targetedMockBuilder.mock(method.getDeclaringClass(), new CallRealMethodAnswer())
//                .store();
//        Class<?>[] params = method.getParameterTypes();
//        for (int i = 0; i < params.length; i++) {
//            if (rules[i].isValueSet()) {
//                targetedMockBuilder.addObject(rules[i].getValue());
//                continue;
//            }
//            targetedMockBuilder.mock(params[i]);
//            rules[i].applyRules(targetedMockBuilder);
//            targetedMockBuilder.store();
//        }
//        Object[] funcDef = targetedMockBuilder.getFinishedMockObjects();
//        Object[] paramArray = Arrays.copyOfRange(funcDef, 1, funcDef.length);
//        long time = System.currentTimeMillis();
//        method.setAccessible(true);
//        Object val = method.invoke(funcDef[0], paramArray);
//        return new MockProcess(val, System.currentTimeMillis() - time);
        return null;
    }

    public static void runMethod(Class<?> type, String methodName, ParameterHarness parameterHarness) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        runMethod(type.getMethod(methodName, parameterHarness.getRules().getParameterTypes()), parameterHarness);
    }

    public static void runMethod(Method method, ParameterHarness parameterHarness) throws InvocationTargetException, IllegalAccessException {
//        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
//        targetedMockBuilder.mock(method.getDeclaringClass(), new CallRealMethodAnswer())
//                .store();
//        ParameterRules[] rules = parameterHarness.getRules().getParameterRules();
//        Class<?>[] params = method.getParameterTypes();
//        for (int i = 0; i < params.length; i++) {
//            if (rules[i].isValueSet()) {
//                targetedMockBuilder.addObject(rules[i].getValue());
//                continue;
//            }
//            targetedMockBuilder.mock(params[i]);
//            rules[i].applyRules(targetedMockBuilder);
//            targetedMockBuilder.store();
//        }
//        Object[] funcDef = targetedMockBuilder.getFinishedMockObjects();
//        Object[] paramArray = Arrays.copyOfRange(funcDef, 1, funcDef.length);
//        while (!parameterHarness.isDone()) {
//            long time = System.currentTimeMillis();
//            Object val = method.invoke(funcDef[0], paramArray);
//            parameterHarness.handle(new MockProcess(val, System.currentTimeMillis() - time));
//        }
    }

    public Object getOutput() {
        return output;
    }

    public Duration getDuration() {
        return time;
    }

    @Override
    public String toString() {
        return time.toString() + " : " + Objects.toString(output);
    }
}
