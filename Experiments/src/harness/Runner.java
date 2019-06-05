package harness;

import instrumentor.AFLMethodVisitor;
import instrumentor.AFLPathMem;
import method.MethodCall;
import method.MethodCallSession;
import method.MethodData;
import method.callbacks.EmptyMethodCallback;
import mock.ClassMap;
import mock.ConstructAnswer;
import mock.TransformClassLoader;
import mock.answers.Answer;
import mock.answers.FixedAnswer;
import mock.answers.ParameterIndexAnswer;
import mock.answers.ReturnTypeAnswer;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

    public static void main(String[] args) throws
            Exception {

        //TODO: figure out how to get generic class then we don't have to do anything
        /*
        Possibly this in configuration file
        {
           "class":"java.util.ArrayList<Foo>"
           ...Definition for Foo class...
        }

        Size is determined by a bytereader of a single integer, long etc.
         */
//        InAndOut.runSpaceTest();
//        ExampleHarness.methodCallExample();
//        Suspicion2.runSpaceTest();
    }



    private static void plaitExample() throws Exception {
        TransformClassLoader classLoader = new TransformClassLoader(
                "out/artifacts/MockingbirdFuzzer/braidit/plait-build");

        MethodCall methodCall = new MethodCall(classLoader, "com.cyberpointllc.stac.plait.Plait",
                "normalizeCompletely");

        ConstructAnswer constructAnswer = new ConstructAnswer(new String[]{"java.lang.String", "int"},
                new Answer[]{new FixedAnswer("Hello"), new FixedAnswer(23)});
        methodCall.constructMethodClass(constructAnswer);

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData m = session.runMethod(executorService);
        if (m.getReturnException() != null) {
            m.getReturnException().printStackTrace();
        } else {
            System.out.print(m);
            System.out.println(" ----- " + m.getReturnValue());
        }
        executorService.shutdown();
    }

    private static void primitiveClassMapExample() throws Exception {
        TransformClassLoader classLoader = new TransformClassLoader("resources/testClasses");
        MethodCall methodCall = new MethodCall(classLoader, "Foo", "test1", "int");

        ClassMap classMapA = new ClassMap();

        classMapA.setConstructAnswer(new ConstructAnswer(new FixedAnswer(3)));

        methodCall.overrideMethod(new ParameterIndexAnswer(0), "test1", "int");
        methodCall.associateClassMapToParameter(0, classMapA);

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData[] methodData = session.runMultipleTimesMethod(executorService, 1);
        for (MethodData m : methodData) {
            if (m.getReturnException() != null) {
                m.getReturnException().printStackTrace();
            } else {
                System.out.print(m);
                System.out.println(" ----- " + m.getReturnValue());
            }
        }


        executorService.shutdown();
    }

    private static void objectClassMapExample() throws Exception {
        TransformClassLoader classLoader = new TransformClassLoader(new File("resources/testClasses").toURI().toURL());
        MethodCall methodCall = new MethodCall(classLoader, "Foo", "loops", "Foo", "Foo");

        ClassMap classMapA = new ClassMap();
        ClassMap classMapB = new ClassMap();

        classMapA.applyMethod(new FixedAnswer(2), "test", "Foo");
        classMapB.applyMethod(new FixedAnswer(4), "test", "Foo");

        methodCall.associateClassMapToParameter(0, classMapA);
        methodCall.associateClassMapToParameter(1, classMapB);

        MethodCallSession session = methodCall.createSession(EmptyMethodCallback.create());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MethodData methodData = session.runMethod(executorService, 1000, false);

        if (methodData.getReturnException() != null) {
            methodData.getReturnException().printStackTrace();
        } else {
            System.out.println(methodData);
            System.out.println(methodData.getReturnValue());
        }

        executorService.shutdown();
    }

//    private static void transformMockClassExample1() throws
//            MalformedURLException,
//            NoSuchMethodException,
//            ClassNotFoundException, InvocationTargetException, IllegalAccessException {
//        TransformClassLoader classLoader = new TransformClassLoader(
//                new URL[]{new File("resources/testClasses").toURI().toURL()});
//
//        TransformMockClass transformMockClass = new TransformMockClass("static", "Foo");
//        transformMockClass.applyMethod("Helldsafdso", "strTest");
//        transformMockClass.applyMethod("Static Method Changed wowowow", "staticTest");
//        BasicAnswer basicAnswer = new BasicAnswer() {
//            int i = 0;
//
//            @Override
//            public Object apply(Object proxy, Object[] params, Class<?> returnType) {
//                i++;
//                return i;
//            }
//
//            @Override
//            public Answer duplicate() {
//                return null;
//            }
//        };
//        transformMockClass.applyField("value", basicAnswer);
//
//
//        classLoader.addTransformation(transformMockClass);
//
//
//        //Create Builder and Transform incoming classes using the TransformClassLoader and then install using the Byte Buddy Agent
//        classLoader.createAgentBuilder().installOn(ByteBuddyAgent.install());
//
//        Method m = transformMockClass.loadMethod("staticTest");
//        System.out.println(m.invoke(null));
//
//        Object foo = transformMockClass.newInstance();
//
//        m = transformMockClass.loadMethod("strTest");
//        System.out.println(m.invoke(foo));
//    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static String test() {
        try {
            System.out.println("In");
            try {
                System.out.println("In2");
            } finally {
                System.out.println("Here");
                return "Out2";
            }
        } finally {
            return "Out";
        }
    }
}
