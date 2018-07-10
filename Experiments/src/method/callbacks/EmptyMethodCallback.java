package method.callbacks;

import method.MethodData;

/**
 * @author Derrick Lockwood
 * @created 6/27/18.
 */
public class EmptyMethodCallback implements MethodCallback {
    public static MethodCallback create() {
        return new EmptyMethodCallback();
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {

    }

    @Override
    public void onEndIteration() {

    }

    @Override
    public boolean continueIteration() {
        return false;
    }
}
