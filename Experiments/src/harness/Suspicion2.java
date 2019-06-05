package harness;

import method.MethodCall;
import method.MethodCallSession;
import method.MethodData;
import method.callbacks.EmptyMethodCallback;
import mock.ClassMap;
import mock.TransformClassLoader;
import mock.answers.Answer;
import mock.answers.FixedAnswer;
import mock.answers.ParameterAnswer;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Derrick Lockwood
 * @created 2019-05-20.
 */
public class Suspicion2 {

    public static void runSpaceTest() throws Exception {

        TransformClassLoader classLoader = new TransformClassLoader(
                "resources/suspicion2/challenge_program/lib/suspicion_2.jar");
        classLoader.addAppPackage("com.cyberpointllc.stac");

        MethodCall methodCall = new MethodCall(classLoader, "com.cyberpointllc.stac.transput.MorseCode",
                "expand","java.io.InputStream", "java.io.OutputStream");

        ClassMap inputStreamParameter = ClassMap.forConstructAnswer(
                new FixedAnswer(new ByteArrayInputStream("\0\0\0H".getBytes())));

        methodCall.associateClassMapToParameter(0, inputStreamParameter);
        methodCall.overrideMethod(new ParameterAnswer() {
            @Override
            public Object applyParameters(Object[] parameters) {
                System.out.println(parameters[1]);
                return null;
            }

            @Override
            public Answer duplicate() {
                return null;
            }
        }, "writeRepeated", "int", "int", "java.io.OutputStream");

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData methodData = session.runMethod(executorService);
        printMethodData(methodData);

        executorService.shutdown();

    }

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
}
