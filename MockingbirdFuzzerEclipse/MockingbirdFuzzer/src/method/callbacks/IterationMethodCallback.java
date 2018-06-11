package method.callbacks;

import java.math.BigInteger;

import method.MethodData;

/**
 * @author Derrick Lockwood
 * @created 6/6/18.
 */
public class IterationMethodCallback implements MethodCallback {

    private BigInteger i;
    private final BigInteger finish;

    public IterationMethodCallback(BigInteger n) {
        i = BigInteger.ZERO;
        finish = n;
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

    public static MethodCallback create(long n) {
        return new IterationMethodCallback(BigInteger.valueOf(n));
    }
}
