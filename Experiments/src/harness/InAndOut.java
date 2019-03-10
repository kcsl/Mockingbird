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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

        FixedAnswer randomCreator = new FixedAnswer(new Random());

        ClassMap pieOrderMapping = new ClassMap();

        pieOrderMapping.setConstructAnswer(pieConstruction);

        methodCall.associateClassMapToParameter(0, pieOrderMapping);
        methodCall.addFieldVariable("random", randomCreator);

        MethodCallSession session = methodCall.createSession(
                CSVMethodCallback.createWithImmediateWrite("resources/inandoutfuzz.csv",
                        methodData -> new Object[]{methodData.getDeltaHeapMemory()}));
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        while (pieAnswer.hasNext()) {
            session.runMethod(executorService).print();
            System.out.println(pieAnswer.orderComponentsIterator.current + " : " + pieAnswer.numberOfPies);
        }
//        List<Future<MethodData>> futures = new ArrayList<>(10);
//        while (pieAnswer.hasNext()) {
//            for (int i = 0; i < 10 && pieAnswer.hasNext(); i++) {
//                futures.add(session.runAsyncMethod(executorService));
//            }
//            finishedOrder(futures, MethodData::print);
//        }
        executorService.shutdown();
    }

    private static <V> void finishedOrder(List<Future<V>> futures, Consumer<V> consumer) throws
            ExecutionException,
            InterruptedException {
        boolean isDone = false;
        while (!isDone) {
            isDone = true;
            for (Future<V> future : futures) {
                if (future.isDone()) {
                    consumer.accept(future.get());
                } else {
                    isDone = false;
                }
            }
        }
    }

    private static class PieAnswer implements ReturnTypeAnswer {

        private final OrderComponentsIterator orderComponentsIterator;
        private int numberOfPies;
        boolean nextValue;

        PieAnswer() {
            numberOfPies = 0;
            orderComponentsIterator = new OrderComponentsIterator();
            nextValue = true;
        }

        @Override
        public Object applyReturnType(Class<?> returnType, boolean forceReload) {
            if (returnType.isAssignableFrom(String.class)) {
                return orderComponentsIterator.current.toString();
            } else if (returnType.isAssignableFrom(int.class)) {
                if (numberOfPies > 9) {
                    numberOfPies = 0;
                    orderComponentsIterator.next();
                }
                numberOfPies++;
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
