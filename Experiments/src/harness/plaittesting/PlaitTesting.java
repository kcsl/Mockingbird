package harness.plaittesting;

import method.MethodCall;
import method.MethodCallFactory;
import method.MethodCallSession;
import method.MethodData;
import method.callbacks.MethodCallback;
import mock.answers.Answer;
import mock.answers.ConstructParamAnswer;
import mock.answers.ReturnTypeAnswer;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * @author Derrick Lockwood
 * @created 9/10/18.
 */
public class PlaitTesting {

    public static void main(String[] args) throws NoSuchMethodException, FileNotFoundException {
        PlaitConstructorAnswer plaitConstructorAnswer = new PlaitConstructorAnswer();
        MethodCall methodCall = MethodCallFactory.createMethodCall(Plait.class, "normalizeCompletely",
                new ConstructParamAnswer(new Class[]{String.class, int.class},
                        new Answer[]{plaitConstructorAnswer, plaitConstructorAnswer}));
        MethodCallSession session = methodCall.createSession(true);
        PrintStream printStream = new PrintStream(new FileOutputStream(new File("./resources/plait_output.txt")));
        while (plaitConstructorAnswer.canContinue()) {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            MethodData methodData = session.runMethod(executorService, 5000, true);
            if (methodData.getReturnException() != null && methodData.getReturnException() instanceof TimeoutException) {
                printStream.println(methodData.getReturnException().getClass());
                String s =
                        "Strand: " + plaitConstructorAnswer.curSpace.getString() + " Unicode: " + getUnicodeDisplayString(plaitConstructorAnswer.curSpace.getString()) + " NumStrands: " + plaitConstructorAnswer.curSpace.getNumStrands();
                printStream.println(s);
            }
            executorService.shutdownNow();
        }
    }

    static class PlaitConstructorAnswer implements ReturnTypeAnswer {

        private Iterator<Space> spaceIterator;
        private Space curSpace;

        public PlaitConstructorAnswer() {
            spaceIterator = new BraidItStringSpace().iterator();
            curSpace = spaceIterator.next();
        }

        public boolean canContinue() {
            if (spaceIterator.hasNext()) {
                curSpace = spaceIterator.next();
                return true;
            }
            return false;
        }

        @Override
        public Object applyReturnType(Class<?> returnType, boolean forceReload) {
            if (returnType.isAssignableFrom(String.class)) {
                return curSpace.getString();
            } else {
                return curSpace.getNumStrands();
            }
        }

        @Override
        public Answer duplicate() {
            return null;
        }
    }

    public static String getUnicodeDisplayString(String str) {
        StringBuilder unicodeDisplayString = new StringBuilder();
        unicodeDisplayString.append("\"");
        for(Character c : str.toCharArray()) {
            unicodeDisplayString.append(String.format("\\u%04x", (int)c));
        }
        unicodeDisplayString.append("\"");
        return unicodeDisplayString.toString();
    }
}
