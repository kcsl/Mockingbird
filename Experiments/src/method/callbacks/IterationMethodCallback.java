package method.callbacks;

import method.MethodData;

import java.math.BigInteger;

/**
 * @author Derrick Lockwood
 * @created 6/6/18.
 */
public class IterationMethodCallback implements MethodCallback {

    private final BigInteger finish;
    private BigInteger i;

    public IterationMethodCallback(BigInteger n) {
        i = BigInteger.ZERO;
        finish = n;
    }

    public static MethodCallback create(long n) {
        return new IterationMethodCallback(BigInteger.valueOf(n));
    }

    @Override
    public void onBefore(MethodData methodData) {

    }

    @Override
    public void onAfter(MethodData methodData) {
        i = i.add(BigInteger.ONE);
    }

    @Override
    public void onEndIteration() {

    }

    @Override
    public boolean continueIteration() {
        return i.compareTo(this.finish) < 0;
    }
}
