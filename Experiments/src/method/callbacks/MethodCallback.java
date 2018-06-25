package method.callbacks;

import method.MethodData;

/**
 * @author Derrick Lockwood
 * @created 6/6/18.
 */
public interface MethodCallback {
    void onBefore(MethodData methodData);

    void onAfter(MethodData methodData);

    void onEndIteration();

    boolean continueIteration();

    default MethodCallback link(MethodCallback methodCallback) {
        MethodCallback self = this;
        return new MethodCallback() {
            @Override
            public void onBefore(MethodData methodData) {
                self.onBefore(methodData);
                methodCallback.onBefore(methodData);
            }

            @Override
            public void onAfter(MethodData methodData) {
                self.onAfter(methodData);
                methodCallback.onAfter(methodData);
            }

            @Override
            public void onEndIteration() {
                self.onEndIteration();
                methodCallback.onEndIteration();
            }

            @Override
            public boolean continueIteration() {
                return self.continueIteration() || methodCallback.continueIteration();
            }
        };
    }

    default MethodCallback andAfter(MethodReturn methodReturn) {
        MethodCallback self = this;
        return new MethodCallback() {
            @Override
            public void onBefore(MethodData methodData) {
                self.onBefore(methodData);
            }

            @Override
            public void onAfter(MethodData methodData) {
                self.onAfter(methodData);
                methodReturn.onAfter(methodData);
            }

            @Override
            public void onEndIteration() {
                self.onEndIteration();
            }

            @Override
            public boolean continueIteration() {
                return self.continueIteration();
            }
        };
    }

    interface MethodReturn {
        void onAfter(MethodData methodData);
    }
}
