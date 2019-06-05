package harness;

import method.MethodCall;
import method.MethodCallSession;
import method.MethodData;
import method.callbacks.EmptyMethodCallback;
import mock.ClassMap;
import mock.ConstructAnswer;
import mock.TransformClassLoader;
import mock.answers.Answer;
import mock.answers.FixedAnswer;
import mock.answers.ParameterAnswer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Derrick Lockwood
 * @created 2019-03-25.
 */
public class ExampleHarness {

    private static void printMethodData(MethodData methodData) {
        if (methodData.getReturnException() != null) {
            methodData.getReturnException().printStackTrace();
        } else {
            if (methodData.getReturnValue() != null) {
                System.out.print(methodData.getReturnValue() + " : ");
            }
            System.out.println(methodData);
        }
    }

    public static void badConstructorCalled() throws Exception {
        TransformClassLoader classLoader = new TransformClassLoader("resources/testClasses/");

        MethodCall methodCall = new MethodCall(classLoader, "harness.Example", "simpleParameterExample",
                "int");

        ClassMap a = ClassMap.forConstructAnswer(new FixedAnswer(1));

        methodCall.associateClassMapToParameter(0, a);
        methodCall.constructMethodClass(new ConstructAnswer(new String[]{"java.lang.String", "int"},
                new Answer[]{new FixedAnswer("23"), new FixedAnswer(3)}));

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData methodData = session.runMethod(executorService);
        printMethodData(methodData);

        executorService.shutdown();
    }

    public static void targetHarness() throws Exception {

        TransformClassLoader classLoader = new TransformClassLoader("resources/testClasses/");

        MethodCall methodCall = new MethodCall(classLoader, "harness.Example", "simpleParameterExample",
                "int");

        ClassMap a = ClassMap.forConstructAnswer(new FixedAnswer(1));
        ClassMap field = ClassMap.forConstructAnswer(new FixedAnswer(3));
        ClassMap s = ClassMap.forConstructAnswer(new FixedAnswer("23"));

        methodCall.associateClassMapToParameter(0, a);
        methodCall.associateFieldVariable("b", "int", field);
        methodCall.associateFieldVariable("s", "String", s);

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData methodData = session.runMethod(executorService);
        printMethodData(methodData);

        executorService.shutdown();
    }

    public static void fieldVariableExample() throws Exception {

        TransformClassLoader classLoader = new TransformClassLoader("resources/testClasses/");

        MethodCall methodCall = new MethodCall(classLoader, "harness.Example", "stringFieldVariableExample");

        ClassMap s = ClassMap.forConstructAnswer(new FixedAnswer("aeiofdsafdkjslaj"));

        methodCall.associateFieldVariable("s", "String", s);

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData methodData = session.runMethod(executorService);
        printMethodData(methodData);

        executorService.shutdown();
    }

    public static void methodCallExample() throws Exception {
        TransformClassLoader classLoader = new TransformClassLoader("resources/testClasses/");

        MethodCall methodCall = new MethodCall(classLoader, "harness.Example", "methodCallExample", "int", "int",
                "int");

        ClassMap a = ClassMap.forConstructAnswer(new FixedAnswer(1));
        ClassMap b = ClassMap.forConstructAnswer(new FixedAnswer(1));
        ClassMap c = ClassMap.forConstructAnswer(new FixedAnswer(1));

        methodCall.associateClassMapToParameter(0, a);
        methodCall.associateClassMapToParameter(1, b);
        methodCall.associateClassMapToParameter(2, c);
        methodCall.overrideMethod(new ParameterAnswer() {
            @Override
            public Object applyParameters(Object[] parameters) {
                int t = (int) parameters[0];
                if (t == 8) {
                    return 0;
                }
                return t / 2;
            }

            @Override
            public Answer duplicate() {
                return null;
            }
        }, "badMethod", "int");

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData methodData = session.runMethod(executorService);
        printMethodData(methodData);

        executorService.shutdown();
    }
}
