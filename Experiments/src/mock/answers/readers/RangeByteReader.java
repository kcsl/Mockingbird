package mock.answers.readers;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class RangeByteReader extends ByteReader {
    private final long delta;
    private final long start;

    public RangeByteReader(long start, long finish) {
        super(null, -1);
        this.start = start;
        delta = finish - start;
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
        if (returnType.isAssignableFrom(byte.class)) {
            return (byte) ((byte) object % delta + start);
        } else if (returnType.isAssignableFrom(char.class)) {
            return (char) ((char) object % delta + start);
        } else if (returnType.isAssignableFrom(short.class)) {
            return (short) ((short) object % delta + start);
        } else if (returnType.isAssignableFrom(int.class)) {
            return (int) ((int) object % delta + start);
        } else if (returnType.isAssignableFrom(long.class)) {
            return (long) object % delta + start;
        } else if (returnType.isAssignableFrom(float.class)) {
            return (float) object % delta + start;
        } else if (returnType.isAssignableFrom(double.class)) {
            return (double) object % delta + start;
        }
        return object;
    }
}
