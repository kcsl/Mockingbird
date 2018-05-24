package mock.answers;

/**
 * @author Derrick Lockwood
 * @created 5/17/18.
 */
public class IntIncrementAnswer implements Answer<Integer> {
    private final int increment;
    private final int start;
    private int index;

    public IntIncrementAnswer() {
        this(1);
    }

    public IntIncrementAnswer(int increment) {
        this(0, increment);
    }

    public IntIncrementAnswer(int start, int increment) {
        this.start = start;
        index = start;
        this.increment = increment;
    }

    public int getIndex() {
        return index;
    }

    public void increment() {
        index += increment;
    }

    public void reset() {
        index = start;
    }


    @Override
    public Integer handle(InvocationData invocationData) throws Throwable {
        return index;
    }
}
