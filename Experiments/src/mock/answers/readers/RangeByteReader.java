package mock.answers.readers;

import java.io.DataInput;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class RangeByteReader extends ByteReader {
    private final long max;
    private final long min;
    private final double range;

    public RangeByteReader(long min, long max) {
        super(null, -1);
        if (max < min) {
            long tmp = min;
            min = max;
            max = tmp;
        }
        this.min = min;
        this.max = max;
        this.range = this.max - this.min;
    }

    @Override
    void handleReadException(IOException e) {
        throw new RuntimeException(e.getCause());
    }

    @Override
    Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) {
        return null;
    }

    @Override
    Object postProcessing(Class<?> returnType, Object object) {
        if (object == null) {
            return null;
        }
        if (returnType.isAssignableFrom(byte.class) || returnType.isAssignableFrom(Byte.class)) {
            return (byte) getRangedValue((double) Byte.MIN_VALUE, (double) Byte.MAX_VALUE, (double) (byte) object);
        } else if (returnType.isAssignableFrom(char.class) || returnType.isAssignableFrom(Character.class)) {
            return (char) getRangedValue((double) Character.MIN_VALUE, (double) Character.MAX_VALUE,
                    (double) (char) object);
        } else if (returnType.isAssignableFrom(short.class) || returnType.isAssignableFrom(Short.class)) {
            return (short) getRangedValue((double) Short.MIN_VALUE, (double) Short.MAX_VALUE, (double) (short) object);
        } else if (returnType.isAssignableFrom(int.class) || returnType.isAssignableFrom(Integer.class)) {
            return (int) getRangedValue((double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE, (double) (int) object);
        } else if (returnType.isAssignableFrom(long.class) || returnType.isAssignableFrom(Long.class)) {
            return (long) getRangedValue((double) Long.MIN_VALUE, (double) Long.MAX_VALUE, (double) (long) object);
        } else if (returnType.isAssignableFrom(float.class) || returnType.isAssignableFrom(Float.class)) {
            return (float) getRangedValue((double) Float.MIN_VALUE, (double) Float.MAX_VALUE, (double) (float) object);
        } else if (returnType.isAssignableFrom(double.class) || returnType.isAssignableFrom(Double.class)) {
            BigDecimal min = BigDecimal.valueOf(Double.MIN_VALUE);
            BigDecimal range = BigDecimal.valueOf(Double.MAX_VALUE).subtract(min);
            BigDecimal thisRange = BigDecimal.valueOf(this.range);
            if (range.compareTo(thisRange) >= 0) {
                return object;
            }
            BigDecimal obj = BigDecimal.valueOf((double) object);
            return obj.subtract(min).multiply(BigDecimal.valueOf(this.range)).divide(range).add(
                    BigDecimal.valueOf(this.min)).doubleValue();
        }
        return object;
    }

    private double getRangedValue(double min, double max, double obj) {
        double range = max - min;
        if (this.range >= range) {
            return obj;
        }
        return (((obj - min) * this.range) / range) + this.min;
    }

    @Override
    ByteReader duplicateByteReader() {
        return new RangeByteReader(min, max);
    }
}
