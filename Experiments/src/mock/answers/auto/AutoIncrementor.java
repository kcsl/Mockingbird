package mock.answers.auto;

import mock.answers.RedefineAnswer;
import mock.answers.StaticAnswer;
import mock.answers.SubAnswer;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Callable;

/**
 * @author Derrick Lockwood
 * @created 6/6/18.
 */
public class AutoIncrementor implements AutoAnswer {

    private static final BigDecimal CHAR_SIZE = BigDecimal.valueOf(256);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private BigDecimal value;
    private final BigDecimal increment;
    private IncrementorHandler handler;

    public AutoIncrementor(BigDecimal start, BigDecimal increment, int precision) {
        this.value = start.setScale(precision, RoundingMode.HALF_DOWN);
        this.increment = increment;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setIncrementHandler(IncrementorHandler handler) {
        this.handler = handler;
    }

    @Override
    public void mutate() {
        this.value = this.value.add(this.increment);
    }

    @Override
    public Object createObject(Class<?> type) {
        return getIncrement(type);
    }

    private Object getIncrement(Class<?> type) {
        if (type.isAssignableFrom(int.class)) {
            return this.value.intValue();
        } else if (type.isAssignableFrom(long.class)) {
            return this.value.longValue();
        } else if (type.isAssignableFrom(byte.class)) {
            return this.value.byteValue();
        } else if (type.isAssignableFrom(char.class)) {
            return (char) this.value.intValue();
        } else if (type.isAssignableFrom(float.class)) {
            return this.value.floatValue();
        } else if (type.isAssignableFrom(double.class)) {
            return this.value.doubleValue();
        } else if (type.isAssignableFrom(String.class)) {
            return getString();
        } else if (type.isAssignableFrom(boolean.class)) {
            return this.value.remainder(TWO).intValue() == 0;
        } else if (handler != null) {
            return handler.handle(type, this.value);
        }
        throw new RuntimeException("Incrementor not allowed on class " + type.toString());
    }
    private String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        BigDecimal[] divAndRemainder = this.value.divideAndRemainder(CHAR_SIZE);
        while (divAndRemainder[0].compareTo(CHAR_SIZE) >= 0) {
            stringBuilder.insert(0, (char) divAndRemainder[1].intValue());
            divAndRemainder = divAndRemainder[0].divideAndRemainder(CHAR_SIZE);
        }
        stringBuilder.insert(0, (char) divAndRemainder[1].intValue());
        if (divAndRemainder[0].intValue() > 0) {
            stringBuilder.insert(0, (char) divAndRemainder[0].subtract(BigDecimal.ONE).intValue());
        }
        return stringBuilder.toString();
    }

    public static AutoIncrementor createPrecisionIncrementor(int precision) {
        return new AutoIncrementor(BigDecimal.ZERO, BigDecimal.valueOf((double) 1 / Math.pow(10, precision)), precision);
    }

    public static AutoIncrementor createIncrementor() {
        return createEndPrecisionIncrementor(1);
    }

    public static AutoIncrementor createEndPrecisionIncrementor(int precision) {
        return createIncrementPrecisionIncrementor(1, precision);
    }

    public static AutoIncrementor createIncrementPrecisionIncrementor(double increment, int precision) {
        return createIncrementor(0, increment, precision);
    }

    public static AutoIncrementor createIncrementor(double start, double increment, int precision) {
        return new AutoIncrementor(BigDecimal.valueOf(start), BigDecimal.valueOf(increment), precision);
    }

    public interface IncrementorHandler {
        Object handle(Class<?> type, BigDecimal value);
    }
}
