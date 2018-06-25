package mock.answers.readers;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author Derrick Lockwood
 * @created 6/21/18.
 */
public class ChunkByteReader extends ByteReader {

    public ChunkByteReader(int chunkSize) {
        super(null, chunkSize);
    }

    @Override
    void handleReadException(IOException e) {
        throw new RuntimeException(e.getCause());
    }

    @Override
    Object readNonPrimitiveClass(Class<?> returnType, DataInput dataInput) throws IOException {
        if (returnType.isAssignableFrom(String.class)) {
            byte[] bytes = new byte[chunkSize];
            dataInput.readFully(bytes);
            return new String(bytes);
        }
        return null;
    }

    @Override
    Object postProcessing(Class<?> returnType, Object object) {
        return object;
    }
}
