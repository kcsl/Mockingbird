package method;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Derrick Lockwood
 * @created 6/13/18.
 */
public class MethodRunner {

    private long timeOut;
    private ExecutorService executorService;

    public MethodRunner(long timeOut) {
        executorService = Executors.newFixedThreadPool(5);
        this.timeOut = timeOut;
    }

    /**
     * Runs the method to mock and recreates the declared object and its parameters every time the method is run
     */
//    public void runRecreate(MethodCall methodCall) {
//
//        while (methodCall.methodCallback.continueIteration()) {
//            Object mockObject = methodCall.methodMockClass.newInstance();
//            Object[] objects = new Object[methodCall.parameters.length];
//            for (MockCreator mockCreator : normalObjects) {
//                mockCreator.newInstance();
//            }
//            for (int i = 0; i < parameters.length; i++) {
//                objects[i] = parameters[i].newInstance();
//            }
//            runMethod(mockObject, objects);
//        }
//        methodCallback.onEndIteration();
//        executorService.shutdown();
//    }
//
//    /**
//     * Runs the method to mock but only creates the declared object ONCE and the parameters ONCE unless it is a primitive
//     * variable
//     */
//    public void run() {
//        Object mockObject = methodMockClass.newInstance();
//        Object[] objects = new Object[parameters.length];
//        for (MockCreator mockCreator : normalObjects) {
//            mockCreator.newInstance();
//        }
//        for (int i = 0; i < parameters.length; i++) {
//            objects[i] = parameters[i].newInstance();
//        }
//        while (methodCallback.continueIteration()) {
//            runMethod(mockObject, objects);
//            for (int i = 0; i < parameters.length; i++) {
//                if (parameters[i].isPrimitive()) {
//                    objects[i] = parameters[i].newInstance();
//                }
//            }
//            //Reloads the primitive instance variables defined applyReturnType the mockObject
//            methodMockClass.reloadInstanceVariables(mockObject, primitiveInstanceVariables);
//        }
//        methodCallback.onEndIteration();
//        executorService.shutdown();
//    }
//
//    private void runMethod(Object mockObject, Object[] objects, long timeOut) {
//        MethodData methodData = new MethodData(mockObject, objects, method.getDeclaringClass(), method.getName(), method.getReturnType(), method.getParameterTypes());
//        methodCallback.onBefore(methodData);
//        Future<Object[]> future = executorService.submit(getCallable(mockObject, objects));
//        Object[] values = new Object[]{
//                null,
//                null,
//                null,
//                0L
//        };
//        try {
//            if (timeOut > 0) {
//                values = future.get(timeOut, TimeUnit.MILLISECONDS);
//            } else {
//                values = future.get();
//            }
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            values[1] = e;
//            values[2] = Duration.of(timeOut, ChronoUnit.MILLIS);
//        }
//        methodData.setOutput(values[0], (Exception) values[1], (Duration) values[2], (long) values[3]);
//        methodCallback.onAfter(methodData);
//    }
}
