package harness;

import method.MethodCall;
import method.MethodCallSession;
import method.MethodData;
import method.callbacks.CSVMethodCallback;
import mock.ClassMap;
import mock.ConstructAnswer;
import mock.TransformClassLoader;
import mock.answers.Answer;
import mock.answers.FixedAnswer;
import mock.answers.ReturnTypeAnswer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @author Derrick Lockwood
 * @created 2019-03-09.
 */
public class InAndOut {


    public static void runSpaceTest() throws Exception {
        TransformClassLoader classLoader = new TransformClassLoader("resources/challenge_program/lib/inandout_1.jar");

        classLoader.addAppPackage("com.cyberpointllc.stac.inandout");

        MethodCall methodCall = new MethodCall(classLoader, "com.cyberpointllc.stac.inandout.OrderDirector", "cookPie",
                "com.cyberpointllc.stac.inandout.PieOrder");
        ConstructAnswer orderFaceConstructor = new ConstructAnswer(new String[]{"java.lang.String"},
                new Answer[]{new FixedAnswer("TESTPIE")});
        PieAnswer pieAnswer = new PieAnswer();
        ConstructAnswer orderComponentsConstructor = new ConstructAnswer(new String[]{"java.lang.String"},
                new Answer[]{pieAnswer});

        ConstructAnswer pieConstruction = new ConstructAnswer(
                new String[]{"com.cyberpointllc.stac.inandout.OrderFACE", "com.cyberpointllc.stac.inandout.OrderComponents", "int"},
                new Answer[]{orderFaceConstructor, orderComponentsConstructor, pieAnswer});

        ClassMap pieOrderMapping = new ClassMap();

        pieOrderMapping.setConstructAnswer(pieConstruction);

        methodCall.associateClassMapToParameter(0, pieOrderMapping);
        methodCall.associateFieldVariable("random", "java.lang.Random", ClassMap.forConstructAnswer(new FixedAnswer(new Random())));

        MethodCallSession session = methodCall.createSession(
                CSVMethodCallback.createWithImmediateWrite("resources/inandoutfuzz.csv",
                        InAndOut::getMethodInput));
        int threads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<MethodData>> futures = new ArrayList<>(threads);
        boolean isValid;
        while (pieAnswer.hasNext()) {
            isValid = true;
            for (int i = 0; i < threads && pieAnswer.hasNext(); i++) {
                futures.add(session.runAsyncMethod(executorService));
            }
            for (Future<MethodData> future: futures) {
                try {
                    MethodData m = future.get(10, TimeUnit.SECONDS);
                    if (m.getReturnException() != null) {
                        m.getReturnException().printStackTrace();
                        isValid = false;
                        break;
                    }
                    Object[] o = getMethodInput(m);
                    System.out.println(o[0]);
                } catch (InterruptedException | TimeoutException e){
                    System.out.println("Timeout");
                    isValid = false;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    isValid = false;
                    break;
                }
            }
            if (!isValid) {
                break;
            }
        }
        executorService.shutdown();
    }

    private static <V> void finishedOrder(List<Future<V>> futures, Consumer<V> consumer) throws
            ExecutionException,
            InterruptedException {
        boolean isDone = false;
        while (!isDone) {
            isDone = true;
            Iterator<Future<V>> iterator = futures.iterator();
            while(iterator.hasNext()) {
                Future<V> future = iterator.next();
                if (future.isDone()) {
                    consumer.accept(future.get());
                    iterator.remove();
                } else {
                    isDone = false;
                }
            }
        }
    }

    private static Object[] getMethodInput(MethodData methodData) {
        String s = "";
        Integer[] componentInts;
        int quantity;
        try {
            Object o = methodData.getParameters()[0];
            Class<?> z = methodData.getParameterTypes()[0];
            Field f = z.getDeclaredField("orderComponents");
            f.setAccessible(true);
            Object components = f.get(o);
            f = z.getDeclaredField("quantity");
            f.setAccessible(true);
            quantity = f.getInt(o);
            z = components.getClass();
            f = z.getDeclaredField("componentsAppeal");
            f.setAccessible(true);
            componentInts = (Integer[]) f.get(components);
            s = "\"" + Arrays.toString(componentInts) + " : " + quantity + "\"";
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return new Object[] {"ERROR"};
        }
        return new Object[]{s, quantity, methodData.getDeltaHeapMemory()};
    }

    private static class PieAnswer implements ReturnTypeAnswer {

        private final OrderComponentsIterator orderComponentsIterator;
        private int numberOfPies;

        PieAnswer() {
            numberOfPies = 0;
            orderComponentsIterator = new OrderComponentsIterator();
            orderComponentsIterator.next();
        }

        @Override
        public Object applyReturnType(Class<?> returnType, boolean forceReload) {
            if (returnType.isAssignableFrom(String.class)) {
                System.out.println(this);
                return orderComponentsIterator.current.toString();
            } else if (returnType.isAssignableFrom(int.class)) {
                if (numberOfPies > 9) {
                    numberOfPies = 0;
                    orderComponentsIterator.next();
                }
                numberOfPies++;
                System.out.println(this);
                return numberOfPies;
            }
            return null;
        }

        public boolean hasNext() {
            return orderComponentsIterator.hasNext();
        }

        @Override
        public Answer duplicate() {
            return new PieAnswer();
        }

        @Override
        public String toString() {
            return orderComponentsIterator.current.toString() + " : " + numberOfPies;
        }
    }

    private static class OrderComponentsIterator implements Iterator<String> {
        private final CombinationIterator<Integer> iterator;
        private final StringBuilder current;

        OrderComponentsIterator() {
            Integer[] ints = new Integer[15];
            for (int i = 0; i < ints.length; i++) {
                ints[i] = i + 1;
            }
            iterator = new CombinationIterator<>(ints);
            current = new StringBuilder();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            if (iterator.hasNext()) {
                current.setLength(0);
                for (Integer i : iterator.next()) {
                    current.append(i);
                    current.append(',');
                }
                current.setLength(current.length() - 1);
            }
            return current.toString();
        }
    }

    private static class CombinationIterator<V> implements Iterator<List<V>> {

        private final V[] startList;
        private int[] combination;
        private int k;

        CombinationIterator(V... startList) {
            this.startList = startList;
            k = 0;
            incrementCombination();
        }

        private void incrementCombination() {
            k++;
            combination = new int[k];
            for (int i = 0; i < k; i++) {
                combination[i] = i;
            }
        }

        @Override
        public boolean hasNext() {
            return k < startList.length;
        }

        @Override
        public List<V> next() {
            if (combination[k - 1] >= startList.length) {
                incrementCombination();
            }
            List<V> list = new ArrayList<>(combination.length);
            for (int i = 0; i < combination.length; i++) {
                list.add(startList[combination[i]]);
            }
            int t = k - 1;
            while (t != 0 && combination[t] == startList.length - k + t) {
                t--;
            }
            combination[t]++;
            for (int i = t + 1; i < k; i++) {
                combination[i] = combination[i - 1] + 1;
            }
            return list;
        }
    }
}
