package mock.harness;

import mock.MockProcess;
import mock.ParameterSet;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Derrick Lockwood
 * @created 5/17/18.
 */
public abstract class RangeHarness implements ParameterHarness {
    private final int finish;
    private final int increment;
    private final int timesToApply;
    private final int runningAvgSize;
    private final List<Queue<MockProcess>> mockProcesses;
    private final List<Duration> durations;
    private int avgIndex, index;

    RangeHarness(int start, int finish, int increment, int timesToApply, int runningAvgSize) {
        this.finish = finish;
        this.increment = increment;
        this.timesToApply = timesToApply;
        this.runningAvgSize = runningAvgSize;
        this.index = start - increment;
        avgIndex = 0;
        mockProcesses = new ArrayList<>((int) Math.ceil((finish - start) / increment));
        durations = new ArrayList<>();
    }

    @Override
    public void handle(MockProcess mockProcess) {
        Queue<MockProcess> queue = mockProcesses.get(avgIndex);
        if (queue == null) {
            mockProcesses.set(avgIndex, new LinkedList<>());
        } else if (queue.size() >= this.runningAvgSize) {
            queue.poll();
        }
        mockProcesses.get(avgIndex).offer(mockProcess);
    }

    @Override
    public ParameterSet getRules() {
        if (this.index < this.finish) {
            this.index += this.increment;
        } else {
            this.index = 0;
            avgIndex++;
        }
        return getRules(this.index);
    }


    @Override
    public boolean isDone() {
        return avgIndex < timesToApply;
    }

    protected abstract ParameterSet getRules(int index);
}
