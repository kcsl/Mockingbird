package harness.plaittesting;

import method.MethodData;
import mock.answers.Answer;
import mock.answers.ConstructParamAnswer;
import mock.answers.EmptyAnswer;
import mock.answers.ReturnTypeAnswer;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class PlaitTestHarness {
//    public static void main(String[] args) throws NoSuchMethodException {
//        PlaitConstructorAnswer constructorAnswer = new PlaitConstructorAnswer();
//        MethodCallDEL methodCallDEL = MethodCallFactory.createMethodCall(Plait.class, "normalizeCompletely",
//                new ConstructParamAnswer(new Class[]{String.class, int.class}, new Answer[]{
//                        constructorAnswer, constructorAnswer
//                }));
//        methodCallDEL.overrideMethod(new EmptyAnswer(), Plait.class.getMethod("log", String.class));
//        MethodCallSessionDEL session = methodCallDEL.createSession(true);
//        while (constructorAnswer.hasNext()) {
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            MethodData methodData = session.runMethod(executorService, 2000);
//            if (methodData.getReturnException() != null && methodData.getReturnException() instanceof TimeoutException) {
//                System.out.println(constructorAnswer.curSpace.getString() + " : " + String.valueOf(constructorAnswer.curSpace.getNumStrands()));
//            } else if (methodData.getReturnException() != null) {
//                methodData.getReturnException().printStackTrace();
//            }
//            executorService.shutdownNow();
//        }
//    }

    private static class PlaitConstructorAnswer implements ReturnTypeAnswer {

        private BraidItStringSpace space;
        private Space curSpace;
        private Iterator<Space> spaceIterator;
        private int index = 1;

        public PlaitConstructorAnswer() {
            space = new BraidItStringSpace();
            spaceIterator = space.iterator();
            curSpace = null;
        }

        public boolean hasNext(){
            if (spaceIterator.hasNext()) {
                curSpace = spaceIterator.next();
                return true;
            }
            return false;
        }

        @Override
        public Object applyReturnType(Class<?> returnType, boolean forceReload) {
            if (returnType.isAssignableFrom(String.class)) {
                if (index % 1000000 == 0) {
                    index = 0;
                    System.out.println(curSpace.getString());
                }
                index++;
                return curSpace.getString();
            } else if (returnType.isAssignableFrom(int.class)) {
                return curSpace.getNumStrands();
            }
            return null;
        }

        @Override
        public Answer duplicate() {
            return new PlaitConstructorAnswer();
        }
    }
}
