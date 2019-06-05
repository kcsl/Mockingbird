package harness.tollboothtesting;

import mock.answers.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Derrick Lockwood
 * @created 8/7/18.
 */
public class TollBoothTesting {

    private static final int STARTING_SIZE = 4;
    private static final int BENIGN_USER_ID = -1;
    private static final int NUMBER_OF_TRIES = 1000;
    private static final boolean RESTART = true;

//    public static void main(String[] args) throws
//            NoSuchMethodException,
//            MalformedURLException,
//            IllegalAccessException,
//            InvocationTargetException, ClassNotFoundException, NoSuchFieldException, FileNotFoundException {
//        InstrumentLoader.addToClassPath(new File("./resources/TollBooth-1.0.0.jar"));
//        MethodCallDEL methodCallDEL = MethodCallFactory.createMethodCall(Class.forName("com.bbn.TransponderDevice"), "run",
//                (String[]) null);
//
//        methodCallDEL.overrideMethodCall(new RunOverrideMethod());
//
//        methodCallDEL.addFieldInstantiator("rand", new ConstructParamAnswer(null, null));
//
//        SubMockClassDELDEL subMockClass = methodCallDEL.createFieldMock("w");
//
//        SetReturnAnswer setReturnAnswer = new SetReturnAnswer();
//        subMockClass.applyMethod(setReturnAnswer, "getKnownTransponders");
//        subMockClass.applyMethod(new CarPassedAnswer(setReturnAnswer), "carPassed", int.class);
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//        MethodData methodData = methodCallDEL.createSession().runMethod(executorService, -1);
//        if (methodData.getReturnException() != null) {
//            methodData.getReturnException().printStackTrace();
//        }
//        executorService.shutdownNow();
//    }

    private static class SetReturnAnswer implements ReturnTypeAnswer {

        private int curSize;
        private Set<Integer> set;

        public SetReturnAnswer() {
            curSize = STARTING_SIZE;
            set = null;
        }

        @Override
        public Object applyReturnType(Class<?> returnType, boolean forceReload) {
            if (set == null) {
                set = new HashSet<>();
                set.add(BENIGN_USER_ID);
                for (int i = 1; i < curSize; i++) {
                    set.add(i);
                }
            }
            return set;
        }

        public void increment() {
            set = null;
            curSize++;
            if (curSize > 178) {
                System.exit(0);
            }
        }

        @Override
        public Answer duplicate() {
            return null;
        }
    }

    private static class CarPassedAnswer implements ParameterAnswer {

        private int hits = 0;
        private int misses = 0;
        private SetReturnAnswer setReturnAnswer;
        private PrintStream printStream;

        public CarPassedAnswer(SetReturnAnswer setReturnAnswer) throws FileNotFoundException {
            this.setReturnAnswer = setReturnAnswer;
            printStream = new PrintStream(new FileOutputStream("./resources/resets.csv", !RESTART));
            printStream.println("Set Size,P(random2 == benign packet index)");
        }

        @Override
        public Object applyParameters(Object[] parameters) {
            if ((int) parameters[0] == BENIGN_USER_ID) {
                hits++;
            } else {
                misses++;
            }
            System.out.println(
                    "Size: " + setReturnAnswer.curSize + " Hits: " + hits + " Misses: " + misses + " Total: " + (hits + misses));
            if (hits + misses >= NUMBER_OF_TRIES) {
                double percentage = hits / (double) (misses + hits);

                printStream.println(
                        String.format("%03d", setReturnAnswer.curSize) + "," + String.format("%.5f", percentage));
                reset();
            }
            return null;
        }

        private void reset() {
            setReturnAnswer.increment();
            hits = 0;
            misses = 0;
        }

        @Override
        public Answer duplicate() {
            return null;
        }
    }

    private static class RunOverrideMethod implements BasicAnswer {

        @Override
        public Object apply(Object proxy, Object[] params, Class<?> returnType) {
            try {
                Class<?> type = proxy.getClass().getSuperclass();
                Field randField = type.getDeclaredField("rand");
                randField.setAccessible(true);
                Random rand = (Random) randField.get(proxy);
                Field workerField = type.getDeclaredField("w");
                workerField.setAccessible(true);
                Object worker = workerField.get(proxy);
                Class<?> workerType = worker.getClass();
                Method getKnownTransponders = workerType.getMethod("getKnownTransponders");
                Method carPassed = workerType.getMethod("carPassed", int.class);
                while (true) {
                    Set<Integer> knownTransponders;
                    int rnd = rand.nextInt(100);
                    System.out.println("Rnd rolled: " + Integer.toString(rnd));
                    if (rnd < 25 && (knownTransponders = (Set<Integer>) getKnownTransponders.invoke(
                            worker)).size() > 0) {
                        Integer transponderReadout = (Integer) knownTransponders.toArray()[rand.nextInt(
                                knownTransponders.size())];
                        carPassed.invoke(worker, transponderReadout);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Answer duplicate() {
            return null;
        }
    }

}
