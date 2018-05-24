package mock.harness;

import mock.MockProcess;
import mock.ParameterSet;

/**
 * @author Derrick Lockwood
 * @created 5/17/18.
 */
public interface ParameterHarness {

    void handle(MockProcess mockProcess);

    ParameterSet getRules();

    boolean isDone();

}
