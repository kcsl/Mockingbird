package mock.answers.readers;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class DefaultByteReader extends ByteReader {

    public DefaultByteReader(String name) {
        super(name,null, -1);
    }

    @Override
    protected void handleReadException(IOException e) {
        throw new RuntimeException(e.getCause());
    }

    @Override
    protected Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) {
        return null;
    }

    @Override
    protected Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }

    @Override
    protected ByteReader duplicateByteReader() {
        return new DefaultByteReader(name);
    }
}
