package method.callbacks;

import method.MethodData;
import mock.answers.auto.AutoAnswer;
import mock.answers.auto.AutoIncrementor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Derrick Lockwood
 * @created 6/7/18.
 */
public class AutoMethodCallback implements MethodCallback {

    private final List<AutoAnswer> autoAnswers;

    public AutoMethodCallback() {
        autoAnswers = new ArrayList<>();
    }

    public void add(AutoIncrementor autoIncrementor) {
        autoAnswers.add(autoIncrementor);
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {
        for (AutoAnswer autoAnswer : autoAnswers) {
            autoAnswer.mutate();
        }
    }

    @Override
    public void onEndIteration() {

    }

    @Override
    public boolean continueIteration() {
        return false;
    }

    public static AutoMethodCallback create(AutoAnswer... autoAnswers) {
        AutoMethodCallback autoMethodCallback = new AutoMethodCallback();
        autoMethodCallback.autoAnswers.addAll(Arrays.asList(autoAnswers));
        return autoMethodCallback;
    }
}
