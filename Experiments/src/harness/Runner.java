package harness;

import method.MethodCall;
import method.callbacks.*;
import mock.MockClass;
import mock.TargetedMockBuilder;
import mock.answers.auto.AutoIncrementor;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;


/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IOException, NoSuchFieldException {
        spaceExample();
    }

    public static void instanceVariableMethodCallExample() throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        MethodCall methodCall = MethodCall.createMethodCall(TestClass.class, "methodToMock2");
        methodCall.linkMethodCallback(PrintMethodCallback.create());
        methodCall.createInstanceMock("instanceObject")
                .applyMethod("Woot woot", "strTest");
        methodCall.run();
    }

    public static void functionRoundingExample() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        OutputRule outputRule = methodData -> {
            long time = TimeUnit.NANOSECONDS.toMillis(methodData.getDuration().get(ChronoUnit.NANOS));
            return time >= 1 ? methodData.getParameters()[0] + " : " + methodData.getDuration().toString() : null;
        };
        AutoIncrementor autoIncrementor = AutoIncrementor.createIncrementor(6690000, 100, 0);
        MethodCallback methodCallback = IterationMethodCallback.create(10000000)
                .link(AutoMethodCallback.create(autoIncrementor))
                .link(LogMethodCallback.create("./resources/round.txt", outputRule));
        MethodCall methodCall = MethodCall.createMethodCall(methodCallback, Foo.class, "function", int.class);
        methodCall.createParameterMock(0, autoIncrementor);
        methodCall.run();
    }

    public static void spaceExample() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MethodCallback methodCallback = IterationMethodCallback.create(1).andAfter(methodData -> System.out.println(methodData.toString()));
        MethodCall fooMethodCall = MethodCall.createMethodCall(methodCallback, Foo.class, "spaceTest");
        fooMethodCall.run();
    }

    public static void autoMethodMockExample() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MethodCallback methodCallback = IterationMethodCallback.create(5).andAfter(methodData -> System.out.println(methodData.getReturnValue() + " : " + methodData.getDuration() + " : " + methodData.getDeltaHeapMemory()));
        MethodCall fooMethodCall = MethodCall.createMethodCall(methodCallback, Foo.class, "loops", Foo.class, Foo.class);
        fooMethodCall.autoFillParameters();
        fooMethodCall.run();
    }

    public static void methodMockExample() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AutoIncrementor fooOne = AutoIncrementor.createIncrementor();
        AutoIncrementor fooTwo = AutoIncrementor.createIncrementor();
        MethodCallback methodCallback = IterationMethodCallback.create(5)
                .link(AutoMethodCallback.create(fooOne, fooTwo));

        MethodCall fooMethodCall = MethodCall.createMethodCall(methodCallback, Foo.class, "loops", Foo.class, Foo.class);
        fooMethodCall.createParameterMock(0, fooOne);
        fooMethodCall.createParameterMock(1, fooTwo);
        fooMethodCall.run();
    }

    public static void staticMethodExample() throws NoSuchMethodException {
        System.out.println(Foo.staticTest());
        ByteBuddyAgent.install();
        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
        targetedMockBuilder.createRedefine(Foo.class, (Implementation) null)
                .applyStaticMethod("Woot Woot", "staticTest")
                .store();
        System.out.println(Foo.staticTest());
    }

    public static void instanceVariableExample() throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
        MockClass testClassMockClass = targetedMockBuilder.createSubclass(TestClass.class, SuperMethodCall.INSTANCE);

        MockClass fooMockClass = targetedMockBuilder.createSubclass(Foo.class)
                .applyMethod("Woot Woot", "strTest");
        fooMockClass.store();

        TestClass testClass = (TestClass) testClassMockClass.applyField("instanceObject", fooMockClass)
                .newInstance();
        System.out.println(testClass.methodToMock2());
    }

}
