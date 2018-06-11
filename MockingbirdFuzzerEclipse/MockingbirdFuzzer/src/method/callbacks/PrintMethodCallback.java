package method.callbacks;

import method.MethodData;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class PrintMethodCallback implements MethodCallback {

    private final OutputRule outputRule;

    private PrintMethodCallback(OutputRule outputRule) {
        this.outputRule = outputRule;
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {
        if (outputRule == null) {
            System.out.println(methodData.toString());
        } else {
            String out = outputRule.getOutput(methodData);
            if (out != null) {
                System.out.print(out);
            }
        }
    }

    @Override
    public void onEndIteration() {
        System.out.println("Finished");
    }

    @Override
    public boolean continueIteration() {
        return false;
    }

    public static PrintMethodCallback create() {
        return new PrintMethodCallback(null);
    }

    public static PrintMethodCallback create(OutputRule outputRule) {
        return new PrintMethodCallback(outputRule);
    }
}
