import com.cyberpointllc.stac.battleship.OceanBoard;
import com.stac.image.algorithms.filters.Intensify;
import mock.MockProcess;
import mock.ParameterSet;
import mock.TargetedMockBuilder;
import mock.answers.Answer;
import mock.answers.IntIncrementAnswer;
import mock.answers.InvocationData;
import mock.harness.ParameterHarness;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;


/**
 * @author Derrick Lockwood
 * @created 5/14/18.
 */
public class Runner {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        /*
        Options for mocked objects:
        1. Create Object naturally supplied parameters
        2. Create Mock Object and supply functionality of the methods

        Mocked Instance of Method consists of:
        1. Mocking parameters
        2. Mocking new instance calls of each callgraph
        3. Handling return of mocked method
         */

    }

    public static void testStrikeLocator() {
        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
        targetedMockBuilder.startSubclass(OceanBoard.class)

    }

    public static void testMaxIntensify(int size) throws NoSuchMethodException, IOException {
        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
        targetedMockBuilder.startSubclass(Intensify.class, MethodCall.invokeSuper().withAllArguments()).storeSubclass();
        ColorSpaceAnswer colorSpaceAnswer = new ColorSpaceAnswer();
        targetedMockBuilder.startSubclass(BufferedImage.class)
                .apply(invocationData -> size, "getWidth")
                .apply(invocationData -> size, "getHeight")
                .apply(colorSpaceAnswer, "getRGB", int.class, int.class)
                .apply(invocationData -> null, "setRGB", int.class, int.class, int.class)
                .storeSubclass();
        Object[] objects = targetedMockBuilder.loadNoParameterProxies();
        Intensify intensify = (Intensify) objects[0];
        BufferedImage bufferedImage = (BufferedImage) objects[1];

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./resources/intensify_graph.csv"));
        long maxTime = -1;
        int maxColor = -1;
        int avgNumber = 5;
        long startTime = System.currentTimeMillis();
        while (colorSpaceAnswer.traverseColors()) {
            long total = 0;
//            System.out.print(colorSpaceAnswer.getColorValue() + " : ");
            for (int j = 0; j < avgNumber; j++) {
                long time = System.currentTimeMillis();
                intensify.filter(bufferedImage);
                time = System.currentTimeMillis() - time;
                total += time;
            }
            total /= avgNumber;
            bufferedWriter.write(colorSpaceAnswer.getColorValue() + "," + total);
            bufferedWriter.newLine();
//            System.out.println(total);
            colorSpaceAnswer.increase();
            if (total > maxTime) {
                maxTime = total;
                maxColor = colorSpaceAnswer.getColorValue();
            }
            if (total > 20) {
                System.out.println(colorSpaceAnswer.getColorValue() + " : " + total + " t: " + String.format("%.2f", (System.currentTimeMillis() - startTime) / 1000.0));
            }
        }
        bufferedWriter.flush();
        bufferedWriter.close();
        System.out.println(maxColor + " : " + maxTime);
    }

    public static void testIntensify(int size, int color) throws NoSuchMethodException {
        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
        targetedMockBuilder.startSubclass(Intensify.class, MethodCall.invokeSuper().withAllArguments()).storeSubclass();
        targetedMockBuilder.startSubclass(BufferedImage.class)
                .apply(invocationData -> size, "getWidth")
                .apply(invocationData -> size, "getHeight")
                .apply(invocationData -> color, "getRGB", int.class, int.class)
                .apply(invocationData -> null, "setRGB", int.class, int.class, int.class)
                .storeSubclass();
        Object[] objects = targetedMockBuilder.loadNoParameterProxies();
        Intensify intensify = (Intensify) objects[0];
        BufferedImage bufferedImage = (BufferedImage) objects[1];
        long time = System.currentTimeMillis();
        intensify.filter(bufferedImage);
        System.out.println(System.currentTimeMillis() - time);
    }

    private static class ColorSpaceAnswer implements Answer<Object> {

        private int index;
        private int r, g, b;
        private int value;
        private int increment;

        public ColorSpaceAnswer() {
            index = 0;
            r = 0;
            g = 0;
            b = 0;
            increment = 10;
            value = (r << 16) & (g << 8) & b;
        }


        @Override
        public Object handle(InvocationData invocationData) throws Throwable {
            return value;
        }

        public boolean traverseColors() {
            return r < 256;
        }

        public void increase() {
            if(b >= 255) {
                g += increment;
                b = 0;
            } else {
                b += increment;
            }
            if (g >= 256){
                r += increment;
                g = 0;
            }
            value = (r << 16) + (g << 8) + b;
            index++;
        }

        public int getColorValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }
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
