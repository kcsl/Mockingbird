package method.callbacks;

import method.MethodData;

/**
 * @author Derrick Lockwood
 * @created 6/11/18.
 */
public class ExceptionMethodCallback implements MethodCallback {
    public static MethodCallback create() {
        return new ExceptionMethodCallback();
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {
        if (methodData.getReturnException() != null) {
            throw new RuntimeException(methodData.getReturnException());
        }
    }

    @Override
    public void onEndIteration() {

    }

    @Override
    public boolean continueIteration() {
        return false;
    }
}
