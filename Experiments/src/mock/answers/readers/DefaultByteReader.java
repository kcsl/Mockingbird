package mock.answers.readers;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class DefaultByteReader extends ByteReader {

    public DefaultByteReader() {
        super(null, -1);
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
        return object;
    }

    @Override
    ByteReader duplicateByteReader() {
        return new DefaultByteReader();
    }
}
