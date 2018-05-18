import com.stac.image.algorithms.filters.Intensify;
import mock.MockProcess;
import mock.ParameterSet;
import mock.answers.IntIncrementAnswer;
import mock.harness.ParameterHarness;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;


/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        /*
        Options for mocked objects:
        1. Create Object naturally supplied parameters
        2. Create Mock Object and supply functionality of the methods

        Mocked Instance of Method consists of:
        1. Mocking parameters
        2. Mocking new instance calls of each callgraph
        3. Handling return of mocked method
         */


//        ParameterSet parameterSet = new ParameterSet(int.class, int.class);
//        ParameterRules intRule = new ParameterRules(2);
//        parameterSet.addParameterRules(intRule);
//        parameterSet.addParameterRules(intRule);
//        System.out.println(MockProcess.runMethod(TestClass.class, "methodToMock1", parameterSet).getOutput());
//        ParameterSet parameterSet = new ParameterSet(Foo.class, Foo.class);
//        parameterSet.createParameterBuilder()
//                .addRule(InvocationOnMock::callRealMethod, "test", Foo.class)
//                .finish();
//        parameterSet.createParameterBuilder().finish();
//        System.out.println(MockProcess.runMethod(TestClass.class, "methodToMock", parameterSet));
        IntensifyHarness harness = new IntensifyHarness(10);
        MockProcess.runMethod(Intensify.class, "filter", harness);
    }


    public static Duration testIntensify(int size, int color) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ParameterSet parameterSet = new ParameterSet(BufferedImage.class);

        parameterSet.createParameterBuilder()
                .addRule(invocation -> size, "getWidth")
                .addRule(invocation -> size, "getHeight")
                .addRule(invocation -> color, "getRGB", int.class, int.class)
                .addRule(invocation -> null, "setRGB", int.class, int.class, int.class)
                .finish();

        return MockProcess.runMethod(Intensify.class, "filter", parameterSet).getDuration();
    }

    public static void showNecessaryMocks(Method method) {
        /*
        TODO: Inside method mock calls that need to be set up
        This only makes it for the parameters
         */
        Class<?>[] params = method.getParameterTypes();
        for (Class<?> type : params) {
            analyzeObject(type);
        }
    }

    public static void analyzeObject(Class<?> type) {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass().equals(type)) {
                System.out.println("Name: " + method.getName() + " Params: " + Arrays.toString(method.getParameterTypes()) + " Return: " + method.getReturnType().toString());
            }
        }
    }

    private static class IntensifyHarness implements ParameterHarness {

        private ParameterSet parameterSet;
        private IntIncrementAnswer intIncrementAnswer;
        private int i, j;
        private Duration[] totals;

        IntensifyHarness(int size) throws NoSuchMethodException {
            parameterSet = new ParameterSet(BufferedImage.class);
            intIncrementAnswer = new IntIncrementAnswer();
            parameterSet.createParameterBuilder()
                    .addRule(invocation -> size, "getWidth")
                    .addRule(invocation -> size, "getHeight")
                    .addRule(intIncrementAnswer, "getRGB", int.class, int.class)
                    .addRule(invocation -> null, "setRGB", int.class, int.class, int.class)
                    .finish();
            i = 0;
            j = 0;
            totals = new Duration[256];

        }

        @Override
        public void handle(MockProcess mockProcess) {
            if (j < totals.length - 1) {
                intIncrementAnswer.increment();
                j++;
            } else {
                j = 0;
                i++;
                intIncrementAnswer.reset();
            }
            if (totals[j] == null) {
                totals[j] = mockProcess.getDuration();
            } else {
                totals[j] = totals[j].plus(mockProcess.getDuration());
            }
            System.out.println(i + " : " + j + " - " + totals[j] + " - " + intIncrementAnswer.getIndex());
        }

        @Override
        public ParameterSet getRules() {
            return parameterSet;
        }

        @Override
        public boolean isDone() {
            if (i >= 2) {
                for (int i = 0; i < totals.length; i++) {
                    System.out.println(i + " : " + totals[i].dividedBy(2));
                }
                return true;
            }
            return false;
        }
    }

}
