package harness;

import method.MethodData;
import method.callbacks.MethodCallback;
import mock.answers.Answer;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class StacColorSpaceExample {

    private StacColorSpaceExample() {
    }


    public static void run() {
//        ColorSpaceSubAnswer colorSpaceSubAnswer = new ColorSpaceSubAnswer();
//        MethodCallback methodCallback = CSVMethodCallback.create("./resources/color_space.csv", methodData -> new Object[]{
//                colorSpaceSubAnswer.r,
//                colorSpaceSubAnswer.g,
//                colorSpaceSubAnswer.b,
//                methodData.getDuration()
//        });
//        int imgSize = 50;
//        MethodCall methodCall = MethodCall.createMethodCall(colorSpaceSubAnswer.link(methodCallback), Intensify.class, "filter", BufferedImage.class);
//        methodCall.createParameterMock(0)
//                .applyMethod(imgSize, "getWidth")
//                .applyMethod(imgSize, "getHeight")
//                .applyMethod(colorSpaceSubAnswer, "getRGB", int.class, int.class)
//                .applyMethod("setRGB", int.class, int.class, int.class);
//        methodCall.run();
    }

    private static class ColorSpaceSubAnswer implements Answer, MethodCallback {

        private int index;
        private int r, g, b;
        private int value;
        private int increment;

        public ColorSpaceSubAnswer() {
            index = 0;
            r = 0;
            g = 0;
            b = 0;
            increment = 10;
            value = (r << 16) & (g << 8) & b;
        }

        public boolean traverseColors() {
            return r < 256;
        }

        public void increase() {
            if (b >= 255) {
                g += increment;
                b = 0;
            } else {
                b += increment;
            }
            if (g >= 256) {
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

        @Override
        public Object handle(Object o, Object[] args, Callable<Object> originalMethod, Method method) {
            return value;
        }

        @Override
        public Object handle(Object[] args) throws Throwable {
            return value;
        }

        @Override
        public Object handle(Object proxy, Object[] parameters, String name, Class<?> returnType) {
            return value;
        }

        @Override
        public Object handle(Object proxy, Object[] args, Method method) throws Throwable {
            return value;
        }

        @Override
        public void onBefore(MethodData methodData) {

        }

        @Override
        public void onAfter(MethodData methodData) {
            increase();
        }

        @Override
        public void onEndIteration() {

        }

        @Override
        public boolean continueIteration() {
            return traverseColors();
        }
    }

//    public static void testMaxIntensify(int size) throws NoSuchMethodException, IOException {
//        TargetedMockBuilder targetedMockBuilder = new TargetedMockBuilder();
//        targetedMockBuilder.createSubclass(Intensify.class, MethodCall.invokeSuper().withAllArguments()).storeSubclass();
//        ColorSpaceSubAnswer colorSpaceAnswer = new ColorSpaceSubAnswer();
//        targetedMockBuilder.createSubclass(BufferedImage.class)
//                .apply(invocationData -> size, "getWidth")
//                .apply(invocationData -> size, "getHeight")
//                .apply(colorSpaceAnswer, "getRGB", int.class, int.class)
//                .apply(invocationData -> null, "setRGB", int.class, int.class, int.class)
//                .storeSubclass();
//        Object[] objects = targetedMockBuilder.loadNoParameterProxies();
//        Intensify intensify = (Intensify) objects[0];
//        BufferedImage bufferedImage = (BufferedImage) objects[1];
//
//        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./resources/intensify_graph.csv"));
//        long maxTime = -1;
//        int maxColor = -1;
//        int avgNumber = 5;
//        long startTime = System.currentTimeMillis();
//        while (colorSpaceAnswer.traverseColors()) {
//            long total = 0;
////            System.out.print(colorSpaceAnswer.getColorValue() + " : ");
//            for (int j = 0; j < avgNumber; j++) {
//                long time = System.currentTimeMillis();
//                intensify.filter(bufferedImage);
//                time = System.currentTimeMillis() - time;
//                total += time;
//            }
//            total /= avgNumber;
//            bufferedWriter.write(colorSpaceAnswer.getColorValue() + "," + total);
//            bufferedWriter.newLine();
////            System.out.println(total);
//            colorSpaceAnswer.increase();
//            if (total > maxTime) {
//                maxTime = total;
//                maxColor = colorSpaceAnswer.getColorValue();
//            }
//            if (total > 20) {
//                System.out.println(colorSpaceAnswer.getColorValue() + " : " + total + " t: " + String.format("%.2f", (System.currentTimeMillis() - startTime) / 1000.0));
//            }
//        }
//        bufferedWriter.flush();
//        bufferedWriter.close();
//        System.out.println(maxColor + " : " + maxTime);
//    }
//
}
