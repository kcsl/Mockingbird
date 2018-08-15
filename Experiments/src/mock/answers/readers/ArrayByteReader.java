package mock.answers.readers;

import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 * @author Derrick Lockwood
 * @created 8/7/18.
 */
public class ArrayByteReader extends ByteReader {

    //TODO: Collection Byte Reader?
    public ArrayByteReader(String name, int fixedSize) {
        super(name, null, fixedSize);
    }

    @Override
    protected ByteReader duplicateByteReader() {
        return null;
    }

    @Override
    protected void handleReadException(IOException e) {

    }

    @Override
    protected Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) throws IOException {
        if (returnType.isAssignableFrom(Integer[].class)) {

        }

        return null;
    }

    @Override
    protected Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }
}
